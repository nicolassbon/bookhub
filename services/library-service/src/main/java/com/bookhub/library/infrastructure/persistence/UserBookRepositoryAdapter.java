package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserBookRepositoryAdapter implements UserBookRepository {

  private final JpaUserBookRepository jpaUserBookRepository;
  private final UserBookEntityMapper userBookEntityMapper;

  public UserBookRepositoryAdapter(
      final JpaUserBookRepository jpaUserBookRepository,
      final UserBookEntityMapper userBookEntityMapper) {
    this.jpaUserBookRepository = jpaUserBookRepository;
    this.userBookEntityMapper = userBookEntityMapper;
  }

  @Override
  public UserBook save(final UserBook userBook) {
    final UserBookEntity entity = userBookEntityMapper.toEntity(userBook);
    final UserBookEntity saved = jpaUserBookRepository.save(entity);
    return userBookEntityMapper.toDomain(saved);
  }

  @Override
  public Optional<UserBook> findById(final UUID id) {
    return jpaUserBookRepository.findById(id).map(userBookEntityMapper::toDomain);
  }

  @Override
  public Optional<UserBook> findByUserIdAndBookId(final UUID userId, final UUID bookId) {
    return jpaUserBookRepository
        .findByUserIdAndBookId(userId, bookId)
        .map(userBookEntityMapper::toDomain);
  }

  @Override
  public List<UserBook> findByUserId(final UUID userId) {
    return jpaUserBookRepository.findByUserId(userId).stream()
        .map(userBookEntityMapper::toDomain)
        .toList();
  }

  @Override
  public List<UserBook> findByUserIdAndState(final UUID userId, final ReadingState state) {
    return jpaUserBookRepository.findByUserIdAndState(userId, state).stream()
        .map(userBookEntityMapper::toDomain)
        .toList();
  }

  @Override
  public long countByUserId(final UUID userId) {
    return jpaUserBookRepository.countByUserId(userId);
  }

  @Override
  public long countByUserIdAndState(final UUID userId, final ReadingState state) {
    return jpaUserBookRepository.countByUserIdAndState(userId, state);
  }
}
