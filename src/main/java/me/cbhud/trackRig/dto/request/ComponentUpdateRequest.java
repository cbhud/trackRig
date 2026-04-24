package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record ComponentUpdateRequest(
        @Nullable
        @Size(max = 100)
        String serialNumber,
        @Nullable
        @Size(max = 200)
        String name,
        @Nullable
        @Size(max = 255)
        String notes
) {
}
