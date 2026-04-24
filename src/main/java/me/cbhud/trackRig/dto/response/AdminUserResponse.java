package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.Role;

import java.time.OffsetDateTime;

public record AdminUserResponse(
        Integer id,
        String username,
        String email,
        String fullName,
        Role role,
        OffsetDateTime createdAt
) {
    public static AdminUserResponse from(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}