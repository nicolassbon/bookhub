package com.bookhub.library.web.library;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProgressRequest(@NotNull @PositiveOrZero Integer pagesRead) {}
