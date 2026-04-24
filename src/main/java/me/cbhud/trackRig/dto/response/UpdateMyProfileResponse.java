package me.cbhud.trackRig.dto.response;

public record UpdateMyProfileResponse(

        UserResponse user,
        String token
) {
}
