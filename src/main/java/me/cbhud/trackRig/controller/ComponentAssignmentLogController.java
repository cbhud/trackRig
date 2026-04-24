package me.cbhud.trackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.trackRig.dto.request.ComponentAssignmentLogRequest;
import me.cbhud.trackRig.dto.response.ComponentAssignmentLogResponse;
import me.cbhud.trackRig.security.SecurityUser;
import me.cbhud.trackRig.service.ComponentAssignmentLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/component-assignments")
public class ComponentAssignmentLogController {

    private final ComponentAssignmentLogService service;

    public ComponentAssignmentLogController(ComponentAssignmentLogService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ComponentAssignmentLogResponse>> getAllLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }

    @GetMapping("/component/{componentId}")
    public ResponseEntity<List<ComponentAssignmentLogResponse>> getLogsByComponent(@PathVariable Integer componentId) {
        return ResponseEntity.ok(service.getLogsByComponent(componentId));
    }

    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<ComponentAssignmentLogResponse>> getLogsByWorkstation(@PathVariable Integer workstationId) {
        return ResponseEntity.ok(service.getLogsByWorkstation(workstationId));
    }

    @GetMapping("/component/{componentId}/active")
    public ResponseEntity<ComponentAssignmentLogResponse> getActiveAssignment(@PathVariable Integer componentId) {
        return ResponseEntity.ok(service.getActiveAssignment(componentId));
    }

    @PostMapping
    public ResponseEntity<ComponentAssignmentLogResponse> createAssignment(
            @RequestBody @Valid ComponentAssignmentLogRequest request,
            @AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createAssignment(request, user.getUsername()));
    }

    /**
     * Close active assignment for a component (move to storage).
     * Optional notes in request param.
     */
    @PatchMapping("/component/{componentId}/close")
    public ResponseEntity<ComponentAssignmentLogResponse> closeAssignment(
            @PathVariable Integer componentId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(service.closeAssignment(componentId, notes));
    }
}
