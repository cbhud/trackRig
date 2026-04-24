package me.cbhud.trackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.trackRig.dto.*;
import me.cbhud.trackRig.service.WorkstationStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workstations/status")
public class WorkstationStatusController {

    private final WorkstationStatusService workstationStatusService;

    public WorkstationStatusController(WorkstationStatusService workstationStatusService){
        this.workstationStatusService = workstationStatusService;
    }

    @GetMapping()
    public ResponseEntity<List<WorkstationStatusResponse>> getAllWorkstationStatuses(){
        return ResponseEntity.ok(workstationStatusService.getAllWorkstationStatuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkstationStatusResponse> getWorkstationStatusById(@PathVariable Integer id){
        return ResponseEntity.ok(workstationStatusService.getWorkstationStatusById(id));
    }

    @PostMapping
    public ResponseEntity<WorkstationStatusResponse> createWorkstationStatus(@RequestBody @Valid WorkstationStatusRequest workstation){
        return ResponseEntity.status(HttpStatus.CREATED).body(workstationStatusService.createWorkstationStatus(workstation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkstationStatus(@PathVariable Integer id){
        workstationStatusService.deleteWorkstationStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WorkstationStatusResponse> updateWorkstationStatus(@PathVariable Integer id, @RequestBody @Valid WorkstationStatusUpdateRequest workstationStatusUpdateRequest){
        workstationStatusService.updateWorkstationStatus(id, workstationStatusUpdateRequest);
        return ResponseEntity.ok(workstationStatusService.getWorkstationStatusById(id));
    }



}
