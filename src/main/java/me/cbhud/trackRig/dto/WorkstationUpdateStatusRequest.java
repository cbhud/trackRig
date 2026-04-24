package me.cbhud.trackRig.dto;

import jakarta.validation.constraints.NotNull;

public record WorkstationUpdateStatusRequest(
        @NotNull
        Integer statusId
) {
}
