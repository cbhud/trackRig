package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentStatusRequest;
import me.cbhud.trackRig.dto.response.ComponentStatusResponse;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.ComponentStatus;
import me.cbhud.trackRig.repository.ComponentStatusRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentStatusServiceImpl implements ComponentStatusService {

    private final ComponentStatusRepository componentStatusRepository;

    public ComponentStatusServiceImpl(ComponentStatusRepository componentStatusRepository){
        this.componentStatusRepository = componentStatusRepository;
    }

    @Override
    public List<ComponentStatusResponse> getAllComponentStatuses() {
        return componentStatusRepository.findAll().stream().map(ComponentStatusResponse::from).toList();
    }

    @Override
    public ComponentStatusResponse getComponentStatusById(Integer id) {

        return componentStatusRepository.findById(id)
                .map(ComponentStatusResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Component status not found"));

    }

    @Override
    public ComponentStatusResponse createComponentStatus(ComponentStatusRequest componentStatusRequest) {
        if (componentStatusRepository.findByName(componentStatusRequest.name()).isPresent()){
            throw new ResourceAlreadyExistsException("Component status already exists");
        }

        ComponentStatus newComponentStatus = new ComponentStatus();
        newComponentStatus.setName(componentStatusRequest.name());
        return ComponentStatusResponse.from(componentStatusRepository.save(newComponentStatus));

    }

    @Override
    public void deleteComponentStatus(Integer id) {

        componentStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component status not found"));
        componentStatusRepository.deleteById(id);

    }

    @Override
    public ComponentStatusResponse updateComponentStatus(Integer id, ComponentStatusRequest componentStatusRequest) {

        ComponentStatus componentStatus = componentStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component status not found"));
        if (componentStatusRequest.name() != null) {
            componentStatus.setName(componentStatusRequest.name());
        }
        return ComponentStatusResponse.from(componentStatusRepository.save(componentStatus));

    }
}