package com.bookhub.library.application;

import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateReadingProgressService {

  private final UserBookRepository userBookRepository;

  @Transactional
  public UserBook execute(final UUID userId, final UUID entryId, final int pagesRead) {
    log.info(
        "Updating reading progress entryId={} userId={} pagesRead={}", entryId, userId, pagesRead);

    final UserBook userBook =
        userBookRepository
            .findById(entryId)
            .orElseThrow(
                () -> new LibraryEntryNotFoundException("Library entry not found: " + entryId));

    if (!userBook.isOwnedBy(userId)) {
      throw new LibraryEntryOwnershipException("User " + userId + " does not own entry " + entryId);
    }

    userBook.updateProgress(pagesRead);
    return userBookRepository.save(userBook);
  }
}
