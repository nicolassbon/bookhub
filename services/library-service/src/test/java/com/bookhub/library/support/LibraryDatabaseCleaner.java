package com.bookhub.library.support;

import com.bookhub.library.infrastructure.persistence.JpaReviewRepository;
import com.bookhub.library.infrastructure.persistence.JpaUserBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LibraryDatabaseCleaner {

  private final JpaUserBookRepository jpaUserBookRepository;
  private final JpaReviewRepository jpaReviewRepository;

  public void clean() {
    jpaReviewRepository.deleteAll();
    jpaUserBookRepository.deleteAll();
  }
}
