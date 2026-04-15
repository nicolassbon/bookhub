package com.bookhub.catalog.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaBookRepository extends JpaRepository<BookEntity, UUID> {

    Optional<BookEntity> findBySourceReference(String sourceReference);

    @Query("""
            SELECT b
            FROM BookEntity b
            WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(COALESCE(b.authorName, '')) LIKE LOWER(CONCAT('%', :query, '%'))
               OR COALESCE(b.isbn13, '') LIKE CONCAT('%', :query, '%')
            """)
    java.util.List<BookEntity> searchByQuery(String query, Pageable pageable);
}
