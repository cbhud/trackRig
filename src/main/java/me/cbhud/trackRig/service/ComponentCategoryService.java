package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentCategoryRequest;
import me.cbhud.trackRig.dto.request.ComponentCategoryUpdateRequest;
import me.cbhud.trackRig.dto.response.ComponentCategoryResponse;

import java.util.List;

public interface ComponentCategoryService {
    List<ComponentCategoryResponse> getAllComponentCategories();
    ComponentCategoryResponse getComponentCategoryById(Integer id);
    ComponentCategoryResponse createComponentCategory(ComponentCategoryRequest componentCategory);
    void deleteComponentCategory(Integer id);
    ComponentCategoryResponse updateComponentCategory(Integer id, ComponentCategoryUpdateRequest componentCategoryUpdateRequest);
}