package com.bookhub.library.application;

import com.bookhub.library.application.error.LibraryEntryNotFoundException;
import com.bookhub.library.application.error.LibraryEntryOwnershipException;
import com.bookhub.library.domain.ReadingState;
import com.bookhub.library.domain.UserBook;
import com.bookhub.library.domain.UserBookRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserLibraryService {

  private final UserBookRepository userBookRepository;

  @Transactional(readOnly = true)
  public List<UserBook> getLibraryEntries(final UUID userId) {
    return userBookRepository.findByUserId(userId);
  }

  @Transactional(readOnly = true)
  public List<UserBook> getLibraryEntriesByState(final UUID userId, final ReadingState state) {
    return userBookRepository.findByUserIdAndState(userId, state);
  }

  @Transactional(readOnly = true)
  public UserBook getLibraryEntry(final UUID userId, final UUID entryId) {
    final UserBook userBook =
        userBookRepository
            .findById(entryId)
            .orElseThrow(
                () -> new LibraryEntryNotFoundException("Library entry not found: " + entryId));

    if (!userBook.isOwnedBy(userId)) {
      throw new LibraryEntryOwnershipException("User " + userId + " does not own entry " + entryId);
    }

    return userBook;
  }

  @Transactional(readOnly = true)
  public LibrarySummary getLibrarySummary(final UUID userId) {
    final long total = userBookRepository.countByUserId(userId);
    final long wantToRead =
        userBookRepository.countByUserIdAndState(userId, ReadingState.WANT_TO_READ);
    final long reading = userBookRepository.countByUserIdAndState(userId, ReadingState.READING);
    final long read = userBookRepository.countByUserIdAndState(userId, ReadingState.READ);
    return new LibrarySummary(total, wantToRead, reading, read);
  }

  public record LibrarySummary(long total, long wantToRead, long reading, long read) {}
}
