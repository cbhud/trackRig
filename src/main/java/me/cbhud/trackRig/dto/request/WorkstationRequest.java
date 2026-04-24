package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkstationRequest(
        @NotBlank
        @Size(min = 3, max = 32)
        String name) {

}
