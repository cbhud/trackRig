package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record ComponentAssignmentLogRequest(
        @NotNull
        Integer componentId,
        @Nullable
        Integer workstationId,
        @Nullable
        String notes
) {
}
