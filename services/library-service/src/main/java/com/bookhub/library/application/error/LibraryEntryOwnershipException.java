package com.bookhub.library.application.error;

public class LibraryEntryOwnershipException extends RuntimeException {
  public LibraryEntryOwnershipException(final String message) {
    super(message);
  }
}
