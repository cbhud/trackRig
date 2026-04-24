package me.cbhud.trackRig.controller;

import tools.jackson.databind.ObjectMapper;
import me.cbhud.trackRig.dto.request.LoginRequest;
import me.cbhud.trackRig.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("should register user and return 201")
        void shouldRegisterUser() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "john123",
                    "john@example.com",
                    "Password123!",
                    "John Doe"
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("john123"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.fullName").value("John Doe"));
        }

        @Test
        @DisplayName("should return 400 when register request is invalid")
        void shouldReturnBadRequestWhenRegisterRequestIsInvalid() throws Exception {
            RegisterRequest request = new RegisterRequest(
                    "",
                    "not-an-email",
                    "123",
                    ""
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("should login successfully after registration")
        void shouldLoginSuccessfully() throws Exception {
            RegisterRequest registerRequest = new RegisterRequest(
                    "john123",
                    "john@example.com",
                    "Password123!",
                    "John Doe"
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest(
                    "john123",
                    "Password123!"
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").isNotEmpty())
                    .andExpect(jsonPath("$.username").value("john123"))
                    .andExpect(jsonPath("$.role").value("EMPLOYEE"));
        }

        @Test
        @DisplayName("should return 401 when password is incorrect")
        void shouldReturnUnauthorizedWhenPasswordIsIncorrect() throws Exception {
            RegisterRequest registerRequest = new RegisterRequest(
                    "john123",
                    "john@example.com",
                    "Password123!",
                    "John Doe"
            );

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest(
                    "john123",
                    "WrongPassword123!"
            );

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}