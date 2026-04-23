package com.bookhub.identity.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username length must be between 3 and 50")
        String username,
    @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 255, message = "email length must be less than or equal to 255")
        String email,
    @NotBlank(message = "password is required")
        @Size(min = 8, max = 72, message = "password length must be between 8 and 72")
        String password,
    @NotBlank(message = "displayName is required")
        @Size(min = 2, max = 100, message = "displayName length must be between 2 and 100")
        String displayName) {}
