package com.bookhub.identity.web.admin;

import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequest(@NotNull(message = "role must not be null") String role) {}
