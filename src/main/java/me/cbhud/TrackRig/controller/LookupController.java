package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.cbhud.TrackRig.model.ComponentCategory;
import me.cbhud.TrackRig.model.ComponentStatus;
import me.cbhud.TrackRig.model.MaintenanceType;
import me.cbhud.TrackRig.model.WorkstationStatus;
import me.cbhud.TrackRig.service.LookupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lookup", description = "Reference data for categories, statuses, and maintenance types")
@RestController
@RequestMapping("/api/lookup")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @Operation(summary = "Get all component categories")
    @ApiResponse(responseCode = "200", description = "List of component categories")
    @GetMapping("/component-categories")
    public ResponseEntity<List<ComponentCategory>> getAllComponentCategories() {
        return ResponseEntity.ok(lookupService.getAllComponentCategories());
    }

    @Operation(summary = "Create component category", description = "Restricted to OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Category created"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/component-categories")
    public ResponseEntity<ComponentCategory> createComponentCategory(
            @RequestBody @Valid ComponentCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lookupService.createComponentCategory(category));
    }

    @Operation(summary = "Update component category")
    @ApiResponse(responseCode = "200", description = "Category updated")
    @PutMapping("/component-categories/{id}")
    public ResponseEntity<ComponentCategory> updateComponentCategory(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentCategory category) {
        return ResponseEntity.ok(lookupService.updateComponentCategory(id, category));
    }

    @Operation(summary = "Delete component category")
    @ApiResponse(responseCode = "204", description = "Category deleted")
    @DeleteMapping("/component-categories/{id}")
    public ResponseEntity<Void> deleteComponentCategory(@PathVariable Integer id) {
        lookupService.deleteComponentCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // COMPONENT STATUSES
    // ========================

    @Operation(summary = "Get all component statuses")
    @ApiResponse(responseCode = "200", description = "List of component statuses")
    @GetMapping("/component-statuses")
    public ResponseEntity<List<ComponentStatus>> getAllComponentStatuses() {
        return ResponseEntity.ok(lookupService.getAllComponentStatuses());
    }

    @Operation(summary = "Create component status")
    @ApiResponse(responseCode = "201", description = "Status created")
    @PostMapping("/component-statuses")
    public ResponseEntity<ComponentStatus> createComponentStatus(
            @RequestBody @Valid ComponentStatus status) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lookupService.createComponentStatus(status));
    }

    @Operation(summary = "Update component status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PutMapping("/component-statuses/{id}")
    public ResponseEntity<ComponentStatus> updateComponentStatus(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentStatus status) {
        return ResponseEntity.ok(lookupService.updateComponentStatus(id, status));
    }

    @Operation(summary = "Delete component status")
    @ApiResponse(responseCode = "204", description = "Status deleted")
    @DeleteMapping("/component-statuses/{id}")
    public ResponseEntity<Void> deleteComponentStatus(@PathVariable Integer id) {
        lookupService.deleteComponentStatus(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // WORKSTATION STATUSES
    // ========================

    @Operation(summary = "Get all workstation statuses")
    @ApiResponse(responseCode = "200", description = "List of workstation statuses")
    @GetMapping("/workstation-statuses")
    public ResponseEntity<List<WorkstationStatus>> getAllWorkstationStatuses() {
        return ResponseEntity.ok(lookupService.getAllWorkstationStatuses());
    }

    @Operation(summary = "Create workstation status")
    @ApiResponse(responseCode = "201", description = "Status created")
    @PostMapping("/workstation-statuses")
    public ResponseEntity<WorkstationStatus> createWorkstationStatus(
            @RequestBody @Valid WorkstationStatus status) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lookupService.createWorkstationStatus(status));
    }

    @Operation(summary = "Update workstation status")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @PutMapping("/workstation-statuses/{id}")
    public ResponseEntity<WorkstationStatus> updateWorkstationStatus(
            @PathVariable Integer id,
            @RequestBody @Valid WorkstationStatus status) {
        return ResponseEntity.ok(lookupService.updateWorkstationStatus(id, status));
    }

    @Operation(summary = "Delete workstation status")
    @ApiResponse(responseCode = "204", description = "Status deleted")
    @DeleteMapping("/workstation-statuses/{id}")
    public ResponseEntity<Void> deleteWorkstationStatus(@PathVariable Integer id) {
        lookupService.deleteWorkstationStatus(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // MAINTENANCE TYPES
    // ========================

    @Operation(summary = "Get all maintenance types")
    @ApiResponse(responseCode = "200", description = "List of maintenance types")
    @GetMapping("/maintenance-types")
    public ResponseEntity<List<MaintenanceType>> getAllMaintenanceTypes() {
        return ResponseEntity.ok(lookupService.getAllMaintenanceTypes());
    }

    @Operation(summary = "Get active maintenance types", description = "Returns only maintenance types with active=true.")
    @ApiResponse(responseCode = "200", description = "List of active maintenance types")
    @GetMapping("/maintenance-types/active")
    public ResponseEntity<List<MaintenanceType>> getActiveMaintenanceTypes() {
        return ResponseEntity.ok(lookupService.getActiveMaintenanceTypes());
    }

    @Operation(summary = "Create maintenance type")
    @ApiResponse(responseCode = "201", description = "Maintenance type created")
    @PostMapping("/maintenance-types")
    public ResponseEntity<MaintenanceType> createMaintenanceType(
            @RequestBody @Valid MaintenanceType type) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lookupService.createMaintenanceType(type));
    }

    @Operation(summary = "Update maintenance type")
    @ApiResponse(responseCode = "200", description = "Maintenance type updated")
    @PutMapping("/maintenance-types/{id}")
    public ResponseEntity<MaintenanceType> updateMaintenanceType(
            @PathVariable Integer id,
            @RequestBody @Valid MaintenanceType type) {
        return ResponseEntity.ok(lookupService.updateMaintenanceType(id, type));
    }

    @Operation(summary = "Delete maintenance type")
    @ApiResponse(responseCode = "204", description = "Maintenance type deleted")
    @DeleteMapping("/maintenance-types/{id}")
    public ResponseEntity<Void> deleteMaintenanceType(@PathVariable Integer id) {
        lookupService.deleteMaintenanceType(id);
        return ResponseEntity.noContent().build();
    }
}
