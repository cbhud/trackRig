package me.cbhud.trackRig.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkstationStatusRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        String name,
        @Nullable
        String color
) {
}
