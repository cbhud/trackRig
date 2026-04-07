package me.cbhud.TrackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.TrackRig.dto.ComponentRequest;
import me.cbhud.TrackRig.dto.ComponentResponse;
import me.cbhud.TrackRig.dto.ImportResultResponse;
import me.cbhud.TrackRig.service.ComponentService;
import me.cbhud.TrackRig.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/components")
public class ComponentController {

    private final ComponentService componentService;
    private final ImportService importService;

    public ComponentController(ComponentService componentService, ImportService importService) {
        this.componentService = componentService;
        this.importService = importService;
    }

    // ========================
    // CRUD ENDPOINTS
    // ========================

    // GET /api/components
    @GetMapping
    public ResponseEntity<List<ComponentResponse>> getAllComponents() {
        return ResponseEntity.ok(componentService.getAllComponents());
    }

    // GET /api/components/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ComponentResponse> getComponentById(@PathVariable Integer id) {
        return ResponseEntity.ok(componentService.getComponentById(id));
    }

    // POST /api/components — restricted to MANAGER or OWNER (enforced in service)
    @PostMapping
    public ResponseEntity<ComponentResponse> createComponent(
            @RequestBody @Valid ComponentRequest request) {
        ComponentResponse created = componentService.createComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/components/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ComponentResponse> updateComponent(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentRequest request) {
        return ResponseEntity.ok(componentService.updateComponent(id, request));
    }

    // DELETE /api/components/{id} — restricted to OWNER only (enforced in service)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Integer id) {
        componentService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // BUSINESS LOGIC ENDPOINTS
    // ========================

    // GET /api/components/storage — all components not assigned to a workstation
    @GetMapping("/storage")
    public ResponseEntity<List<ComponentResponse>> getComponentsInStorage() {
        return ResponseEntity.ok(componentService.getComponentsInStorage());
    }

    // PATCH /api/components/{id}/assign — assign component to a workstation
    // Body: { "workstationId": 5 }
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ComponentResponse> assignToWorkstation(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        Integer workstationId = body.get("workstationId");
        return ResponseEntity.ok(componentService.assignToWorkstation(id, workstationId));
    }

    // PATCH /api/components/{id}/storage — move component back to storage
    @PatchMapping("/{id}/storage")
    public ResponseEntity<ComponentResponse> moveToStorage(@PathVariable Integer id) {
        return ResponseEntity.ok(componentService.moveToStorage(id));
    }

    // ========================
    // IMPORT ENDPOINT
    // ========================

    /**
     * POST /api/components/import/excel
     * Content-Type: multipart/form-data
     * Param name: file
     *
     * Restricted to MANAGER or OWNER (enforced in ImportServiceImpl
     * via @PreAuthorize).
     *
     * Expected column layout in the .xlsx file (row 1 = header, ignored):
     * A: serialNumber
     * B: name
     * C: categoryName (resolved by name, case-insensitive)
     * D: statusName (resolved by name, case-insensitive)
     * E: workstationId (optional integer; leave blank for storage)
     * F: purchaseDate (optional; format: yyyy-MM-dd)
     * G: notes (optional)
     *
     * Returns 200 with ImportResultResponse:
     * { "importedCount": 8, "errorCount": 2, "errors": [...] }
     */
    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultResponse> importFromExcel(
            @RequestParam("file") MultipartFile file) {
        ImportResultResponse result = importService.importComponentsFromExcel(file);
        return ResponseEntity.ok(result);
    }
}
