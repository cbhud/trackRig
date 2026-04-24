package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record WorkstationUpdateRequest(
        @Nullable
        @Size(min = 3, max = 32)
        String name,
        @Nullable
        Integer gridX,
        @Nullable
        Integer gridY,
        @Nullable
        Integer floor
) {
}
