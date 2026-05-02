package com.bookhub.library.infrastructure.persistence;

import com.bookhub.library.domain.BookSnapshot;
import com.bookhub.library.domain.UserBook;
import org.springframework.stereotype.Component;

@Component
public class UserBookEntityMapper {

  public UserBookEntity toEntity(final UserBook userBook) {
    final UserBookEntity entity = new UserBookEntity();
    entity.setId(userBook.getId());
    entity.setUserId(userBook.getUserId());
    entity.setBookId(userBook.getBookId());
    entity.setBookTitle(userBook.getBook().title());
    entity.setBookCoverUrl(userBook.getBook().coverUrl());
    entity.setBookPageCount(userBook.getBook().pageCount());
    entity.setState(userBook.getState());
    entity.setPagesRead(userBook.getPagesRead());
    entity.setPercentage(userBook.getPercentage());
    entity.setStartedAt(userBook.getStartedAt());
    entity.setFinishedAt(userBook.getFinishedAt());
    entity.setAddedAt(userBook.getAddedAt());
    entity.setLastProgressAt(userBook.getLastProgressAt());
    return entity;
  }

  public UserBook toDomain(final UserBookEntity entity) {
    return UserBook.rehydrate(
        entity.getId(),
        entity.getUserId(),
        entity.getBookId(),
        new BookSnapshot(
            entity.getBookTitle(), entity.getBookCoverUrl(), entity.getBookPageCount()),
        entity.getState(),
        entity.getPagesRead(),
        entity.getPercentage(),
        entity.getStartedAt(),
        entity.getFinishedAt(),
        entity.getAddedAt(),
        entity.getLastProgressAt());
  }
}
