package com.bookhub.library.application.error;

public class YearlyGoalNotFoundException extends RuntimeException {

  public YearlyGoalNotFoundException(final String message) {
    super(message);
  }
}
