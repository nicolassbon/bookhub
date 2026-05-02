package com.bookhub.library.web.library;

import com.bookhub.library.domain.ReadingState;
import jakarta.validation.constraints.NotNull;

public record UpdateStateRequest(@NotNull ReadingState state) {}
