package com.bookhub.library.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserBookRepository {

  UserBook save(UserBook userBook);

  Optional<UserBook> findById(UUID id);

  Optional<UserBook> findByUserIdAndBookId(UUID userId, UUID bookId);

  List<UserBook> findByUserId(UUID userId);

  List<UserBook> findByUserIdAndState(UUID userId, ReadingState state);

  long countByUserId(UUID userId);

  long countByUserIdAndState(UUID userId, ReadingState state);
}
