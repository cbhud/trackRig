package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.Role;

public record UserResponse(
        String username,
        String email,
        String fullName,
        Role role
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }
}