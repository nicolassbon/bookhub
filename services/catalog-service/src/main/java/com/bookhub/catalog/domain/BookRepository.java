package com.bookhub.catalog.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findBySourceReference(String sourceReference);

    Optional<Book> findById(UUID id);

    java.util.List<Book> searchByQuery(String query, Pageable pageable);
}
