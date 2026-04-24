package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.*;
import me.cbhud.trackRig.dto.response.ComponentResponse;

import java.util.List;

public interface ComponentService {
    List<ComponentResponse> getAllComponents();
    ComponentResponse getComponentById(Integer id);
    ComponentResponse createComponent(ComponentRequest request);
    ComponentResponse updateComponent(Integer id, ComponentUpdateRequest request);
    void deleteComponent(Integer id);
    ComponentResponse assignWorkstation(Integer id, ComponentAssignWorkstationRequest request);
    ComponentResponse assignCategory(Integer id, ComponentAssignCategoryRequest request);
    ComponentResponse assignStatus(Integer id, ComponentAssignStatusRequest request);
}
