package com.bookhub.library.application.error;

public class ReviewNotFoundException extends RuntimeException {

  public ReviewNotFoundException(final String reviewId) {
    super("Review not found: " + reviewId);
  }
}
