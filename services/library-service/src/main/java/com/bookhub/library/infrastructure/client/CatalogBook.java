package com.bookhub.library.infrastructure.client;

import java.util.UUID;

/** Read-only snapshot of a book from catalog-service's internal endpoint. */
public record CatalogBook(UUID bookId, String title, String coverUrl, Integer pageCount) {}
