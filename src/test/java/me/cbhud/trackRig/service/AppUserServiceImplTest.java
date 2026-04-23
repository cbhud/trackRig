package me.cbhud.trackRig.service;

import me.cbhud.trackRig.dto.*;
import me.cbhud.trackRig.exception.UserAlreadyExistsException;
import me.cbhud.trackRig.exception.UserNotFoundException;
import me.cbhud.trackRig.model.AppUser;
import me.cbhud.trackRig.model.Role;
import me.cbhud.trackRig.repository.AppUserRepository;
import me.cbhud.trackRig.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppUserServiceImpl Unit Test")
class AppUserServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    private AppUser buildUser() {
        AppUser user = new AppUser();
        user.setId(1);
        user.setUsername("gasfloat");
        user.setEmail("gas@gmail.com");
        user.setFullName("Gas Float");
        user.setPassword("encodedPassword");
        user.setRole(Role.EMPLOYEE);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return mapped user response when user exists")
        void shouldReturnUserResponse() {
            // Given
            AppUser user = buildUser();
            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));

            // When
            UserResponse response = appUserService.getCurrentUser("gasfloat");

            // Then
            assertNotNull(response);
            assertEquals("gasfloat", response.username());
            assertEquals("gas@gmail.com", response.email());
            assertEquals("Gas Float", response.fullName());
            assertEquals(Role.EMPLOYEE, response.role());

            verify(appUserRepository).findByUsername("gasfloat");
        }

        @Test
        @DisplayName("should throw when user does not exist")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(appUserRepository.findByUsername("missing")).thenReturn(Optional.empty());

            // When / Then
            assertThrows(UserNotFoundException.class, () -> appUserService.getCurrentUser("missing"));

            verify(appUserRepository).findByUsername("missing");
            verifyNoMoreInteractions(appUserRepository);
        }
    }

    @Nested
    @DisplayName("updateMyProfile")
    class UpdateMyProfileTests {

        @Test
        @DisplayName("should update email and full name without generating token")
        void shouldUpdateProfileWithoutUsernameChange() {
            // Given
            AppUser user = buildUser();
            UpdateMyProfileRequest request = new UpdateMyProfileRequest(
                    null,
                    "newmail@gmail.com",
                    "New Name"
            );

            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));
            when(appUserRepository.save(user)).thenReturn(user);

            // When
            UpdateMyProfileResponse response = appUserService.updateMyProfile("gasfloat", request);

            // Then
            assertNotNull(response);
            assertEquals("newmail@gmail.com", response.user().email());
            assertEquals("New Name", response.user().fullName());
            assertNull(response.token());

            verify(appUserRepository).findByUsername("gasfloat");
            verify(appUserRepository).findByEmail("newmail@gmail.com");
            verify(appUserRepository).save(user);
            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("should update username and generate new token")
        void shouldUpdateUsernameAndGenerateToken() {
            // Given
            AppUser user = buildUser();
            UpdateMyProfileRequest request = new UpdateMyProfileRequest(
                    "newusername",
                    null,
                    null
            );

            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));
            when(appUserRepository.findByUsername("newusername")).thenReturn(Optional.empty());
            when(appUserRepository.save(user)).thenReturn(user);
            when(jwtService.generateToken("newusername")).thenReturn("new-jwt-token");

            // When
            UpdateMyProfileResponse response = appUserService.updateMyProfile("gasfloat", request);

            // Then
            assertNotNull(response);
            assertEquals("newusername", response.user().username());
            assertEquals("new-jwt-token", response.token());

            verify(appUserRepository).findByUsername("gasfloat");
            verify(appUserRepository).findByUsername("newusername");
            verify(appUserRepository).save(user);
            verify(jwtService).generateToken("newusername");
        }

        @Test
        @DisplayName("should throw when new username already exists")
        void shouldThrowWhenUsernameAlreadyExists() {
            // Given
            AppUser currentUser = buildUser();
            AppUser existingUser = buildUser();
            existingUser.setUsername("taken");

            UpdateMyProfileRequest request = new UpdateMyProfileRequest(
                    "taken",
                    null,
                    null
            );

            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(currentUser));
            when(appUserRepository.findByUsername("taken")).thenReturn(Optional.of(existingUser));

            // When / Then
            assertThrows(UserAlreadyExistsException.class,
                    () -> appUserService.updateMyProfile("gasfloat", request));

            verify(appUserRepository).findByUsername("gasfloat");
            verify(appUserRepository).findByUsername("taken");
            verify(appUserRepository, never()).save(any());
            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("changeMyPassword")
    class ChangePasswordTests {

        @Test
        @DisplayName("should change password when current password matches")
        void shouldChangePassword() {
            // Given
            AppUser user = buildUser();
            ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "NewPassword123!");

            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("NewPassword123!")).thenReturn("newEncodedPassword");

            // When
            appUserService.changeMyPassword("gasfloat", request);

            // Then
            assertEquals("newEncodedPassword", user.getPassword());

            verify(appUserRepository).findByUsername("gasfloat");
            verify(passwordEncoder).matches("oldPass", "encodedPassword");
            verify(passwordEncoder).encode("NewPassword123!");
            verify(appUserRepository).save(user);
        }

        @Test
        @DisplayName("should throw when current password is incorrect")
        void shouldThrowWhenCurrentPasswordIsIncorrect() {
            // Given
            AppUser user = buildUser();
            ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "NewPassword123!");

            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPass", "encodedPassword")).thenReturn(false);

            // When / Then
            assertThrows(BadCredentialsException.class,
                    () -> appUserService.changeMyPassword("gasfloat", request));

            verify(appUserRepository).findByUsername("gasfloat");
            verify(passwordEncoder).matches("wrongPass", "encodedPassword");
            verify(appUserRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteMyAccount")
    class DeleteMyAccountTests {

        @Test
        @DisplayName("should delete current user account")
        void shouldDeleteMyAccount() {
            // Given
            AppUser user = buildUser();
            when(appUserRepository.findByUsername("gasfloat")).thenReturn(Optional.of(user));

            // When
            appUserService.deleteMyAccount("gasfloat");

            // Then
            verify(appUserRepository).findByUsername("gasfloat");
            verify(appUserRepository).delete(user);
        }
    }

    @Nested
    @DisplayName("admin user management")
    class AdminUserManagementTests {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            // Given
            AppUser user1 = buildUser();
            AppUser user2 = buildUser();
            user2.setId(2);
            user2.setUsername("owner1");
            user2.setRole(Role.OWNER);

            when(appUserRepository.findAll()).thenReturn(List.of(user1, user2));

            // When
            List<AdminUserResponse> result = appUserService.getAllUsers();

            // Then
            assertEquals(2, result.size());
            assertEquals("gasfloat", result.get(0).username());
            assertEquals("owner1", result.get(1).username());

            verify(appUserRepository).findAll();
        }

        @Test
        @DisplayName("should update target user by id")
        void shouldUpdateUserById() {
            // Given
            AppUser user = buildUser();
            AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                    "updatedUser",
                    "updated@gmail.com",
                    "Updated Name",
                    Role.MANAGER
            );

            when(appUserRepository.findById(2)).thenReturn(Optional.of(user));
            when(appUserRepository.findByUsername("updatedUser")).thenReturn(Optional.empty());
            when(appUserRepository.findByEmail("updated@gmail.com")).thenReturn(Optional.empty());
            when(appUserRepository.save(user)).thenReturn(user);

            // When
            AdminUserResponse response = appUserService.updateUserById(1, 2, request);

            // Then
            assertEquals("updatedUser", response.username());
            assertEquals("updated@gmail.com", response.email());
            assertEquals("Updated Name", response.fullName());
            assertEquals(Role.MANAGER, response.role());

            verify(appUserRepository).findById(2);
            verify(appUserRepository).findByUsername("updatedUser");
            verify(appUserRepository).findByEmail("updated@gmail.com");
            verify(appUserRepository).save(user);
        }

        @Test
        @DisplayName("should throw when caller tries to admin-update self")
        void shouldThrowWhenCallerUpdatesSelf() {
            // Given
            AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                    "updatedUser",
                    "updated@gmail.com",
                    "Updated Name",
                    Role.OWNER
            );

            // When / Then
            assertThrows(AccessDeniedException.class,
                    () -> appUserService.updateUserById(1, 1, request));

            verify(appUserRepository, never()).findById(any());
            verify(appUserRepository, never()).save(any());
        }

        @Test
        @DisplayName("should delete target user by id")
        void shouldDeleteUserById() {
            // Given
            AppUser user = buildUser();
            when(appUserRepository.findById(2)).thenReturn(Optional.of(user));

            // When
            appUserService.deleteUserById(1, 2);

            // Then
            verify(appUserRepository).findById(2);
            verify(appUserRepository).delete(user);
        }

        @Test
        @DisplayName("should throw when caller tries to delete self through admin endpoint")
        void shouldThrowWhenCallerDeletesSelf() {
            // When / Then
            assertThrows(AccessDeniedException.class,
                    () -> appUserService.deleteUserById(1, 1));

            verify(appUserRepository, never()).findById(any());
            verify(appUserRepository, never()).delete(any());
        }
    }
}