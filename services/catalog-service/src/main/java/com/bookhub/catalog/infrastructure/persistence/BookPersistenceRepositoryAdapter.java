package com.bookhub.catalog.infrastructure.persistence;

import com.bookhub.catalog.domain.Book;
import com.bookhub.catalog.domain.BookRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class BookPersistenceRepositoryAdapter implements BookRepository {

  private final JpaBookRepository jpaBookRepository;
  private final BookEntityMapper bookEntityMapper;

  public BookPersistenceRepositoryAdapter(
      final JpaBookRepository jpaBookRepository, final BookEntityMapper bookEntityMapper) {
    this.jpaBookRepository = jpaBookRepository;
    this.bookEntityMapper = bookEntityMapper;
  }

  @Override
  public Book save(final Book book) {
    final BookEntity saved = jpaBookRepository.saveAndFlush(bookEntityMapper.toEntity(book));
    return bookEntityMapper.toDomain(saved);
  }

  @Override
  public Optional<Book> findBySourceReference(final String sourceReference) {
    return jpaBookRepository.findBySourceReference(sourceReference).map(bookEntityMapper::toDomain);
  }

  @Override
  public Optional<Book> findById(final UUID id) {
    return jpaBookRepository.findById(id).map(bookEntityMapper::toDomain);
  }

  @Override
  public List<Book> searchByQuery(final String query, final int candidateLimit) {
    return jpaBookRepository.searchByQuery(query, candidateLimit).stream()
        .map(bookEntityMapper::toDomain)
        .toList();
  }
}
