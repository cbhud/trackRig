package me.cbhud.TrackRig.service;

import me.cbhud.TrackRig.dto.ImportResultResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for batch importing components from an Excel file.
 */
public interface ImportService {

    /**
     * Parses the uploaded .xlsx file, validates each data row, and batch-saves
     * all valid components. Invalid rows are collected and returned in the result
     * rather than causing a full rollback — partial success is intentional.
     *
     * <p>
     * Expected column layout (row 1 = header row, ignored):
     * <ol>
     * <li>serialNumber</li>
     * <li>name</li>
     * <li>categoryName (resolved by name, case-insensitive)</li>
     * <li>statusName (resolved by name, case-insensitive)</li>
     * <li>workstationId (optional integer — leave blank for storage)</li>
     * <li>purchaseDate (optional, format: yyyy-MM-dd)</li>
     * <li>notes (optional)</li>
     * </ol>
     *
     * @param file the uploaded .xlsx file
     * @return summary with importedCount, errorCount, and per-row error details
     */
    ImportResultResponse importComponentsFromExcel(MultipartFile file);
}
