package com.bookhub.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findBySourceReference(String sourceReference);

    Optional<Book> findById(UUID id);

    List<Book> searchByQuery(String query, int candidateLimit);
}
