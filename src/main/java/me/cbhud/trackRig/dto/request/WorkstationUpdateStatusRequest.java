package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.NotNull;

public record WorkstationUpdateStatusRequest(
        @NotNull
        Integer statusId
) {
}
