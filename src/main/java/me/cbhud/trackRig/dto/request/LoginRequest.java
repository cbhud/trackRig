package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(min = 3, max = 16)
        String username,
        @NotBlank
        @Size(min = 8, max = 32)
        String password) {
}
