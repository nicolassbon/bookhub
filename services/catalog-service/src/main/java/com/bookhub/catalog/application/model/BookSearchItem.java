package com.bookhub.catalog.application.model;

public record BookSearchItem(
    String id,
    String title,
    String authorName,
    String sourceReference,
    String isbn13,
    String coverUrl,
    Integer publishedYear) {}
