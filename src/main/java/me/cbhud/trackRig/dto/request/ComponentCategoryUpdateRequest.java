package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComponentCategoryUpdateRequest(
        @Nullable
        @Size(max = 50)
        String name,
        @Nullable
        @Size(max = 255)
        String description
) {
}
