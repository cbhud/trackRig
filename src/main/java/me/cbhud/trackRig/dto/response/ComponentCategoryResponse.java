package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.ComponentCategory;

public record ComponentCategoryResponse(
        Integer id,
        String name,
        String description
) {
    public static ComponentCategoryResponse from(ComponentCategory componentCategory) {
        return new ComponentCategoryResponse(
                componentCategory.getId(),
                componentCategory.getName(),
                componentCategory.getDescription()
        );
    }
}
