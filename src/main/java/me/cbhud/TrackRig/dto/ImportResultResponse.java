package me.cbhud.TrackRig.dto;

import lombok.Data;

import java.util.List;

/**
 * Top-level response returned by the component import endpoint.
 * Supports partial success: valid rows are saved even when some rows fail.
 *
 * Example:
 * {
 * "importedCount": 8,
 * "errorCount": 2,
 * "errors": [
 * { "row": 3, "field": "serialNumber", "message": "Duplicate serial number in
 * file" },
 * { "row": 7, "field": "categoryName", "message": "Category 'GPU' not found" }
 * ]
 * }
 */
@Data
public class ImportResultResponse {

    private int importedCount;
    private int errorCount;
    private List<ImportErrorResponse> errors;

    public ImportResultResponse(int importedCount, List<ImportErrorResponse> errors) {
        this.importedCount = importedCount;
        this.errors = errors;
        this.errorCount = errors.size();
    }
}
