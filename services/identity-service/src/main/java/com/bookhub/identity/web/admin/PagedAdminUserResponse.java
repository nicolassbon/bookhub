package com.bookhub.identity.web.admin;

import java.util.List;

public record PagedAdminUserResponse(
    List<AdminUserResponse> items,
    int page,
    int size,
    long totalElements,
    long totalPages) {}
