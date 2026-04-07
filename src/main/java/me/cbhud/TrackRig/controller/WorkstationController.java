package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.cbhud.TrackRig.dto.ComponentResponse;
import me.cbhud.TrackRig.dto.MaintenanceStatusResponse;
import me.cbhud.TrackRig.dto.WorkstationRequest;
import me.cbhud.TrackRig.dto.WorkstationResponse;
import me.cbhud.TrackRig.service.WorkstationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Workstations", description = "CRUD and business operations for workstations")
@RestController
@RequestMapping("/api/workstations")
public class WorkstationController {

    private final WorkstationService workstationService;

    public WorkstationController(WorkstationService workstationService) {
        this.workstationService = workstationService;
    }

    @Operation(summary = "Get all workstations", description = "Returns all workstations. Optionally filter by statusId (e.g. statusId=3 for Out of Order).")
    @ApiResponse(responseCode = "200", description = "List of workstations")
    @GetMapping
    public ResponseEntity<List<WorkstationResponse>> getAllWorkstations(
            @Parameter(description = "Filter by workstation status ID") @RequestParam(required = false) Integer statusId) {
        List<WorkstationResponse> workstations;

        if (statusId != null) {
            workstations = workstationService.getWorkstationsByStatusId(statusId);
        } else {
            workstations = workstationService.getAllWorkstations();
        }

        return ResponseEntity.ok(workstations);
    }

    @Operation(summary = "Get workstation by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Workstation found"),
        @ApiResponse(responseCode = "404", description = "Workstation not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<WorkstationResponse> getWorkstationById(@PathVariable Integer id) {
        return ResponseEntity.ok(workstationService.getWorkstationById(id));
    }

    @Operation(summary = "Create workstation", description = "Body: { \"name\": \"Station-D1\", \"statusId\": 1, \"gridX\": 0, \"gridY\": 3 }")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Workstation created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Duplicate workstation name")
    })
    @PostMapping
    public ResponseEntity<WorkstationResponse> createWorkstation(
            @RequestBody @Valid WorkstationRequest request) {
        WorkstationResponse created = workstationService.createWorkstation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update workstation", description = "Full update — all fields must be provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Workstation updated"),
        @ApiResponse(responseCode = "404", description = "Workstation not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<WorkstationResponse> updateWorkstation(
            @PathVariable Integer id,
            @RequestBody @Valid WorkstationRequest request) {
        return ResponseEntity.ok(workstationService.updateWorkstation(id, request));
    }

    @Operation(summary = "Delete workstation", description = "Restricted to MANAGER or OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Workstation deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Workstation not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkstation(@PathVariable Integer id) {
        workstationService.deleteWorkstation(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // BUSINESS LOGIC ENDPOINTS
    // ========================

    @Operation(summary = "Update workstation status", description = "Partial update — only changes the status. Body: { \"statusId\": 2 }")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status updated"),
        @ApiResponse(responseCode = "404", description = "Workstation not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkstationResponse> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        return ResponseEntity.ok(workstationService.updateStatus(id, statusId));
    }

    @Operation(summary = "Update workstation grid position", description = "Partial update for floor map drag-and-drop. Body: { \"gridX\": 2, \"gridY\": 1 }")
    @ApiResponse(responseCode = "200", description = "Position updated")
    @PatchMapping("/{id}/position")
    public ResponseEntity<WorkstationResponse> updateGridPosition(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        int gridX = body.get("gridX");
        int gridY = body.get("gridY");
        return ResponseEntity.ok(workstationService.updateGridPosition(id, gridX, gridY));
    }

    @Operation(summary = "Get components for a workstation", description = "Returns all components installed in this workstation.")
    @ApiResponse(responseCode = "200", description = "List of components")
    @GetMapping("/{id}/components")
    public ResponseEntity<List<ComponentResponse>> getComponents(@PathVariable Integer id) {
        return ResponseEntity.ok(workstationService.getComponentsByWorkstationId(id));
    }

    @Operation(summary = "Get maintenance status for a workstation", description = "Returns computed maintenance status for each active maintenance type. Status values: OK, DUE_SOON, OVERDUE, NEVER_DONE.")
    @ApiResponse(responseCode = "200", description = "Maintenance status list")
    @GetMapping("/{id}/maintenance-status")
    public ResponseEntity<List<MaintenanceStatusResponse>> getMaintenanceStatus(
            @PathVariable Integer id) {
        return ResponseEntity.ok(workstationService.getMaintenanceStatus(id));
    }
}
