package me.cbhud.TrackRig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Components", description = "CRUD, assignment, storage, and import operations for components")
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

    @Operation(summary = "Get all components")
    @ApiResponse(responseCode = "200", description = "List of all components")
    @GetMapping
    public ResponseEntity<List<ComponentResponse>> getAllComponents() {
        return ResponseEntity.ok(componentService.getAllComponents());
    }

    @Operation(summary = "Get component by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Component found"),
        @ApiResponse(responseCode = "404", description = "Component not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ComponentResponse> getComponentById(@PathVariable Integer id) {
        return ResponseEntity.ok(componentService.getComponentById(id));
    }

    @Operation(summary = "Create component", description = "Restricted to MANAGER or OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Component created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping
    public ResponseEntity<ComponentResponse> createComponent(
            @RequestBody @Valid ComponentRequest request) {
        ComponentResponse created = componentService.createComponent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update component", description = "Full update — all fields must be provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Component updated"),
        @ApiResponse(responseCode = "404", description = "Component not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ComponentResponse> updateComponent(
            @PathVariable Integer id,
            @RequestBody @Valid ComponentRequest request) {
        return ResponseEntity.ok(componentService.updateComponent(id, request));
    }

    @Operation(summary = "Delete component", description = "Restricted to OWNER only.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Component deleted"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Component not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComponent(@PathVariable Integer id) {
        componentService.deleteComponent(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // BUSINESS LOGIC ENDPOINTS
    // ========================

    @Operation(summary = "Get components in storage", description = "Returns all components not currently assigned to a workstation.")
    @ApiResponse(responseCode = "200", description = "List of components in storage")
    @GetMapping("/storage")
    public ResponseEntity<List<ComponentResponse>> getComponentsInStorage() {
        return ResponseEntity.ok(componentService.getComponentsInStorage());
    }

    @Operation(summary = "Assign component to workstation", description = "Body: { \"workstationId\": 5 }")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Component assigned"),
        @ApiResponse(responseCode = "404", description = "Component or workstation not found")
    })
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ComponentResponse> assignToWorkstation(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        Integer workstationId = body.get("workstationId");
        return ResponseEntity.ok(componentService.assignToWorkstation(id, workstationId));
    }

    @Operation(summary = "Move component to storage", description = "Unassigns the component from its workstation and moves it back to storage.")
    @ApiResponse(responseCode = "200", description = "Component moved to storage")
    @PatchMapping("/{id}/storage")
    public ResponseEntity<ComponentResponse> moveToStorage(@PathVariable Integer id) {
        return ResponseEntity.ok(componentService.moveToStorage(id));
    }

    // ========================
    // IMPORT ENDPOINT
    // ========================

    @Operation(
        summary = "Import components from Excel",
        description = """
            Upload a .xlsx file to bulk-import components. Restricted to MANAGER or OWNER.

            Expected column layout (row 1 = header, ignored):
            - A: serialNumber
            - B: name
            - C: categoryName (case-insensitive)
            - D: statusName (case-insensitive)
            - E: workstationId (optional integer; blank = storage)
            - F: purchaseDate (optional; format: yyyy-MM-dd)
            - G: notes (optional)
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Import completed — check importedCount and errors in response"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping(value = "/import/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultResponse> importFromExcel(
            @Parameter(description = "Excel (.xlsx) file to import") @RequestParam("file") MultipartFile file) {
        ImportResultResponse result = importService.importComponentsFromExcel(file);
        return ResponseEntity.ok(result);
    }
}
