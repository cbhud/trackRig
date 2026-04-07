package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.cbhud.TrackRig.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles Excel and PDF export requests for workstations and components.
 *
 * All endpoints are accessible to any authenticated user (EMPLOYEE, MANAGER,
 * OWNER)
 * since exporting is a READ operation.
 */
@Tag(name = "Export", description = "Export workstations and components to Excel or PDF")
@RestController
public class ExportController {

    private static final MediaType EXCEL_MEDIA_TYPE = MediaType
            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    // ========================
    // WORKSTATION EXPORTS
    // ========================

    @Operation(summary = "Export workstations to Excel", description = "Downloads a .xlsx file. Optionally include a Components sheet and/or Maintenance Logs sheet.")
    @ApiResponse(responseCode = "200", description = "Excel file download")
    @GetMapping("/api/workstations/export/excel")
    public ResponseEntity<byte[]> exportWorkstationsExcel(
            @Parameter(description = "Include a Components sheet") @RequestParam(defaultValue = "false") boolean includeComponents,
            @Parameter(description = "Include a Maintenance Logs sheet") @RequestParam(defaultValue = "false") boolean includeLogs) {

        byte[] bytes = exportService.exportWorkstationsToExcel(includeComponents, includeLogs);
        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"workstations_export.xlsx\"")
                .body(bytes);
    }

    @Operation(summary = "Export workstations to PDF", description = "Downloads a .pdf file. Optionally include a components section and/or maintenance logs section.")
    @ApiResponse(responseCode = "200", description = "PDF file download")
    @GetMapping("/api/workstations/export/pdf")
    public ResponseEntity<byte[]> exportWorkstationsPdf(
            @Parameter(description = "Include a components section") @RequestParam(defaultValue = "false") boolean includeComponents,
            @Parameter(description = "Include a maintenance logs section") @RequestParam(defaultValue = "false") boolean includeLogs) {

        byte[] bytes = exportService.exportWorkstationsToPdf(includeComponents, includeLogs);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"workstations_export.pdf\"")
                .body(bytes);
    }

    // ========================
    // COMPONENT EXPORTS
    // ========================

    @Operation(summary = "Export components to Excel", description = "Downloads a .xlsx file. All filter params are optional.")
    @ApiResponse(responseCode = "200", description = "Excel file download")
    @GetMapping("/api/components/export/excel")
    public ResponseEntity<byte[]> exportComponentsExcel(
            @Parameter(description = "Filter by component status ID") @RequestParam(required = false) Integer statusId,
            @Parameter(description = "Filter by component category ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "true = storage only, false = assigned only, omit = all") @RequestParam(required = false) Boolean inStorage) {

        byte[] bytes = exportService.exportComponentsToExcel(statusId, categoryId, inStorage);
        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"components_export.xlsx\"")
                .body(bytes);
    }

    @Operation(summary = "Export components to PDF", description = "Downloads a .pdf file. Same filter params as the Excel variant.")
    @ApiResponse(responseCode = "200", description = "PDF file download")
    @GetMapping("/api/components/export/pdf")
    public ResponseEntity<byte[]> exportComponentsPdf(
            @Parameter(description = "Filter by component status ID") @RequestParam(required = false) Integer statusId,
            @Parameter(description = "Filter by component category ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "true = storage only, false = assigned only, omit = all") @RequestParam(required = false) Boolean inStorage) {

        byte[] bytes = exportService.exportComponentsToPdf(statusId, categoryId, inStorage);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"components_export.pdf\"")
                .body(bytes);
    }
}
