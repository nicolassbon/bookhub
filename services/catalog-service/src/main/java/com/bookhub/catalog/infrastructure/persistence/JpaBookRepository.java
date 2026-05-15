package com.bookhub.catalog.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaBookRepository extends JpaRepository<BookEntity, UUID> {

  Optional<BookEntity> findBySourceReference(String sourceReference);

  Page<BookEntity> findAllBySource(String source, Pageable pageable);

  long countBySource(String source);

  @Query(
      value =
          """
            SELECT b.*
            FROM books b
            WHERE b.title ILIKE CONCAT('%', :query, '%')
               OR COALESCE(b.author_name, '') ILIKE CONCAT('%', :query, '%')
               OR COALESCE(b.isbn13, '') ILIKE CONCAT('%', :query, '%')
            ORDER BY b.title ASC
            LIMIT :candidateLimit
            """,
      nativeQuery = true)
  java.util.List<BookEntity> searchByQuery(String query, int candidateLimit);
}
