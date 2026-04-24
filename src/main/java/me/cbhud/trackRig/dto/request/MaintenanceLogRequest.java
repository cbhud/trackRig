package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record MaintenanceLogRequest(
        @NotNull
        Integer workstationId,
        @NotNull
        Integer maintenanceTypeId,
        @Nullable
        String notes
) {
}
