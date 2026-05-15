package com.bookhub.library.web.admin;

import jakarta.validation.constraints.NotNull;

public record ModerateReviewRequest(@NotNull(message = "status must not be null") String status) {}
