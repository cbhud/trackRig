package me.cbhud.trackRig.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateMyProfileRequest(

        @Size(min = 3, max = 16)
        String username,

        @Email
        String email,

        @Size(min = 1, max = 100)
        String fullName
) {
}