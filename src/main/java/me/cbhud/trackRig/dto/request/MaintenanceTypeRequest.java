package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record MaintenanceTypeRequest(
        @NotNull
        String name,
        @Nullable
        String description,
        @NotNull
        @Min(1)
        Integer intervalDays,
        @Nullable
        Boolean isActive
) {
}
