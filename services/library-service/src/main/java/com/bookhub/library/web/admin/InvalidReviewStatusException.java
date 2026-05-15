package com.bookhub.library.web.admin;

public class InvalidReviewStatusException extends RuntimeException {

  public InvalidReviewStatusException(final String status) {
    super("Invalid review status: " + status);
  }
}
