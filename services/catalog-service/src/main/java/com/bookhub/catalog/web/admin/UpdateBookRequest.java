package com.bookhub.catalog.web.admin;

public record UpdateBookRequest(
    String title,
    String authorName,
    String isbn13,
    String coverUrl,
    Integer publishedYear,
    Integer pageCount) {}
