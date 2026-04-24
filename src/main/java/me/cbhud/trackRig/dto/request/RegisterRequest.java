package me.cbhud.trackRig.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 16)
        String username,
        @Email
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 8, max = 32)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#+\\-_])[A-Za-z\\d@$!%*?&#+\\-_]{8,32}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
        String password,
        @NotBlank
        String fullName) {
}
