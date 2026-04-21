package me.cbhud.trackRig.dto;

import jakarta.validation.constraints.NotNull;
import me.cbhud.trackRig.model.Role;

public record UpdateUserRoleRequest(
        @NotNull
        Role role
) {
}