package com.bookhub.identity.application.auth;

public class InvalidServiceCredentialsException extends RuntimeException {

  public InvalidServiceCredentialsException() {
    super("Invalid service credentials");
  }
}
