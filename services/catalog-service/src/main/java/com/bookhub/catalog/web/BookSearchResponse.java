package com.bookhub.catalog.web;

import lombok.Builder;

@Builder
public record BookSearchResponse(String id, String title, String authorName, String coverUrl) {}
