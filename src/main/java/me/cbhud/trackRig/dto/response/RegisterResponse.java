package me.cbhud.trackRig.dto.response;

import me.cbhud.trackRig.model.AppUser;

public record RegisterResponse(String username, String email, String fullName) {
    public static RegisterResponse from(AppUser user) {
        return new RegisterResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFullName()
        );
    }
}

