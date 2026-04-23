package com.bookhub.catalog.web;

import lombok.Builder;

@Builder
public record BookDetailResponse(
    String id,
    String title,
    String authorName,
    String isbn13,
    String sourceReference,
    String coverUrl,
    Integer publishedYear) {}
