package com.bookhub.library.application.error;

public class LibraryEntryNotFoundException extends RuntimeException {
  public LibraryEntryNotFoundException(final String message) {
    super(message);
  }
}
