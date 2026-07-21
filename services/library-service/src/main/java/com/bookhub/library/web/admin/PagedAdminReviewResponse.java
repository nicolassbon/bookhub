package com.bookhub.library.web.admin;

import java.util.List;

public record PagedAdminReviewResponse(
    List<AdminReviewResponse> items, int page, int size, long totalElements, long totalPages) {}
