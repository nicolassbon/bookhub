package com.bookhub.library.infrastructure.client;

public class ServiceTokenAcquisitionException extends RuntimeException {

  public ServiceTokenAcquisitionException(final String message) {
    super(message);
  }

  public ServiceTokenAcquisitionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
