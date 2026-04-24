package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.NotNull;

public record ComponentAssignCategoryRequest(
        @NotNull
        Integer categoryId
) {
}
