package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.*;
import me.cbhud.trackRig.dto.response.ComponentResponse;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.Component;
import me.cbhud.trackRig.model.ComponentCategory;
import me.cbhud.trackRig.model.ComponentStatus;
import me.cbhud.trackRig.model.Workstation;
import me.cbhud.trackRig.repository.ComponentCategoryRepository;
import me.cbhud.trackRig.repository.ComponentRepository;
import me.cbhud.trackRig.repository.ComponentStatusRepository;
import me.cbhud.trackRig.repository.WorkstationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ComponentServiceImpl implements ComponentService {

    private final ComponentRepository componentRepository;
    private final ComponentCategoryRepository categoryRepository;
    private final ComponentStatusRepository statusRepository;
    private final WorkstationRepository workstationRepository;

    public ComponentServiceImpl(
            ComponentRepository componentRepository,
            ComponentCategoryRepository categoryRepository,
            ComponentStatusRepository statusRepository,
            WorkstationRepository workstationRepository
    ) {
        this.componentRepository = componentRepository;
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
        this.workstationRepository = workstationRepository;
    }

    @Override
    public List<ComponentResponse> getAllComponents() {
        return componentRepository.findAll()
                .stream()
                .map(ComponentResponse::from)
                .toList();
    }

    @Override
    public ComponentResponse getComponentById(Integer id) {
        return componentRepository.findById(id)
                .map(ComponentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));
    }

    @Override
    @Transactional
    public ComponentResponse createComponent(ComponentRequest request) {
        if (request.serialNumber() != null && componentRepository.findBySerialNumber(request.serialNumber()).isPresent()) {
            throw new ResourceAlreadyExistsException("Component with serial number already exists");
        }

        ComponentCategory category = categoryRepository
                .findById(request.componentCategory().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Component category not found"));

        ComponentStatus status = statusRepository
                .findById(request.componentStatus().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Component status not found"));

        Workstation workstation = null;
        if (request.workstation() != null) {
            workstation = workstationRepository.findByName(request.workstation().name())
                    .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));
        }

        Component component = new Component();
        component.setSerialNumber(request.serialNumber());
        component.setName(request.name());
        component.setNotes(request.notes());
        component.setCategory(category);
        component.setStatus(status);
        component.setWorkstation(workstation);

        return ComponentResponse.from(componentRepository.save(component));
    }

    @Override
    @Transactional
    public ComponentResponse updateComponent(Integer id, ComponentUpdateRequest request) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

        boolean changed = false;

        if (request.serialNumber() != null && !Objects.equals(component.getSerialNumber(), request.serialNumber())) {
            componentRepository.findBySerialNumber(request.serialNumber())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new ResourceAlreadyExistsException("Component with serial number already exists");
                    });
            component.setSerialNumber(request.serialNumber());
            changed = true;
        }

        if (request.name() != null && !Objects.equals(component.getName(), request.name())) {
            component.setName(request.name());
            changed = true;
        }

        if (request.notes() != null && !Objects.equals(component.getNotes(), request.notes())) {
            component.setNotes(request.notes());
            changed = true;
        }

        if (changed) {
            component = componentRepository.save(component);
        }

        return ComponentResponse.from(component);
    }

    @Override
    @Transactional
    public void deleteComponent(Integer id) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));
        componentRepository.delete(component);
    }

    @Override
    @Transactional
    public ComponentResponse assignWorkstation(Integer id, ComponentAssignWorkstationRequest request) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

        if (request.workstationId() == null) {
            component.setWorkstation(null);
        } else {
            Workstation workstation = workstationRepository.findById(request.workstationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Workstation not found"));
            component.setWorkstation(workstation);
        }

        return ComponentResponse.from(componentRepository.save(component));
    }

    @Override
    @Transactional
    public ComponentResponse assignCategory(Integer id, ComponentAssignCategoryRequest request) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

        ComponentCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Component category not found"));

        component.setCategory(category);
        return ComponentResponse.from(componentRepository.save(component));
    }

    @Override
    @Transactional
    public ComponentResponse assignStatus(Integer id, ComponentAssignStatusRequest request) {
        Component component = componentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component not found"));

        ComponentStatus status = statusRepository.findById(request.statusId())
                .orElseThrow(() -> new ResourceNotFoundException("Component status not found"));

        component.setStatus(status);
        return ComponentResponse.from(componentRepository.save(component));
    }
}
