package com.bookhub.identity.web.auth;

import com.bookhub.identity.application.auth.InvalidServiceCredentialsException;
import com.bookhub.identity.application.auth.ServiceTokenIssuer;
import com.bookhub.identity.application.auth.TokenIssuer.IssuedTokenPair;
import com.bookhub.identity.domain.auth.ServicePrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class ServiceAuthController {

  private static final String BASIC_PREFIX = "Basic ";
  private static final int BASIC_PREFIX_LENGTH = 6;

  private final ServiceTokenIssuer serviceTokenIssuer;
  private final String expectedClientId;
  private final String expectedClientSecret;

  public ServiceAuthController(
      final ServiceTokenIssuer serviceTokenIssuer,
      @Value("${service.client-id}") final String expectedClientId,
      @Value("${service.client-secret}") final String expectedClientSecret) {
    this.serviceTokenIssuer = serviceTokenIssuer;
    this.expectedClientId = expectedClientId;
    this.expectedClientSecret = expectedClientSecret;
  }

  @PostMapping("/service-token")
  public ResponseEntity<IssuedTokenPair> issueServiceToken(
      @RequestHeader(value = "Authorization", required = false) final String authHeader) {

    // HTTP Basic auth scheme is case-insensitive per RFC 7235
    if (authHeader == null
        || !authHeader.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX_LENGTH)) {
      throw new InvalidServiceCredentialsException();
    }

    final String base64Credentials = authHeader.substring(BASIC_PREFIX.length());
    final String credentials;
    try {
      credentials =
          new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException e) {
      throw new InvalidServiceCredentialsException();
    }

    final String[] parts = credentials.split(":", 2);
    if (parts.length != 2) {
      throw new InvalidServiceCredentialsException();
    }

    final String clientId = parts[0];
    final String clientSecret = parts[1];

    if (!MessageDigest.isEqual(
            expectedClientId.getBytes(StandardCharsets.UTF_8),
            clientId.getBytes(StandardCharsets.UTF_8))
        || !MessageDigest.isEqual(
            expectedClientSecret.getBytes(StandardCharsets.UTF_8),
            clientSecret.getBytes(StandardCharsets.UTF_8))) {
      throw new InvalidServiceCredentialsException();
    }

    final ServicePrincipal principal = new ServicePrincipal(clientId);
    final IssuedTokenPair tokenPair = serviceTokenIssuer.issueFor(principal);

    return ResponseEntity.ok(tokenPair);
  }
}
