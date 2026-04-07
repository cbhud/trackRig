package me.cbhud.TrackRig.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import me.cbhud.TrackRig.model.Component;
import me.cbhud.TrackRig.model.MaintenanceLog;
import me.cbhud.TrackRig.model.Workstation;
import me.cbhud.TrackRig.repository.ComponentRepository;
import me.cbhud.TrackRig.repository.MaintenanceLogRepository;
import me.cbhud.TrackRig.repository.WorkstationRepository;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implements Excel and PDF export for workstations and components.
 *
 * Excel: Uses XSSFWorkbook (in-memory) — avoids SXSSFWorkbook's temp-file
 * lifecycle issue where close() tries to flush/dispose already-closed streams.
 * XSSFWorkbook is perfectly adequate for any realistic dataset in this app.
 *
 * PDF: Uses OpenPDF PdfPTable with alternating row shading and a title banner.
 */
@Service
public class ExportServiceImpl implements ExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WorkstationRepository workstationRepository;
    private final ComponentRepository componentRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;

    public ExportServiceImpl(WorkstationRepository workstationRepository,
            ComponentRepository componentRepository,
            MaintenanceLogRepository maintenanceLogRepository) {
        this.workstationRepository = workstationRepository;
        this.componentRepository = componentRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
    }

    // ========================
    // WORKSTATION EXCEL EXPORT
    // ========================

    @Override
    public byte[] exportWorkstationsToExcel(boolean includeComponents, boolean includeLogs) {
        List<Workstation> workstations = workstationRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = buildExcelHeaderStyle(workbook);

            // --- Sheet 1: Workstations ---
            Sheet wsSheet = workbook.createSheet("Workstations");
            String[] wsHeaders = { "ID", "Name", "Status", "Grid X", "Grid Y", "Created At" };
            writeExcelHeaderRow(wsSheet, headerStyle, wsHeaders);

            int rowIdx = 1;
            for (Workstation ws : workstations) {
                org.apache.poi.ss.usermodel.Row row = wsSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(ws.getId());
                row.createCell(1).setCellValue(nullSafe(ws.getName()));
                row.createCell(2).setCellValue(ws.getWorkstationStatus() != null
                        ? ws.getWorkstationStatus().getName()
                        : "");
                row.createCell(3).setCellValue(ws.getGridX());
                row.createCell(4).setCellValue(ws.getGridY());
                row.createCell(5).setCellValue(ws.getCreatedAt() != null
                        ? ws.getCreatedAt().format(DATETIME_FMT)
                        : "");
            }
            autoSizeColumns(wsSheet, wsHeaders.length);

            // --- Sheet 2 (optional): Components ---
            if (includeComponents) {
                List<Component> components = componentRepository.findAll();
                Sheet compSheet = workbook.createSheet("Components");
                String[] compHeaders = { "ID", "Serial Number", "Name", "Category",
                        "Status", "Workstation", "Purchase Date", "Warranty Expiry",
                        "Notes", "Created At" };
                writeExcelHeaderRow(compSheet, headerStyle, compHeaders);
                int ci = 1;
                for (Component c : components) {
                    org.apache.poi.ss.usermodel.Row row = compSheet.createRow(ci++);
                    writeComponentRow(row, c);
                }
                autoSizeColumns(compSheet, compHeaders.length);
            }

            // --- Sheet 3 (optional): Maintenance Logs ---
            if (includeLogs) {
                List<MaintenanceLog> logs = maintenanceLogRepository.findAll();
                Sheet logSheet = workbook.createSheet("Maintenance Logs");
                String[] logHeaders = { "ID", "Workstation", "Maintenance Type",
                        "Performed By", "Performed At", "Notes" };
                writeExcelHeaderRow(logSheet, headerStyle, logHeaders);
                int li = 1;
                for (MaintenanceLog log : logs) {
                    org.apache.poi.ss.usermodel.Row row = logSheet.createRow(li++);
                    row.createCell(0).setCellValue(log.getId());
                    row.createCell(1).setCellValue(log.getWorkstation() != null
                            ? log.getWorkstation().getName()
                            : "");
                    row.createCell(2).setCellValue(log.getMaintenanceType() != null
                            ? log.getMaintenanceType().getName()
                            : "");
                    row.createCell(3).setCellValue(log.getPerformedBy() != null
                            ? log.getPerformedBy().getFullName()
                            : "");
                    row.createCell(4).setCellValue(log.getPerformedAt() != null
                            ? log.getPerformedAt().format(DATETIME_FMT)
                            : "");
                    row.createCell(5).setCellValue(nullSafe(log.getNotes()));
                }
                autoSizeColumns(logSheet, logHeaders.length);
            }

            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate workstation Excel export", e);
        }

        return out.toByteArray();
    }

    // ========================
    // WORKSTATION PDF EXPORT
    // ========================

    @Override
    public byte[] exportWorkstationsToPdf(boolean includeComponents, boolean includeLogs) {
        List<Workstation> workstations = workstationRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(buildPdfTitle("TrackRig \u2014 Workstation Export"));
            doc.add(buildPdfSectionLabel("Workstations"));

            String[] wsHeaders = { "ID", "Name", "Status", "Grid X", "Grid Y", "Created At" };
            PdfPTable wsTable = new PdfPTable(wsHeaders.length);
            wsTable.setWidthPercentage(100);
            addPdfHeaderRow(wsTable, wsHeaders);

            boolean alternate = false;
            for (Workstation ws : workstations) {
                Color rowColour = alternate ? new Color(240, 240, 240) : Color.WHITE;
                addPdfCell(wsTable, String.valueOf(ws.getId()), rowColour);
                addPdfCell(wsTable, nullSafe(ws.getName()), rowColour);
                addPdfCell(wsTable, ws.getWorkstationStatus() != null
                        ? ws.getWorkstationStatus().getName()
                        : "", rowColour);
                addPdfCell(wsTable, String.valueOf(ws.getGridX()), rowColour);
                addPdfCell(wsTable, String.valueOf(ws.getGridY()), rowColour);
                addPdfCell(wsTable, ws.getCreatedAt() != null
                        ? ws.getCreatedAt().format(DATETIME_FMT)
                        : "", rowColour);
                alternate = !alternate;
            }
            doc.add(wsTable);

            if (includeComponents) {
                doc.newPage();
                doc.add(buildPdfSectionLabel("Components"));
                doc.add(buildComponentPdfTable(componentRepository.findAll()));
            }

            if (includeLogs) {
                doc.newPage();
                doc.add(buildPdfSectionLabel("Maintenance Logs"));
                doc.add(buildLogPdfTable(maintenanceLogRepository.findAll()));
            }

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate workstation PDF export", e);
        }

        return out.toByteArray();
    }

    // ========================
    // COMPONENT EXCEL EXPORT
    // ========================

    @Override
    public byte[] exportComponentsToExcel(Integer statusId, Integer categoryId, Boolean inStorage) {
        List<Component> components = componentRepository.findByFilters(statusId, categoryId, inStorage);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Components");
            CellStyle headerStyle = buildExcelHeaderStyle(workbook);

            String[] headers = { "ID", "Serial Number", "Name", "Category",
                    "Status", "Workstation", "Purchase Date", "Warranty Expiry",
                    "Notes", "Created At" };
            writeExcelHeaderRow(sheet, headerStyle, headers);

            int rowIdx = 1;
            for (Component c : components) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                writeComponentRow(row, c);
            }
            autoSizeColumns(sheet, headers.length);
            workbook.write(out);
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate component Excel export", e);
        }

        return out.toByteArray();
    }

    // ========================
    // COMPONENT PDF EXPORT
    // ========================

    @Override
    public byte[] exportComponentsToPdf(Integer statusId, Integer categoryId, Boolean inStorage) {
        List<Component> components = componentRepository.findByFilters(statusId, categoryId, inStorage);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(buildPdfTitle("TrackRig \u2014 Component Export"));
            doc.add(buildPdfSectionLabel("Components (" + components.size() + " records)"));
            doc.add(buildComponentPdfTable(components));
            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to generate component PDF export", e);
        }

        return out.toByteArray();
    }

    // ========================
    // SHARED EXCEL HELPERS
    // ========================

    private CellStyle buildExcelHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private void writeExcelHeaderRow(Sheet sheet, CellStyle style, String[] headers) {
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void writeComponentRow(org.apache.poi.ss.usermodel.Row row, Component c) {
        row.createCell(0).setCellValue(c.getId());
        row.createCell(1).setCellValue(nullSafe(c.getSerialNumber()));
        row.createCell(2).setCellValue(nullSafe(c.getName()));
        row.createCell(3).setCellValue(c.getComponentCategory() != null
                ? c.getComponentCategory().getName()
                : "");
        row.createCell(4).setCellValue(c.getComponentStatus() != null
                ? c.getComponentStatus().getName()
                : "");
        row.createCell(5).setCellValue(c.getWorkstation() != null
                ? c.getWorkstation().getName()
                : "Storage");
        row.createCell(6).setCellValue(c.getPurchaseDate() != null
                ? c.getPurchaseDate().format(DATE_FMT)
                : "");
        row.createCell(7).setCellValue(c.getWarrantyDate() != null
                ? c.getWarrantyDate().format(DATE_FMT)
                : "");
        row.createCell(8).setCellValue(nullSafe(c.getNotes()));
        row.createCell(9).setCellValue(c.getCreatedAt() != null
                ? c.getCreatedAt().format(DATETIME_FMT)
                : "");
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ========================
    // SHARED PDF HELPERS
    // ========================

    private Paragraph buildPdfTitle(String text) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(20, 45, 90));
        Paragraph p = new Paragraph(text, titleFont);
        p.setSpacingAfter(8f);
        return p;
    }

    private Paragraph buildPdfSectionLabel(String text) {
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(50, 80, 130));
        Paragraph p = new Paragraph(text, sectionFont);
        p.setSpacingBefore(12f);
        p.setSpacingAfter(4f);
        return p;
    }

    private void addPdfHeaderRow(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Color headerBg = new Color(20, 45, 90);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setPadding(5f);
            table.addCell(cell);
        }
    }

    private void addPdfCell(PdfPTable table, String text, Color bg) {
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", bodyFont));
        cell.setBackgroundColor(bg);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private PdfPTable buildComponentPdfTable(List<Component> components) throws DocumentException {
        String[] headers = { "ID", "Serial Number", "Name", "Category",
                "Status", "Workstation", "Purchase Date", "Warranty Expiry", "Notes" };
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        addPdfHeaderRow(table, headers);

        boolean alternate = false;
        for (Component c : components) {
            Color rowColour = alternate ? new Color(240, 240, 240) : Color.WHITE;
            addPdfCell(table, String.valueOf(c.getId()), rowColour);
            addPdfCell(table, nullSafe(c.getSerialNumber()), rowColour);
            addPdfCell(table, nullSafe(c.getName()), rowColour);
            addPdfCell(table, c.getComponentCategory() != null
                    ? c.getComponentCategory().getName()
                    : "", rowColour);
            addPdfCell(table, c.getComponentStatus() != null
                    ? c.getComponentStatus().getName()
                    : "", rowColour);
            addPdfCell(table, c.getWorkstation() != null
                    ? c.getWorkstation().getName()
                    : "Storage", rowColour);
            addPdfCell(table, c.getPurchaseDate() != null
                    ? c.getPurchaseDate().format(DATE_FMT)
                    : "", rowColour);
            addPdfCell(table, c.getWarrantyDate() != null
                    ? c.getWarrantyDate().format(DATE_FMT)
                    : "", rowColour);
            addPdfCell(table, nullSafe(c.getNotes()), rowColour);
            alternate = !alternate;
        }
        return table;
    }

    private PdfPTable buildLogPdfTable(List<MaintenanceLog> logs) throws DocumentException {
        String[] headers = { "ID", "Workstation", "Maintenance Type",
                "Performed By", "Performed At", "Notes" };
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        addPdfHeaderRow(table, headers);

        boolean alternate = false;
        for (MaintenanceLog log : logs) {
            Color rowColour = alternate ? new Color(240, 240, 240) : Color.WHITE;
            addPdfCell(table, String.valueOf(log.getId()), rowColour);
            addPdfCell(table, log.getWorkstation() != null
                    ? log.getWorkstation().getName()
                    : "", rowColour);
            addPdfCell(table, log.getMaintenanceType() != null
                    ? log.getMaintenanceType().getName()
                    : "", rowColour);
            addPdfCell(table, log.getPerformedBy() != null
                    ? log.getPerformedBy().getFullName()
                    : "", rowColour);
            addPdfCell(table, log.getPerformedAt() != null
                    ? log.getPerformedAt().format(DATETIME_FMT)
                    : "", rowColour);
            addPdfCell(table, nullSafe(log.getNotes()), rowColour);
            alternate = !alternate;
        }
        return table;
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }
}
