package com.bookhub.catalog.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

  Book save(Book book);

  Optional<Book> findBySourceReference(String sourceReference);

  Optional<Book> findById(UUID id);

  List<Book> searchByQuery(String query, int candidateLimit);

  List<Book> findAll(int page, int size);

  List<Book> findAll(int page, int size, String source);

  long countAll();

  long countAll(String source);
}

