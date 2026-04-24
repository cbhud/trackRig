package me.cbhud.trackRig.controller;

import jakarta.validation.Valid;
import me.cbhud.trackRig.dto.request.AdminUpdateUserRequest;
import me.cbhud.trackRig.dto.request.ChangePasswordRequest;
import me.cbhud.trackRig.dto.request.UpdateMyProfileRequest;
import me.cbhud.trackRig.dto.response.AdminUserResponse;
import me.cbhud.trackRig.dto.response.UpdateMyProfileResponse;
import me.cbhud.trackRig.dto.response.UserResponse;
import me.cbhud.trackRig.security.SecurityUser;
import me.cbhud.trackRig.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal SecurityUser user) {
        return ResponseEntity.ok(appUserService.getCurrentUser(user.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UpdateMyProfileResponse> updateMyProfile(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody UpdateMyProfileRequest request
    ) {
        return ResponseEntity.ok(appUserService.updateMyProfile(user.getUsername(), request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changeMyPassword(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        appUserService.changeMyPassword(user.getUsername(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal SecurityUser user) {
        appUserService.deleteMyAccount(user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        return ResponseEntity.ok(appUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(appUserService.getUserById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<AdminUserResponse> updateUserById(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Integer id,
            @Valid @RequestBody AdminUpdateUserRequest request
    ) {
        return ResponseEntity.ok(appUserService.updateUserById(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteUserById(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable Integer id
    ) {
        appUserService.deleteUserById(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}