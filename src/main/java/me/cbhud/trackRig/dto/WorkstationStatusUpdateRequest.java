package me.cbhud.trackRig.dto;

import jakarta.validation.constraints.Size;

public record WorkstationStatusUpdateRequest(
        @Size(min = 3, max = 32)
        String name,
        @Size(min = 3, max = 32)
        String color
) {
}
