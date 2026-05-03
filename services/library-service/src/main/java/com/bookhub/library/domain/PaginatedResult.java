package com.bookhub.library.domain;

import java.util.List;

public record PaginatedResult<T>(
    List<T> items, int page, int size, long totalElements, int totalPages) {}
