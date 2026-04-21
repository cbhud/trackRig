package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;

import java.util.List;

public interface AppUserService {
    UserResponse getCurrentUser(String username);
    UpdateMyProfileResponse updateMyProfile(String username, UpdateMyProfileRequest request);
    void changeMyPassword(String username, ChangePasswordRequest request);
    void deleteMyAccount(String username);

    List<AdminUserResponse> getAllUsers();
    AdminUserResponse getUserById(Integer id);
    AdminUserResponse updateUserById(Integer callerId, Integer targetId, AdminUpdateUserRequest request);
    void deleteUserById(Integer callerId, Integer targetId);
    }