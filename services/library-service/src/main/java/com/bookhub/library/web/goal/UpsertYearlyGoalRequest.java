package com.bookhub.library.web.goal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpsertYearlyGoalRequest(
    @NotNull(message = "year is required") Integer year,
    @Min(value = 1, message = "targetBooks must be at least 1") int targetBooks) {}
