package com.bookhub.identity.web.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "token is required") String token,
    @NotBlank(message = "newPassword is required")
        @Size(min = 8, max = 72, message = "newPassword length must be between 8 and 72")
        String newPassword) {}
