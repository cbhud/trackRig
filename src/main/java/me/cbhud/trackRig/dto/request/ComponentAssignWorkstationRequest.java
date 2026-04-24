package me.cbhud.trackRig.dto.request;

import jakarta.annotation.Nullable;

public record ComponentAssignWorkstationRequest(
        @Nullable
        Integer workstationId
) {
}
