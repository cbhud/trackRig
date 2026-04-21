package me.cbhud.trackRig.dto;

public record UpdateMyProfileResponse(

        UserResponse user,
        String token
) {
}
