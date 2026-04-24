package me.cbhud.trackRig.controller;

import me.cbhud.trackRig.dto.request.*;
import me.cbhud.trackRig.dto.response.WorkstationResponse;
import me.cbhud.trackRig.dto.response.WorkstationStatusResponse;
import me.cbhud.trackRig.service.WorkstationService;
import jakarta.validation.Valid;
import me.cbhud.trackRig.service.WorkstationStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workstations")
public class WorkstationController {

    private final WorkstationService workstationService;
    private final WorkstationStatusService workstationStatusService;

    public WorkstationController(WorkstationService workstationService,
            WorkstationStatusService workstationStatusService) {
        this.workstationService = workstationService;
        this.workstationStatusService = workstationStatusService;
    }

    @GetMapping()
    public ResponseEntity<List<WorkstationResponse>> getAllWorkstations() {
        return ResponseEntity.ok(workstationService.getAllWorkstations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkstationResponse> getWorkstationById(@PathVariable Integer id) {
        return ResponseEntity.ok(workstationService.getWorkstationById(id));
    }

    @PostMapping()
    public ResponseEntity<WorkstationResponse> createWorkstation(@RequestBody @Valid WorkstationRequest workstation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workstationService.createWorkstation(workstation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkstation(@PathVariable Integer id) {
        workstationService.deleteWorkstation(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkstationResponse> updateWorkstation(@PathVariable Integer id,
            @RequestBody @Valid WorkstationUpdateRequest workstation) {
        return ResponseEntity.ok(workstationService.updateWorkstation(id, workstation));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkstationResponse> updateWorkstationStatus(@PathVariable Integer id,
            @RequestBody @Valid WorkstationUpdateStatusRequest workstation) {
        return ResponseEntity.ok(workstationService.updateWorkstationStatus(id, workstation));
    }

    // Status controllers

    @GetMapping("/status")
    public ResponseEntity<List<WorkstationStatusResponse>> getAllWorkstationStatuses() {
        return ResponseEntity.ok(workstationStatusService.getAllWorkstationStatuses());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<WorkstationStatusResponse> getWorkstationStatusById(@PathVariable Integer id) {
        return ResponseEntity.ok(workstationStatusService.getWorkstationStatusById(id));
    }

    @PostMapping("/status")
    public ResponseEntity<WorkstationStatusResponse> createWorkstationStatus(
            @RequestBody @Valid WorkstationStatusRequest workstation) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workstationStatusService.createWorkstationStatus(workstation));
    }

    @DeleteMapping("/status/{id}")
    public ResponseEntity<Void> deleteWorkstationStatus(@PathVariable Integer id) {
        workstationStatusService.deleteWorkstationStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<WorkstationStatusResponse> updateWorkstationStatus(@PathVariable Integer id,
            @RequestBody @Valid WorkstationStatusUpdateRequest workstationStatusUpdateRequest) {
        return ResponseEntity.ok(workstationStatusService.updateWorkstationStatus(id, workstationStatusUpdateRequest));
    }

}
