package com.bookhub.catalog.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaBookRepository extends JpaRepository<BookEntity, UUID> {

    Optional<BookEntity> findBySourceReference(String sourceReference);

    @Query(value = """
            SELECT b.*
            FROM books b
            WHERE b.title ILIKE CONCAT('%', :query, '%')
               OR COALESCE(b.author_name, '') ILIKE CONCAT('%', :query, '%')
               OR COALESCE(b.isbn13, '') ILIKE CONCAT('%', :query, '%')
            ORDER BY b.title ASC
            LIMIT :candidateLimit
            """, nativeQuery = true)
    java.util.List<BookEntity> searchByQuery(String query, int candidateLimit);
}
