package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserService registerUserService;

    @Test
    @DisplayName("Should create user and return 201 response")
    void shouldCreateUserAndReturn201Response() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class))).thenReturn(
                RegisterUserResult.builder()
                        .userId("usr_123")
                        .username("nico")
                        .email("nico@example.com")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build());

        final String requestBody = """
                {
                  "username": "nico",
                  "email": "nico@example.com",
                  "password": "StrongPassword123!",
                  "displayName": "Nicolas Bon"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("usr_123"))
                .andExpect(jsonPath("$.username").value("nico"))
                .andExpect(jsonPath("$.email").value("nico@example.com"))
                .andExpect(jsonPath("$.displayName").value("Nicolas Bon"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should return 400 with structured error when request is invalid")
    void shouldReturn400WithStructuredErrorWhenRequestIsInvalid() throws Exception {
        final String invalidRequestBody = """
                {
                  "username": "",
                  "email": "not-an-email",
                  "password": "123",
                  "displayName": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
    }

    @Test
    @DisplayName("Should return 409 with structured error when email or username already exists")
    void shouldReturn409WithStructuredErrorWhenEmailOrUsernameAlreadyExists() throws Exception {
        when(registerUserService.register(any(RegisterUserCommand.class))).thenThrow(
                new DuplicateResourceException("email", "Email already in use"));

        final String requestBody = """
                {
                  "username": "nico",
                  "email": "nico@example.com",
                  "password": "StrongPassword123!",
                  "displayName": "Nicolas Bon"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.message").value("Email already in use"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/register"));
    }
}
