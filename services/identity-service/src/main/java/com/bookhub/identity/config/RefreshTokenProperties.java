package com.bookhub.identity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenProperties {

  private final long expirationSeconds;
  private final String cookieName;
  private final String cookiePath;
  private final String cookieSameSite;
  private final boolean cookieSecure;

  public RefreshTokenProperties(
      @Value("${auth.refresh-token.expiration-seconds:604800}") final long expirationSeconds,
      @Value("${auth.refresh-token.cookie.name:refresh_token}") final String cookieName,
      @Value("${auth.refresh-token.cookie.path:/api/v1/auth}") final String cookiePath,
      @Value("${auth.refresh-token.cookie.same-site:Strict}") final String cookieSameSite,
      @Value("${auth.refresh-token.cookie.secure:true}") final boolean cookieSecure) {
    this.expirationSeconds = expirationSeconds;
    this.cookieName = cookieName;
    this.cookiePath = cookiePath;
    this.cookieSameSite = cookieSameSite;
    this.cookieSecure = cookieSecure;
  }

  public long expirationSeconds() {
    return expirationSeconds;
  }

  public String cookieName() {
    return cookieName;
  }

  public String cookiePath() {
    return cookiePath;
  }

  public String cookieSameSite() {
    return cookieSameSite;
  }

  public boolean cookieSecure() {
    return cookieSecure;
  }
}
