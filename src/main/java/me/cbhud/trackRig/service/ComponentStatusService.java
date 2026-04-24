package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentStatusRequest;
import me.cbhud.trackRig.dto.response.ComponentStatusResponse;

import java.util.List;

public interface ComponentStatusService {
    List<ComponentStatusResponse> getAllComponentStatuses();
    ComponentStatusResponse getComponentStatusById(Integer id);
    ComponentStatusResponse createComponentStatus(ComponentStatusRequest componentStatusRequest);
    void deleteComponentStatus(Integer id);
    ComponentStatusResponse updateComponentStatus(Integer id, ComponentStatusRequest componentStatusRequest);
}