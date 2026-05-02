package com.bookhub.library.application.error;

public class DuplicateLibraryEntryException extends RuntimeException {
  public DuplicateLibraryEntryException(final String message) {
    super(message);
  }
}
