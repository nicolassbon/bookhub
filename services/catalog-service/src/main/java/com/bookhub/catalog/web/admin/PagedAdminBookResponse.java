package com.bookhub.catalog.web.admin;

import java.util.List;

public record PagedAdminBookResponse(
    List<AdminBookResponse> items,
    int page,
    int size,
    long totalElements,
    long totalPages) {}
