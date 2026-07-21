package com.bookhub.library.infrastructure.client;

import com.bookhub.library.application.error.CatalogIntegrationException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CatalogServiceClient {

  private final RestClient restClient;
  private final ServiceTokenProvider serviceTokenProvider;

  public CatalogServiceClient(
      @Value("${catalog.service.base-url}") final String catalogBaseUrl,
      final RestClient.Builder restClientBuilder,
      final ServiceTokenProvider serviceTokenProvider) {
    this.restClient = restClientBuilder.baseUrl(catalogBaseUrl).build();
    this.serviceTokenProvider = serviceTokenProvider;
  }

  public Optional<CatalogBook> findBookById(final UUID bookId) {
    final String serviceToken = serviceTokenProvider.getServiceToken();
    try {
      return executeRequest(bookId, serviceToken);
    } catch (ServiceTokenRejectedException exception) {
      serviceTokenProvider.invalidateToken();
      final String freshToken = serviceTokenProvider.getServiceToken();
      try {
        return executeRequest(bookId, freshToken);
      } catch (ServiceTokenRejectedException retryException) {
        throw new CatalogIntegrationException(
            "Catalog service rejected service token after retry", retryException);
      }
    }
  }

  private Optional<CatalogBook> executeRequest(final UUID bookId, final String serviceToken) {
    try {
      return Optional.ofNullable(
          restClient
              .get()
              .uri("/api/v1/internal/books/{bookId}", bookId)
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken)
              .retrieve()
              .onStatus(
                  statusCode -> statusCode.value() == 404,
                  (request, response) -> {
                    throw new CatalogBookNotFoundException();
                  })
              .onStatus(
                  statusCode -> statusCode.value() == 401 || statusCode.value() == 403,
                  (request, response) -> {
                    throw new ServiceTokenRejectedException();
                  })
              .onStatus(
                  HttpStatusCode::isError,
                  (request, response) -> {
                    throw new CatalogIntegrationException(
                        "Catalog service returned status " + response.getStatusCode().value());
                  })
              .body(CatalogBook.class));
    } catch (CatalogBookNotFoundException ignored) {
      return Optional.empty();
    } catch (ServiceTokenRejectedException exception) {
      throw exception;
    } catch (CatalogIntegrationException exception) {
      throw exception;
    } catch (RestClientResponseException exception) {
      throw new CatalogIntegrationException(
          "Catalog service returned unexpected status " + exception.getStatusCode().value(),
          exception);
    } catch (RestClientException exception) {
      throw new CatalogIntegrationException("Catalog service communication failure", exception);
    }
  }

  private static final class CatalogBookNotFoundException extends RuntimeException {}

  private static final class ServiceTokenRejectedException extends RuntimeException {
    ServiceTokenRejectedException() {
      super("Service token rejected by catalog");
    }
  }
}
