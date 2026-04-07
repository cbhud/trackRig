package me.cbhud.TrackRig.service;

import me.cbhud.TrackRig.dto.ComponentResponse;
import me.cbhud.TrackRig.dto.MaintenanceStatusResponse;
import me.cbhud.TrackRig.dto.WorkstationRequest;
import me.cbhud.TrackRig.dto.WorkstationResponse;
import me.cbhud.TrackRig.exception.DuplicateResourceException;
import me.cbhud.TrackRig.exception.ResourceNotFoundException;
import me.cbhud.TrackRig.model.Workstation;
import me.cbhud.TrackRig.model.WorkstationStatus;
import me.cbhud.TrackRig.repository.ComponentRepository;
import me.cbhud.TrackRig.repository.MaintenanceLogRepository;
import me.cbhud.TrackRig.repository.WorkstationRepository;
import me.cbhud.TrackRig.repository.WorkstationStatusRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkstationServiceImpl implements WorkstationService {

    private final WorkstationRepository workstationRepository;
    private final WorkstationStatusRepository workstationStatusRepository;
    private final ComponentRepository componentRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;

    public WorkstationServiceImpl(
            WorkstationRepository workstationRepository,
            WorkstationStatusRepository workstationStatusRepository,
            ComponentRepository componentRepository,
            MaintenanceLogRepository maintenanceLogRepository) {
        this.workstationRepository = workstationRepository;
        this.workstationStatusRepository = workstationStatusRepository;
        this.componentRepository = componentRepository;
        this.maintenanceLogRepository = maintenanceLogRepository;
    }

    // ========================
    // CRUD METHODS
    // ========================

    @Override
    public List<WorkstationResponse> getAllWorkstations() {
        return workstationRepository.findAll()
                .stream()
                .map(WorkstationResponse::from)
                .toList();
    }

    @Override
    public WorkstationResponse getWorkstationById(Integer id) {
        Workstation workstation = findWorkstationOrThrow(id);
        return WorkstationResponse.from(workstation);
    }

    @Override
    @Transactional
    public WorkstationResponse createWorkstation(WorkstationRequest request) {
        if (workstationRepository.existsByGridXAndGridY(request.getGridX(), request.getGridY())) {
            throw new DuplicateResourceException("A workstation already exists at this grid position.");
        }

        WorkstationStatus status = findStatusOrThrow(request.getStatusId());

        Workstation workstation = new Workstation();
        workstation.setName(request.getName());
        workstation.setWorkstationStatus(status);
        workstation.setGridX(request.getGridX());
        workstation.setGridY(request.getGridY());

        Workstation saved = workstationRepository.save(workstation);
        return WorkstationResponse.from(saved);
    }

    @Override
    @Transactional
    public WorkstationResponse updateWorkstation(Integer id, WorkstationRequest request) {
        if (workstationRepository.existsByGridXAndGridYAndIdNot(request.getGridX(), request.getGridY(), id)) {
            throw new DuplicateResourceException("A workstation already exists at this grid position.");
        }

        Workstation workstation = findWorkstationOrThrow(id);
        WorkstationStatus status = findStatusOrThrow(request.getStatusId());

        workstation.setName(request.getName());
        workstation.setWorkstationStatus(status);
        workstation.setGridX(request.getGridX());
        workstation.setGridY(request.getGridY());

        Workstation saved = workstationRepository.save(workstation);
        return WorkstationResponse.from(saved);
    }

    // DELETE: Only MANAGER or OWNER can delete workstations.
    // SQL cascading handles the rest:
    // - component.workstation_id → ON DELETE SET NULL (components move to storage)
    // - maintenance_log.workstation_id → ON DELETE CASCADE (logs are deleted)
    @Override
    @Transactional
    @PreAuthorize("hasRole('MANAGER') or hasRole('OWNER')")
    public void deleteWorkstation(Integer id) {
        Workstation workstation = findWorkstationOrThrow(id);
        workstationRepository.delete(workstation);
    }

    // ========================
    // BUSINESS LOGIC METHODS
    // ========================

    @Override
    @Transactional
    public WorkstationResponse updateStatus(Integer workstationId, Integer statusId) {
        Workstation workstation = findWorkstationOrThrow(workstationId);
        WorkstationStatus status = findStatusOrThrow(statusId);

        workstation.setWorkstationStatus(status);
        Workstation saved = workstationRepository.save(workstation);
        return WorkstationResponse.from(saved);
    }

    @Override
    @Transactional
    public WorkstationResponse updateGridPosition(Integer workstationId, int gridX, int gridY) {
        if (workstationRepository.existsByGridXAndGridYAndIdNot(gridX, gridY, workstationId)) {
            throw new DuplicateResourceException("A workstation already exists at this grid position.");
        }

        Workstation workstation = findWorkstationOrThrow(workstationId);

        workstation.setGridX(gridX);
        workstation.setGridY(gridY);

        Workstation saved = workstationRepository.save(workstation);
        return WorkstationResponse.from(saved);
    }

    @Override
    public List<ComponentResponse> getComponentsByWorkstationId(Integer workstationId) {
        findWorkstationOrThrow(workstationId);

        return componentRepository.findByWorkstationId(workstationId)
                .stream()
                .map(ComponentResponse::from)
                .toList();
    }

    // GET MAINTENANCE STATUS: Queries the PostgreSQL view 'view_maintenance_status'
    // directly.
    //
    // The view handles all the computation:
    // - CROSS JOIN workstation × active maintenance types
    // - LEFT JOIN to find the latest log per combo
    // - CASE expression to compute status (OK/DUE_SOON/OVERDUE/NEVER_DONE)
    //
    // This is more efficient than recomputing in Java and keeps the logic in one
    // place (the DB).
    @Override
    public List<MaintenanceStatusResponse> getMaintenanceStatus(Integer workstationId) {
        findWorkstationOrThrow(workstationId);

        return maintenanceLogRepository.findMaintenanceStatusByWorkstationId(workstationId)
                .stream()
                .map(MaintenanceStatusResponse::from)
                .toList();
    }

    @Override
    public List<WorkstationResponse> getWorkstationsByStatusId(Integer statusId) {
        return workstationRepository.findByWorkstationStatusId(statusId)
                .stream()
                .map(WorkstationResponse::from)
                .toList();
    }

    // ========================
    // PRIVATE HELPER METHODS
    // ========================

    private Workstation findWorkstationOrThrow(Integer id) {
        return workstationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found with id: " + id));
    }

    private WorkstationStatus findStatusOrThrow(Integer statusId) {
        return workstationStatusRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation status not found with id: " + statusId));
    }
}
