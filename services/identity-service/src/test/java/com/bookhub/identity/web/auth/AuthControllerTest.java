package com.bookhub.identity.web.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.application.auth.LoginUserCommand;
import com.bookhub.identity.application.auth.LoginUserResult;
import com.bookhub.identity.application.auth.RegisterUserCommand;
import com.bookhub.identity.application.auth.RegisterUserResult;
import com.bookhub.identity.application.auth.RegisterUserService;
import com.bookhub.identity.config.SecurityConfig;
import com.bookhub.identity.domain.user.DuplicateResourceException;
import com.bookhub.identity.application.auth.LoginUserService;
import com.bookhub.identity.application.auth.InvalidCredentialsException;
import com.bookhub.identity.web.error.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@TestPropertySource(properties = {
        "jwt.secret=test-signing-secret-test-signing-secret-1234",
        "jwt.expiration=3600"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterUserService registerUserService;

    @MockBean
    private LoginUserService loginUserService;

    @MockBean
    private AuthWebMapper authWebMapper;

    @Test
    @DisplayName("Should create user and return 201 response")
    void shouldCreateUserAndReturn201Response() throws Exception {
        when(authWebMapper.toRegisterUserCommand(any(RegisterRequest.class))).thenReturn(RegisterUserCommand.builder()
                .username("nico")
                .email("nico@example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build());

        when(registerUserService.register(any(RegisterUserCommand.class))).thenReturn(
                RegisterUserResult.builder()
                        .userId("usr_123")
                        .username("nico")
                        .email("nico@example.com")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build());

        when(authWebMapper.toRegisterResponse(any(RegisterUserResult.class))).thenReturn(RegisterResponse.builder()
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
        when(authWebMapper.toRegisterUserCommand(any(RegisterRequest.class))).thenReturn(RegisterUserCommand.builder()
                .username("nico")
                .email("nico@example.com")
                .password("StrongPassword123!")
                .displayName("Nicolas Bon")
                .build());

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

    @Test
    @DisplayName("Should authenticate user and return 200 response")
    void shouldAuthenticateUserAndReturn200Response() throws Exception {
        when(authWebMapper.toLoginUserCommand(any(LoginRequest.class))).thenReturn(LoginUserCommand.builder()
                .email("nico@example.com")
                .password("StrongPassword123!")
                .build());

        when(loginUserService.login(any(LoginUserCommand.class))).thenReturn(LoginUserResult.builder()
                .accessToken("jwt-access-token")
                .expiresIn(3600)
                .user(LoginUserResult.LoginUserView.builder()
                        .userId("usr_123")
                        .username("nico")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build())
                .build());

        when(authWebMapper.toLoginResponse(any(LoginUserResult.class))).thenReturn(LoginResponse.builder()
                .accessToken("jwt-access-token")
                .expiresIn(3600)
                .user(LoginResponse.LoginUserResponse.builder()
                        .userId("usr_123")
                        .username("nico")
                        .displayName("Nicolas Bon")
                        .role("USER")
                        .build())
                .build());

        final String requestBody = """
                {
                  "email": "nico@example.com",
                  "password": "StrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-access-token"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.user.userId").value("usr_123"))
                .andExpect(jsonPath("$.user.username").value("nico"))
                .andExpect(jsonPath("$.user.displayName").value("Nicolas Bon"))
                .andExpect(jsonPath("$.user.role").value("USER"));
    }

    @Test
    @DisplayName("Should return 400 with structured error when login request is invalid")
    void shouldReturn400WithStructuredErrorWhenLoginRequestIsInvalid() throws Exception {
        final String invalidRequestBody = """
                {
                  "email": "not-an-email",
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    @DisplayName("Should return 401 with structured error when credentials are invalid")
    void shouldReturn401WithStructuredErrorWhenCredentialsAreInvalid() throws Exception {
        when(authWebMapper.toLoginUserCommand(any(LoginRequest.class))).thenReturn(LoginUserCommand.builder()
                .email("nico@example.com")
                .password("WrongPassword123!")
                .build());

        when(loginUserService.login(any(LoginUserCommand.class))).thenThrow(new InvalidCredentialsException());

        final String requestBody = """
                {
                  "email": "nico@example.com",
                  "password": "WrongPassword123!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }
}
