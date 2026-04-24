package me.cbhud.trackRig.service;

import jakarta.transaction.Transactional;
import me.cbhud.trackRig.dto.request.LoginRequest;
import me.cbhud.trackRig.dto.request.RegisterRequest;
import me.cbhud.trackRig.dto.response.AuthResponse;
import me.cbhud.trackRig.dto.response.RegisterResponse;
import me.cbhud.trackRig.exception.UserAlreadyExistsException;
import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.Role;
import me.cbhud.trackRig.repository.AppUserRepository;
import me.cbhud.trackRig.security.JwtService;
import me.cbhud.trackRig.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    AuthServiceImpl(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService){
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

        if (appUserRepository.findByEmail(registerRequest.email()).isPresent()){
            throw new UserAlreadyExistsException("Account with that email already exists!");
        }

        if (appUserRepository.findByUsername(registerRequest.username()).isPresent()){
            throw new UserAlreadyExistsException("Account with that username already exists!");
        }

        AppUser newUser = new AppUser();

        newUser.setUsername(registerRequest.username());
        newUser.setEmail(registerRequest.email());
        newUser.setPassword(passwordEncoder.encode(registerRequest.password()));
        newUser.setFullName(registerRequest.fullName());
        newUser.setRole(Role.EMPLOYEE);

        AppUser savedUser = appUserRepository.save(newUser);

        return RegisterResponse.from(savedUser);

    }


    @Override
    public AuthResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());

    }



}
