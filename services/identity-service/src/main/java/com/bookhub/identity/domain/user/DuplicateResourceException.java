package com.bookhub.identity.domain.user;

public class DuplicateResourceException extends RuntimeException {

  private final String resource;

  public DuplicateResourceException(final String resource, final String message) {
    super(message);
    this.resource = resource;
  }

  public String getResource() {
    return resource;
  }
}
