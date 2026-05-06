package com.bookhub.library.infrastructure.client;

import java.util.UUID;

public record CatalogBook(UUID bookId, String title, String coverUrl, Integer pageCount) {}
