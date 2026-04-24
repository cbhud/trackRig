package me.cbhud.trackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.trackRig.dto.request.MaintenanceLogRequest;
import me.cbhud.trackRig.dto.request.MaintenanceTypeRequest;
import me.cbhud.trackRig.dto.request.MaintenanceTypeUpdateRequest;
import me.cbhud.trackRig.dto.response.MaintenanceLogResponse;
import me.cbhud.trackRig.dto.response.MaintenanceStatusResponse;
import me.cbhud.trackRig.dto.response.MaintenanceTypeResponse;
import me.cbhud.trackRig.security.SecurityUser;
import me.cbhud.trackRig.service.MaintenanceLogService;
import me.cbhud.trackRig.service.MaintenanceTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceTypeService typeService;
    private final MaintenanceLogService logService;

    public MaintenanceController(MaintenanceTypeService typeService, MaintenanceLogService logService) {
        this.typeService = typeService;
        this.logService = logService;
    }

    // ─── Maintenance Types ────────────────────────────────────────────────────

    @GetMapping("/types")
    public ResponseEntity<List<MaintenanceTypeResponse>> getAllTypes() {
        return ResponseEntity.ok(typeService.getAllMaintenanceTypes());
    }

    @GetMapping("/types/active")
    public ResponseEntity<List<MaintenanceTypeResponse>> getActiveTypes() {
        return ResponseEntity.ok(typeService.getActiveMaintenanceTypes());
    }

    @GetMapping("/types/{id}")
    public ResponseEntity<MaintenanceTypeResponse> getTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(typeService.getMaintenanceTypeById(id));
    }

    @PostMapping("/types")
    public ResponseEntity<MaintenanceTypeResponse> createType(
            @RequestBody @Valid MaintenanceTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeService.createMaintenanceType(request));
    }

    @PatchMapping("/types/{id}")
    public ResponseEntity<MaintenanceTypeResponse> updateType(
            @PathVariable Integer id,
            @RequestBody @Valid MaintenanceTypeUpdateRequest request) {
        return ResponseEntity.ok(typeService.updateMaintenanceType(id, request));
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Integer id) {
        typeService.deleteMaintenanceType(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Maintenance Logs ─────────────────────────────────────────────────────

    @GetMapping("/logs")
    public ResponseEntity<List<MaintenanceLogResponse>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<MaintenanceLogResponse> getLogById(@PathVariable Integer id) {
        return ResponseEntity.ok(logService.getLogById(id));
    }

    @GetMapping("/logs/workstation/{workstationId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getLogsByWorkstation(@PathVariable Integer workstationId) {
        return ResponseEntity.ok(logService.getLogsByWorkstation(workstationId));
    }

    @GetMapping("/logs/type/{typeId}")
    public ResponseEntity<List<MaintenanceLogResponse>> getLogsByType(@PathVariable Integer typeId) {
        return ResponseEntity.ok(logService.getLogsByType(typeId));
    }

    @PostMapping("/logs")
    public ResponseEntity<MaintenanceLogResponse> createLog(
            @RequestBody @Valid MaintenanceLogRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(logService.createLog(request, user.getUsername()));
    }

    @DeleteMapping("/logs/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Integer id) {
        logService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Maintenance Status (view) ────────────────────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<List<MaintenanceStatusResponse>> getMaintenanceStatus() {
        return ResponseEntity.ok(logService.getMaintenanceStatus());
    }

    @GetMapping("/status/workstation/{workstationId}")
    public ResponseEntity<List<MaintenanceStatusResponse>> getStatusByWorkstation(@PathVariable Integer workstationId) {
        return ResponseEntity.ok(logService.getMaintenanceStatusByWorkstation(workstationId));
    }

    @GetMapping("/status/overdue")
    public ResponseEntity<List<MaintenanceStatusResponse>> getOverdue() {
        return ResponseEntity.ok(logService.getOverdueMaintenances());
    }
}
