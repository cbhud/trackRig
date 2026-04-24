package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentAssignmentLogRequest;
import me.cbhud.trackRig.dto.response.ComponentAssignmentLogResponse;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.Component;
import me.cbhud.trackRig.model.ComponentAssignmentLog;
import me.cbhud.trackRig.model.Workstation;
import me.cbhud.trackRig.repository.AppUserRepository;
import me.cbhud.trackRig.repository.ComponentAssignmentLogRepository;
import me.cbhud.trackRig.repository.ComponentRepository;
import me.cbhud.trackRig.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ComponentAssignmentLogServiceImpl implements ComponentAssignmentLogService {

    private final ComponentAssignmentLogRepository logRepository;
    private final ComponentRepository componentRepository;
    private final WorkstationRepository workstationRepository;
    private final AppUserRepository userRepository;

    public ComponentAssignmentLogServiceImpl(
            ComponentAssignmentLogRepository logRepository,
            ComponentRepository componentRepository,
            WorkstationRepository workstationRepository,
            AppUserRepository userRepository
    ) {
        this.logRepository = logRepository;
        this.componentRepository = componentRepository;
        this.workstationRepository = workstationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ComponentAssignmentLogResponse> getAllLogs() {
        return logRepository.findAll()
                .stream()
                .map(ComponentAssignmentLogResponse::from)
                .toList();
    }

    @Override
    public List<ComponentAssignmentLogResponse> getLogsByComponent(Integer componentId) {
        if (!componentRepository.existsById(componentId)) {
            throw new ResourceNotFoundException("Component not found");
        }
        return logRepository.findByComponentId(componentId)
                .stream()
                .map(ComponentAssignmentLogResponse::from)
                .toList();
    }

    @Override
    public List<ComponentAssignmentLogResponse> getLogsByWorkstation(Integer workstationId) {
        if (!workstationRepository.existsById(workstationId)) {
            throw new ResourceNotFoundException("Workstation not found");
        }
        return logRepository.findByWorkstationId(workstationId)
                .stream()
                .map(ComponentAssignmentLogResponse::from)
                .toList();
    }

    @Override
    public ComponentAssignmentLogResponse getActiveAssignment(Integer componentId) {
        if (!componentRepository.existsById(componentId)) {
            throw new ResourceNotFoundException("Component not found");
        }
        return logRepository.findByComponentIdAndRemovedAtIsNull(componentId)
                .map(ComponentAssignmentLogResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment for this component"));
    }

    @Override
    @Transactional
    public ComponentAssignmentLogResponse createAssignment(ComponentAssignmentLogRequest request, String username) {
        Component component = componentRepository.findById(request.componentId())
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

        // Close existing active assignment if present
        logRepository.findByComponentIdAndRemovedAtIsNull(request.componentId())
                .ifPresent(existing -> {
                    existing.setRemovedAt(OffsetDateTime.now());
                    logRepository.save(existing);
                });

        Workstation workstation = null;
        if (request.workstationId() != null) {
            workstation = workstationRepository.findById(request.workstationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));
        }

        AppUser assignedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ComponentAssignmentLog log = new ComponentAssignmentLog();
        log.setComponent(component);
        log.setWorkstation(workstation);
        log.setAssignedByUser(assignedBy);
        log.setNotes(request.notes());

        return ComponentAssignmentLogResponse.from(logRepository.save(log));
    }

    @Override
    @Transactional
    public ComponentAssignmentLogResponse closeAssignment(Integer componentId, String notes) {
        ComponentAssignmentLog log = logRepository
                .findByComponentIdAndRemovedAtIsNull(componentId)
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment for this component"));

        log.setRemovedAt(OffsetDateTime.now());
        if (notes != null) {
            log.setNotes(notes);
        }

        return ComponentAssignmentLogResponse.from(logRepository.save(log));
    }
}
