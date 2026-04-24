package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;

public record MaintenanceTypeUpdateRequest(
        @Nullable
        String name,
        @Nullable
        String description,
        @Nullable
        @Min(1)
        Integer intervalDays,
        @Nullable
        Boolean isActive
) {
}
