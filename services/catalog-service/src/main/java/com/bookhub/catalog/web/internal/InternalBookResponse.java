package com.bookhub.catalog.web.internal;

import java.util.UUID;

public record InternalBookResponse(
    UUID bookId, String title, String coverUrl, Integer pageCount, boolean exists, String status) {}
