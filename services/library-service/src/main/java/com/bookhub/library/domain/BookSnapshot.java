package com.bookhub.library.domain;

import java.util.Objects;

public record BookSnapshot(String title, String coverUrl, Integer pageCount) {

  public BookSnapshot {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (pageCount != null && pageCount < 0) {
      throw new IllegalArgumentException("pageCount must be greater than or equal to zero");
    }
    coverUrl = Objects.requireNonNullElse(coverUrl, "");
  }
}
