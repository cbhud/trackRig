package me.cbhud.trackRig.controller;

import me.cbhud.trackRig.dto.WorkstationRequest;
import me.cbhud.trackRig.dto.WorkstationResponse;
import me.cbhud.trackRig.dto.WorkstationUpdateRequest;
import me.cbhud.trackRig.dto.WorkstationUpdateStatusRequest;
import me.cbhud.trackRig.service.WorkstationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workstations")
public class WorkstationController {

    private final WorkstationService workstationService;

    public WorkstationController(WorkstationService workstationService) {
        this.workstationService = workstationService;
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

}
