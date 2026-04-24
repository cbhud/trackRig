package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.request.ComponentCategoryRequest;
import me.cbhud.trackRig.dto.request.ComponentCategoryUpdateRequest;
import me.cbhud.trackRig.dto.response.ComponentCategoryResponse;
import me.cbhud.trackRig.exception.ResourceAlreadyExistsException;
import me.cbhud.trackRig.exception.ResourceNotFoundException;
import me.cbhud.trackRig.model.ComponentCategory;
import me.cbhud.trackRig.repository.ComponentCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComponentCategoryServiceImpl implements ComponentCategoryService {

    private final ComponentCategoryRepository componentCategoryRepository;

    public ComponentCategoryServiceImpl(ComponentCategoryRepository componentCategoryRepository){
        this.componentCategoryRepository = componentCategoryRepository;
    }


    @Override
    public List<ComponentCategoryResponse> getAllComponentCategories() {
        return componentCategoryRepository.findAll().stream().map(ComponentCategoryResponse::from).toList();
    }

    @Override
    public ComponentCategoryResponse getComponentCategoryById(Integer id) {
        return componentCategoryRepository.findById(id)
                .map(ComponentCategoryResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Component category not found"));
    }

    @Override
    public ComponentCategoryResponse createComponentCategory(ComponentCategoryRequest componentCategory) {
        if (componentCategoryRepository.findByName(componentCategory.name()).isPresent()){
            throw new ResourceAlreadyExistsException("Component category already exists");
        }
        ComponentCategory newComponentCategory = new ComponentCategory();
        newComponentCategory.setName(componentCategory.name());
        newComponentCategory.setDescription(componentCategory.description());
        return ComponentCategoryResponse.from(componentCategoryRepository.save(newComponentCategory));
    }

    @Override
    public void deleteComponentCategory(Integer id) {
        ComponentCategory componentCategory = componentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component category not found"));
        componentCategoryRepository.delete(componentCategory);
    }

    @Override
    public ComponentCategoryResponse updateComponentCategory(Integer id, ComponentCategoryUpdateRequest componentCategoryUpdateRequest) {
        ComponentCategory componentCategory = componentCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Component category not found"));
        if (componentCategoryUpdateRequest.name() != null) {
            componentCategory.setName(componentCategoryUpdateRequest.name());
        }
        if (componentCategoryUpdateRequest.description() != null) {
            componentCategory.setDescription(componentCategoryUpdateRequest.description());
        }
        componentCategoryRepository.save(componentCategory);
        return ComponentCategoryResponse.from(componentCategory);
    }
}