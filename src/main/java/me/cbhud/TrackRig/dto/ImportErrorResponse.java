package me.cbhud.TrackRig.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a single row-level validation failure during an Excel import.
 * Returned as part of ImportResultResponse.errors so the client knows
 * exactly which rows failed and why.
 */
@Data
@AllArgsConstructor
public class ImportErrorResponse {

    /**
     * 1-based row number in the Excel file (row 1 = header, so data starts at row
     * 2).
     */
    private int row;

    /**
     * The column/field that failed validation (e.g. "serialNumber",
     * "categoryName").
     */
    private String field;

    /** Human-readable reason for the failure. */
    private String message;
}
