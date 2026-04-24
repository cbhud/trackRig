package me.cbhud.trackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.trackRig.dto.request.*;
import me.cbhud.trackRig.dto.response.ComponentCategoryResponse;
import me.cbhud.trackRig.dto.response.ComponentResponse;
import me.cbhud.trackRig.dto.response.ComponentStatusResponse;
import me.cbhud.trackRig.service.ComponentCategoryService;
import me.cbhud.trackRig.service.ComponentService;
import me.cbhud.trackRig.service.ComponentStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/components")
public class ComponentController {

    private final ComponentCategoryService componentCategoryService;
    private final ComponentStatusService componentStatusService;
    private final ComponentService componentService;

    public ComponentController(
            ComponentCategoryService componentCategoryService,
            ComponentStatusService componentStatusService,
            ComponentService componentService
    ) {
        this.componentCategoryService = componentCategoryService;
        this.componentStatusService = componentStatusService;
        this.componentService = componentService;
    }

    // ─── Component CRUD ────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<ComponentResponse>> getAllComponents() {
        return ResponseEntity.ok(componentService.getAllComponents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComponentResponse> getComponentById(@PathVariable Integer id) {
        return ResponseEntity.ok(componentService.getComponentById(id));
    }

    @PostMapping
    public ResponseEntity<ComponentResponse> createComponent(@RequestBody @Valid ComponentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(componentService.createComponent(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ComponentResponse> updateComponent(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentUpdateRequest request) {
        return ResponseEntity.ok(componentService.updateComponent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Integer id) {
        componentService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }

    // ─── Assignment endpoints ──────────────────────────────────────────────────

    @PatchMapping("/{id}/workstation")
    public ResponseEntity<ComponentResponse> assignWorkstation(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentAssignWorkstationRequest request) {
        return ResponseEntity.ok(componentService.assignWorkstation(id, request));
    }

    @PatchMapping("/{id}/category")
    public ResponseEntity<ComponentResponse> assignCategory(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentAssignCategoryRequest request) {
        return ResponseEntity.ok(componentService.assignCategory(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ComponentResponse> assignStatus(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentAssignStatusRequest request) {
        return ResponseEntity.ok(componentService.assignStatus(id, request));
    }

    // ─── Category CRUD ─────────────────────────────────────────────────────────

    @GetMapping("/categories")
    public ResponseEntity<List<ComponentCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(componentCategoryService.getAllComponentCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ComponentCategoryResponse> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(componentCategoryService.getComponentCategoryById(id));
    }

    @PostMapping("/categories")
    public ResponseEntity<ComponentCategoryResponse> createCategory(
            @RequestBody @Valid ComponentCategoryRequest componentCategoryRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(componentCategoryService.createComponentCategory(componentCategoryRequest));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteComponentCategory(@PathVariable Integer id) {
        componentCategoryService.deleteComponentCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/categories/{id}")
    public ResponseEntity<ComponentCategoryResponse> updateComponentCategory(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentCategoryUpdateRequest componentCategoryUpdateRequest) {
        return ResponseEntity.ok(componentCategoryService.updateComponentCategory(id, componentCategoryUpdateRequest));
    }

    // ─── Status CRUD ───────────────────────────────────────────────────────────

    @GetMapping("/status")
    public ResponseEntity<List<ComponentStatusResponse>> getAllComponentStatuses() {
        return ResponseEntity.ok(componentStatusService.getAllComponentStatuses());
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<ComponentStatusResponse> getStatusById(@PathVariable Integer id) {
        return ResponseEntity.ok(componentStatusService.getComponentStatusById(id));
    }

    @PostMapping("/status")
    public ResponseEntity<ComponentStatusResponse> createComponentStatus(
            @RequestBody @Valid ComponentStatusRequest componentStatusRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(componentStatusService.createComponentStatus(componentStatusRequest));
    }

    @DeleteMapping("/status/{id}")
    public ResponseEntity<Void> deleteComponentStatus(@PathVariable Integer id) {
        componentStatusService.deleteComponentStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/status/{id}")
    public ResponseEntity<ComponentStatusResponse> updateComponentStatus(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentStatusRequest componentStatusRequest) {
        return ResponseEntity.ok(componentStatusService.updateComponentStatus(id, componentStatusRequest));
    }
}
