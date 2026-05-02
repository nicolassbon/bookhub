package com.bookhub.library.web.library;

import com.bookhub.library.domain.ReadingState;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddBookRequest(@NotNull UUID bookId, ReadingState initialState) {}
