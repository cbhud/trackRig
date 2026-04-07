package me.cbhud.TrackRig.controller;

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

    /**
     * GET /api/workstations/export/excel
     *
     * Query params:
     * includeComponents (default: false) — add a Components sheet
     * includeLogs (default: false) — add a Maintenance Logs sheet
     *
     * Example:
     * GET /api/workstations/export/excel?includeComponents=true&includeLogs=true
     */
    @GetMapping("/api/workstations/export/excel")
    public ResponseEntity<byte[]> exportWorkstationsExcel(
            @RequestParam(defaultValue = "false") boolean includeComponents,
            @RequestParam(defaultValue = "false") boolean includeLogs) {

        byte[] bytes = exportService.exportWorkstationsToExcel(includeComponents, includeLogs);
        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"workstations_export.xlsx\"")
                .body(bytes);
    }

    /**
     * GET /api/workstations/export/pdf
     *
     * Query params:
     * includeComponents (default: false) — add a components section
     * includeLogs (default: false) — add a maintenance logs section
     *
     * Example:
     * GET /api/workstations/export/pdf?includeComponents=true&includeLogs=true
     */
    @GetMapping("/api/workstations/export/pdf")
    public ResponseEntity<byte[]> exportWorkstationsPdf(
            @RequestParam(defaultValue = "false") boolean includeComponents,
            @RequestParam(defaultValue = "false") boolean includeLogs) {

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

    /**
     * GET /api/components/export/excel
     *
     * All filter params are optional — omit any to include all values for that
     * field.
     *
     * Query params:
     * statusId — filter by component_status.id
     * categoryId — filter by component_category.id
     * inStorage — true = storage only | false = assigned only | omit = all
     *
     * Examples:
     * GET /api/components/export/excel → all components
     * GET /api/components/export/excel?statusId=1 → status filter only
     * GET /api/components/export/excel?categoryId=2&inStorage=true → category +
     * storage only
     */
    @GetMapping("/api/components/export/excel")
    public ResponseEntity<byte[]> exportComponentsExcel(
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean inStorage) {

        byte[] bytes = exportService.exportComponentsToExcel(statusId, categoryId, inStorage);
        return ResponseEntity.ok()
                .contentType(EXCEL_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"components_export.xlsx\"")
                .body(bytes);
    }

    /**
     * GET /api/components/export/pdf
     *
     * Same filter params as the Excel variant.
     *
     * Examples:
     * GET /api/components/export/pdf → all components
     * GET /api/components/export/pdf?statusId=2 → damaged components only
     * GET /api/components/export/pdf?inStorage=false → assigned components only
     */
    @GetMapping("/api/components/export/pdf")
    public ResponseEntity<byte[]> exportComponentsPdf(
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean inStorage) {

        byte[] bytes = exportService.exportComponentsToPdf(statusId, categoryId, inStorage);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"components_export.pdf\"")
                .body(bytes);
    }
}
