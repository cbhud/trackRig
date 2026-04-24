package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.response.AuthResponse;
import me.cbhud.trackRig.dto.request.LoginRequest;
import me.cbhud.trackRig.dto.request.RegisterRequest;
import me.cbhud.trackRig.dto.response.RegisterResponse;

public interface AuthService {
    public RegisterResponse register(RegisterRequest registerRequest);
    public AuthResponse login(LoginRequest loginRequest);
}
