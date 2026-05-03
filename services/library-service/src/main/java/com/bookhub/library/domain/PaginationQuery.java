package com.bookhub.library.domain;

public record PaginationQuery(int page, int size) {

  public PaginationQuery {
    if (page < 0) {
      throw new IllegalArgumentException("page must be zero or greater");
    }
    if (size <= 0) {
      throw new IllegalArgumentException("size must be greater than zero");
    }
  }
}
