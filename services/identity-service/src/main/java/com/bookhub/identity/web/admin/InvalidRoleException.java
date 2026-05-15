package com.bookhub.identity.web.admin;

public class InvalidRoleException extends RuntimeException {

  public InvalidRoleException(final String role) {
    super("Invalid role value: '" + role + "'. Accepted values: USER, ADMIN");
  }
}
