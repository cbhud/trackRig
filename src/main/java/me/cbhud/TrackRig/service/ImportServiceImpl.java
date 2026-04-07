package me.cbhud.TrackRig.service;

import me.cbhud.TrackRig.dto.ImportErrorResponse;
import me.cbhud.TrackRig.dto.ImportResultResponse;
import me.cbhud.TrackRig.model.Component;
import me.cbhud.TrackRig.model.ComponentCategory;
import me.cbhud.TrackRig.model.ComponentStatus;
import me.cbhud.TrackRig.model.Workstation;
import me.cbhud.TrackRig.repository.ComponentCategoryRepository;
import me.cbhud.TrackRig.repository.ComponentRepository;
import me.cbhud.TrackRig.repository.ComponentStatusRepository;
import me.cbhud.TrackRig.repository.WorkstationRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implements batch component import from an Excel (.xlsx or .xls) file.
 *
 * Validation strategy (per row):
 * 1. serialNumber — not blank, max 100 chars
 * 2. serialNumber — not already in the DB (existsBySerialNumber)
 * 3. serialNumber — not duplicated within the uploaded file
 * 4. name — not blank, max 200 chars
 * 5. categoryName — must match an existing ComponentCategory (case-insensitive)
 * 6. statusName — must match an existing ComponentStatus (case-insensitive)
 * 7. workstationId (optional) — if provided must be a valid integer and
 * existing Workstation
 * 8. purchaseDate (optional) — must be a valid yyyy-MM-dd date
 *
 * Partial success: rows that pass all validations are batch-saved regardless of
 * how many other rows fail. All errors are returned in ImportResultResponse.
 */
@Service
public class ImportServiceImpl implements ImportService {

    // Expected column indices (0-based) in the Excel sheet
    private static final int COL_SERIAL_NUMBER = 0;
    private static final int COL_NAME = 1;
    private static final int COL_CATEGORY_NAME = 2;
    private static final int COL_STATUS_NAME = 3;
    private static final int COL_WORKSTATION_ID = 4;
    private static final int COL_PURCHASE_DATE = 5;
    private static final int COL_NOTES = 6;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_SERIAL_LENGTH = 100;
    private static final int MAX_NAME_LENGTH = 200;

    private final ComponentRepository componentRepository;
    private final ComponentCategoryRepository categoryRepository;
    private final ComponentStatusRepository statusRepository;
    private final WorkstationRepository workstationRepository;

    public ImportServiceImpl(ComponentRepository componentRepository,
            ComponentCategoryRepository categoryRepository,
            ComponentStatusRepository statusRepository,
            WorkstationRepository workstationRepository) {
        this.componentRepository = componentRepository;
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
        this.workstationRepository = workstationRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER') or hasRole('OWNER')")
    public ImportResultResponse importComponentsFromExcel(MultipartFile file) {
        List<ImportErrorResponse> errors = new ArrayList<>();
        List<Component> toSave = new ArrayList<>();

        // Tracks serial numbers seen within this file to catch in-file duplicates
        Set<String> seenSerials = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Row 0 is the header — skip it, data starts at row index 1 (displayed as row
            // 2)
            int lastRow = sheet.getLastRowNum();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);

                // Skip completely blank rows
                if (row == null || isRowBlank(row)) {
                    continue;
                }

                // Excel rows are 0-indexed; the human-readable row number is i+1
                int displayRow = i + 1;

                List<ImportErrorResponse> rowErrors = validateAndCollect(
                        row, displayRow, seenSerials, toSave, errors);
                errors.addAll(rowErrors);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Excel file: " + e.getMessage(), e);
        }

        // Batch-save all valid components in a single call
        if (!toSave.isEmpty()) {
            componentRepository.saveAll(toSave);
        }

        return new ImportResultResponse(toSave.size(), errors);
    }

    // ========================
    // VALIDATION LOGIC
    // ========================

