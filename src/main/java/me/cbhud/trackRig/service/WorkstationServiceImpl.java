package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.Workstation;
import me.cbhud.trackRig.model.WorkstationStatus;
import me.cbhud.trackRig.repository.WorkstationRepository;
import me.cbhud.trackRig.repository.WorkstationStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


@Service
public class WorkstationServiceImpl implements WorkstationService{

    private final WorkstationRepository workstationRepository;
    private final WorkstationStatusRepository workstationStatusRepository;

    public WorkstationServiceImpl(WorkstationRepository workstationRepository, WorkstationStatusRepository workstationStatusRepository
    ){
        this.workstationRepository = workstationRepository;
        this.workstationStatusRepository = workstationStatusRepository;
    }

    @Override
    public List<WorkstationResponse> getAllWorkstations() {
        return workstationRepository.findAll()
                .stream()
                .map(WorkstationResponse::from)
                .toList();
    }

    @Override
    public WorkstationResponse getWorkstationById(Integer id) {
        return workstationRepository.findById(id)
                .map(WorkstationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));
    }

    @Override
    public WorkstationResponse createWorkstation(WorkstationRequest workstationRequest) {
        if (workstationRepository.findByName(workstationRequest.name()).isPresent()){
            throw new ResourceAlreadyExistsException("Workstation already exists");
        }
        Workstation newWorkstation = new Workstation();
        newWorkstation.setName(workstationRequest.name());

        Workstation savedWorkstation = workstationRepository.save(newWorkstation);
        return WorkstationResponse.from(savedWorkstation);
    }

    @Override
    @Transactional
    public void deleteWorkstation(Integer id) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));
        workstationRepository.delete(workstation);
    }


    @Override
    @Transactional
    public WorkstationResponse updateWorkstation(
            Integer id,
            WorkstationUpdateRequest workstationUpdateRequest
    ) {
        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));

        boolean gridXProvided = workstationUpdateRequest.gridX() != null;
        boolean gridYProvided = workstationUpdateRequest.gridY() != null;
        boolean floorProvided = workstationUpdateRequest.floor() != null;

        boolean anyGridProvided = gridXProvided || gridYProvided || floorProvided;
        boolean allGridProvided = gridXProvided && gridYProvided && floorProvided;

        if (anyGridProvided && !allGridProvided) {
            throw new IllegalArgumentException(
                    "gridX, gridY, and floor must all be provided together"
            );
        }

        boolean changed = false;

        if (workstationUpdateRequest.name() != null
                && !Objects.equals(workstation.getName(), workstationUpdateRequest.name())) {
            workstation.setName(workstationUpdateRequest.name());
            changed = true;
        }

        if (allGridProvided) {
            if (!Objects.equals(workstation.getGridX(), workstationUpdateRequest.gridX())) {
                workstation.setGridX(workstationUpdateRequest.gridX());
                changed = true;
            }

            if (!Objects.equals(workstation.getGridY(), workstationUpdateRequest.gridY())) {
                workstation.setGridY(workstationUpdateRequest.gridY());
                changed = true;
            }

            if (!Objects.equals(workstation.getFloor(), workstationUpdateRequest.floor())) {
                workstation.setFloor(workstationUpdateRequest.floor());
                changed = true;
            }
        }

        if (changed) {
            workstation = workstationRepository.save(workstation);
        }

        return WorkstationResponse.from(workstation);
    }

    @Override
    @Transactional
    public WorkstationResponse updateWorkstationStatus(Integer id, WorkstationUpdateStatusRequest workstationUpdateStatusRequest) {
        WorkstationStatus status = workstationStatusRepository
                .findById(workstationUpdateStatusRequest.statusId())
                .orElseThrow(() -> new ResourceNotFoundException("Workstation status not found"));

        Workstation workstation = workstationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));

        workstation.setStatus(status);
        return WorkstationResponse.from(workstationRepository.save(workstation));
    }


}