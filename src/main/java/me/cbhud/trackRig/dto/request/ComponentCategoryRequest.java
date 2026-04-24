package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComponentCategoryRequest(
        @NotNull
        @Size(min = 3, max = 50)
        String name,
        @Size(max = 255)
        String description
) {
}