    /**
     * Validates a single Excel row, builds a Component if valid, and returns any
     * row-level errors. Valid components are added to toSave; errors are returned.
     */
    private List<ImportErrorResponse> validateAndCollect(Row row,
            int displayRow,
            Set<String> seenSerials,
            List<Component> toSave,
            List<ImportErrorResponse> existingErrors) {
        List<ImportErrorResponse> rowErrors = new ArrayList<>();

        // --- Column A: serialNumber ---
        String serialNumber = getCellString(row, COL_SERIAL_NUMBER);

        if (serialNumber == null || serialNumber.isBlank()) {
            rowErrors.add(new ImportErrorResponse(displayRow, "serialNumber",
                    "Serial number is required"));
        } else if (serialNumber.length() > MAX_SERIAL_LENGTH) {
            rowErrors.add(new ImportErrorResponse(displayRow, "serialNumber",
                    "Serial number exceeds maximum length of " + MAX_SERIAL_LENGTH + " characters"));
        } else if (!seenSerials.add(serialNumber)) {
            // add() returns false if the value was already in the set → duplicate in file
            rowErrors.add(new ImportErrorResponse(displayRow, "serialNumber",
                    "Duplicate serial number within the uploaded file: '" + serialNumber + "'"));
        } else if (componentRepository.existsBySerialNumber(serialNumber)) {
            rowErrors.add(new ImportErrorResponse(displayRow, "serialNumber",
                    "Serial number already exists in the database: '" + serialNumber + "'"));
        }

        // --- Column B: name ---
        String name = getCellString(row, COL_NAME);

        if (name == null || name.isBlank()) {
            rowErrors.add(new ImportErrorResponse(displayRow, "name",
                    "Component name is required"));
        } else if (name.length() > MAX_NAME_LENGTH) {
            rowErrors.add(new ImportErrorResponse(displayRow, "name",
                    "Name exceeds maximum length of " + MAX_NAME_LENGTH + " characters"));
        }

        // --- Column C: categoryName ---
        String categoryName = getCellString(row, COL_CATEGORY_NAME);
        ComponentCategory category = null;

        if (categoryName == null || categoryName.isBlank()) {
            rowErrors.add(new ImportErrorResponse(displayRow, "categoryName",
                    "Category name is required"));
        } else {
            Optional<ComponentCategory> catOpt = categoryRepository.findByNameIgnoreCase(categoryName);
            if (catOpt.isEmpty()) {
                rowErrors.add(new ImportErrorResponse(displayRow, "categoryName",
                        "Category '" + categoryName + "' not found"));
            } else {
                category = catOpt.get();
            }
        }

        // --- Column D: statusName ---
        String statusName = getCellString(row, COL_STATUS_NAME);
        ComponentStatus status = null;

        if (statusName == null || statusName.isBlank()) {
            rowErrors.add(new ImportErrorResponse(displayRow, "statusName",
                    "Status name is required"));
        } else {
            Optional<ComponentStatus> statusOpt = statusRepository.findByNameIgnoreCase(statusName);
            if (statusOpt.isEmpty()) {
                rowErrors.add(new ImportErrorResponse(displayRow, "statusName",
                        "Status '" + statusName + "' not found"));
            } else {
                status = statusOpt.get();
            }
        }

        // --- Column E: workstationId (optional) ---
        String workstationIdRaw = getCellString(row, COL_WORKSTATION_ID);
        Workstation workstation = null;

        if (workstationIdRaw != null && !workstationIdRaw.isBlank()) {
            try {
                // Excel may store numbers as "1.0" — trim decimal part
                int wsId = (int) Double.parseDouble(workstationIdRaw.trim());
                Optional<Workstation> wsOpt = workstationRepository.findById(wsId);
                if (wsOpt.isEmpty()) {
                    rowErrors.add(new ImportErrorResponse(displayRow, "workstationId",
                            "Workstation with ID " + wsId + " not found"));
                } else {
                    workstation = wsOpt.get();
                }
            } catch (NumberFormatException e) {
                rowErrors.add(new ImportErrorResponse(displayRow, "workstationId",
                        "Workstation ID must be a valid integer, got: '" + workstationIdRaw + "'"));
            }
        }

        // --- Column F: purchaseDate (optional) ---
        String purchaseDateRaw = getCellString(row, COL_PURCHASE_DATE);
        LocalDate purchaseDate = null;

        if (purchaseDateRaw != null && !purchaseDateRaw.isBlank()) {
            try {
                purchaseDate = LocalDate.parse(purchaseDateRaw.trim(), DATE_FMT);
            } catch (DateTimeParseException e) {
                rowErrors.add(new ImportErrorResponse(displayRow, "purchaseDate",
                        "Invalid date format. Expected yyyy-MM-dd, got: '" + purchaseDateRaw + "'"));
            }
        }

        // --- Column G: notes (optional, no validation) ---
        String notes = getCellString(row, COL_NOTES);

        // If this row has any errors, skip saving it
        if (!rowErrors.isEmpty()) {
            return rowErrors;
        }

        // All validations passed — build the entity
        Component component = new Component();
        component.setSerialNumber(serialNumber);
        component.setName(name);
        component.setComponentCategory(category);
        component.setComponentStatus(status);
        component.setWorkstation(workstation); // null = storage
        component.setPurchaseDate(purchaseDate);
        component.setNotes(notes);

        toSave.add(component);
        return rowErrors; // empty list
    }

    // ========================
    // CELL READING HELPERS
    // ========================

    /**
     * Reads a cell value as a String regardless of its actual cell type.
     * Numeric cells are converted to string (trimming ".0" for whole numbers).
     * Returns null if the cell is null or BLANK.
     */
    private String getCellString(Row row, int colIndex) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colIndex,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> {
                String val = cell.getStringCellValue().trim();
                yield val.isEmpty() ? null : val;
            }
            case NUMERIC -> {
                double num = cell.getNumericCellValue();
                // Return as integer string if it's a whole number (e.g. workstationId)
                if (num == Math.floor(num) && !Double.isInfinite(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCachedFormulaResultType().toString();
            default -> null;
        };
    }

    /**
     * Returns true if all columns in the row up to COL_NOTES are empty.
     * Prevents processing trailing empty rows that some Excel editors append.
     */
    private boolean isRowBlank(Row row) {
        for (int col = COL_SERIAL_NUMBER; col <= COL_NOTES; col++) {
            String val = getCellString(row, col);
            if (val != null && !val.isBlank())
                return false;
        }
        return true;
    }
}
