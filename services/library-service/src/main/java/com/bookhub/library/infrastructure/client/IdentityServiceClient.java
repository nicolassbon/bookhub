package com.bookhub.library.infrastructure.client;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class IdentityServiceClient {

  private final RestClient restClient;

  public IdentityServiceClient(
      @Value("${identity.service.url}") final String identityServiceUrl,
      final RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(identityServiceUrl).build();
  }

  public IssuedServiceToken requestServiceToken(final String clientId, final String clientSecret) {
    final String authHeader =
        "Basic "
            + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

    return restClient
        .post()
        .uri("/api/v1/auth/service-token")
        .header(HttpHeaders.AUTHORIZATION, authHeader)
        .contentType(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(IssuedServiceToken.class);
  }
}
