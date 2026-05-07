package com.bookhub.library.infrastructure.client;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
public class ServiceTokenProvider {

  private static final long EXPIRY_BUFFER_SECONDS = 30;

  private final IdentityServiceClient identityServiceClient;
  private final String clientId;
  private final String clientSecret;
  private volatile String cachedToken;
  private volatile Instant tokenExpiresAt;

  public ServiceTokenProvider(
      final IdentityServiceClient identityServiceClient,
      @Value("${service.client-id}") final String clientId,
      @Value("${service.client-secret}") final String clientSecret) {
    this.identityServiceClient = identityServiceClient;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public String getServiceToken() {
    if (cachedToken != null && !isExpired()) {
      return cachedToken;
    }

    try {
      final IssuedServiceToken response =
          identityServiceClient.requestServiceToken(clientId, clientSecret);

      if (response == null || response.accessToken() == null) {
        throw new ServiceTokenAcquisitionException("Failed to acquire service token");
      }

      cachedToken = response.accessToken();
      tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn());
      return cachedToken;
    } catch (ServiceTokenAcquisitionException e) {
      throw e;
    } catch (RestClientException e) {
      throw new ServiceTokenAcquisitionException("Failed to acquire service token", e);
    }
  }

  void invalidateToken() {
    this.cachedToken = null;
    this.tokenExpiresAt = null;
  }

  private boolean isExpired() {
    return tokenExpiresAt == null
        || Instant.now().isAfter(tokenExpiresAt.minusSeconds(EXPIRY_BUFFER_SECONDS));
  }
}
