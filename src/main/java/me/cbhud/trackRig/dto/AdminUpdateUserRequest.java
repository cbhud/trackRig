package me.cbhud.trackRig.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import me.cbhud.trackRig.model.Role;

public record AdminUpdateUserRequest(

        @Size(min = 3, max = 16)
        String username,

        @Email
        String email,

        @Size(min = 1, max = 100)
        String fullName,

        Role role
) {
}