package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.WorkstationStatus;
import me.cbhud.trackRig.repository.WorkstationStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkstationStatusServiceImpl implements WorkstationStatusService {

    private final WorkstationStatusRepository workstationRepositoryStatus;

    public WorkstationStatusServiceImpl(WorkstationStatusRepository workstationStatusRepository) {
        this.workstationRepositoryStatus = workstationStatusRepository;
    }

    @Override
    public List<WorkstationStatusResponse> getAllWorkstationStatuses() {
        return workstationRepositoryStatus.findAll().stream().map(WorkstationStatusResponse::from).toList();
    }

    @Override
    public WorkstationStatusResponse getWorkstationStatusById(Integer id) {
        return workstationRepositoryStatus.findById(id)
                .map(WorkstationStatusResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation status not found"));
    }

    @Override
    public WorkstationStatusResponse createWorkstationStatus(WorkstationStatusRequest workstation) {

        if (workstationRepositoryStatus.findByName(workstation.name()).isPresent()) {
            throw new ResourceAlreadyExistsException("Workstation status already exists");
        }
        WorkstationStatus newWorkstationStatus = new WorkstationStatus();
        newWorkstationStatus.setName(workstation.name());
        newWorkstationStatus.setColor(workstation.color() != null ? workstation.color() : "#FFFFFF");
        return WorkstationStatusResponse.from(workstationRepositoryStatus.save(newWorkstationStatus));
    }

    @Override
    public void deleteWorkstationStatus(Integer id) {
        WorkstationStatus workstationStatus = workstationRepositoryStatus.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation status not found"));
        workstationRepositoryStatus.delete(workstationStatus);

    }

    @Override
    public WorkstationStatusResponse updateWorkstationStatus(Integer id,
            WorkstationStatusUpdateRequest workstationStatusUpdateRequest) {
        WorkstationStatus workstationStatus = workstationRepositoryStatus.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation status not found"));
        if (workstationStatusUpdateRequest.name() != null) {
            workstationStatus.setName(workstationStatusUpdateRequest.name());
        }
        if (workstationStatusUpdateRequest.color() != null) {
            workstationStatus.setColor(workstationStatusUpdateRequest.color());
        }
        workstationRepositoryStatus.save(workstationStatus);
        return WorkstationStatusResponse.from(workstationStatus);
    }
}