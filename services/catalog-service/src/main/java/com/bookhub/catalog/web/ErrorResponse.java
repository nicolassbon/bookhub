package com.bookhub.catalog.web;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp, int status, String error, String code, String message, String path) {}
