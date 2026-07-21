package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.ReadingState;
import java.time.Instant;
import java.util.UUID;

public final class UserBookEntityTestFactory {

  private UserBookEntityTestFactory() {}

  public static UserBookEntity create(
      final UUID userId, final UUID bookId, final ReadingState state, final Instant now) {
    final UserBookEntity entity = new UserBookEntity();
    entity.setUserId(userId);
    entity.setBookId(bookId);
    entity.setBookTitle("Test Book");
    entity.setState(state);
    entity.setAddedAt(now);
    entity.setCreatedAt(now);
    entity.setUpdatedAt(now);
    return entity;
  }
}
