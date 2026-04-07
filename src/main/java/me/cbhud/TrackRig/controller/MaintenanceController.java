package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.cbhud.TrackRig.dto.MaintenanceLogRequest;
import me.cbhud.TrackRig.dto.MaintenanceLogResponse;
import me.cbhud.TrackRig.dto.MaintenanceStatusResponse;
import me.cbhud.TrackRig.service.MaintenanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Maintenance", description = "Log maintenance actions and query maintenance history and overdue items")
@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Operation(summary = "Log a maintenance action", description = "Records a maintenance event for a workstation. performed_by is set from the JWT token; performed_at is set to the current timestamp.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Maintenance log created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Workstation or maintenance type not found")
    })
    @PostMapping("/log")
    public ResponseEntity<MaintenanceLogResponse> logMaintenance(
            @RequestBody @Valid MaintenanceLogRequest request) {
        MaintenanceLogResponse created = maintenanceService.logMaintenance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get maintenance logs for a workstation", description = "Returns full maintenance history for the given workstation.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Maintenance log list"),
        @ApiResponse(responseCode = "404", description = "Workstation not found")
    })
    @GetMapping("/logs/workstation/{workstationId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getLogsByWorkstation(
            @PathVariable Integer workstationId) {
        return ResponseEntity.ok(maintenanceService.getLogsByWorkstationId(workstationId));
    }

    @Operation(summary = "Get all overdue and due-soon maintenance items", description = "Returns OVERDUE and DUE_SOON items across all workstations, sourced from the view_maintenance_status DB view.")
    @ApiResponse(responseCode = "200", description = "List of overdue/due-soon maintenance statuses")
    @GetMapping("/overdue")
    public ResponseEntity<List<MaintenanceStatusResponse>> getAllOverdue() {
        return ResponseEntity.ok(maintenanceService.getAllOverdue());
    }
}
