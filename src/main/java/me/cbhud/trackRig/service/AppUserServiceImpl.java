package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;
import me.cbhud.trackRig.exception.UserAlreadyExistsException;
import me.cbhud.trackRig.exception.UserNotFoundException;
import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.repository.AppUserRepository;
import me.cbhud.trackRig.security.JwtService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserRepository appUserRepository,
                              JwtService jwtService,
                              PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse getCurrentUser(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public UpdateMyProfileResponse updateMyProfile(String username, UpdateMyProfileRequest request) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        boolean usernameChanged = false;

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            appUserRepository.findByUsername(request.username()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Account with that username already exists!");
            });
            user.setUsername(request.username());
            usernameChanged = true;
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            appUserRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Account with that email already exists!");
            });
            user.setEmail(request.email());
        }

        if (request.fullName() != null && !request.fullName().equals(user.getFullName())) {
            user.setFullName(request.fullName());
        }

        AppUser savedUser = appUserRepository.save(user);

        String newToken = null;
        if (usernameChanged) {
            newToken = jwtService.generateToken(savedUser.getUsername());
        }

        return new UpdateMyProfileResponse(
                UserResponse.from(savedUser),
                newToken
        );
    }

    @Override
    @Transactional
    public void changeMyPassword(String username, ChangePasswordRequest request) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        appUserRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteMyAccount(String username) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        appUserRepository.delete(user);
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return appUserRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @Override
    public AdminUserResponse getUserById(Integer id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        return AdminUserResponse.from(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserById(Integer callerId, Integer targetId, AdminUpdateUserRequest request) {
        if (callerId.equals(targetId)) {
            throw new AccessDeniedException("You cannot modify your own account through admin endpoints. Use /users/me.");
        }

        AppUser user = appUserRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            appUserRepository.findByUsername(request.username()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Account with that username already exists!");
            });
            user.setUsername(request.username());
        }

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            appUserRepository.findByEmail(request.email()).ifPresent(existing -> {
                throw new UserAlreadyExistsException("Account with that email already exists!");
            });
            user.setEmail(request.email());
        }

        if (request.fullName() != null && !request.fullName().equals(user.getFullName())) {
            user.setFullName(request.fullName());
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        AppUser savedUser = appUserRepository.save(user);
        return AdminUserResponse.from(savedUser);
    }

    @Override
    @Transactional
    public void deleteUserById(Integer callerId, Integer targetId) {
        if (callerId.equals(targetId)) {
            throw new AccessDeniedException("You cannot delete your own account through admin endpoints. Use /users/me.");
        }

        AppUser user = appUserRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("User does not exist"));

        appUserRepository.delete(user);
    }

}