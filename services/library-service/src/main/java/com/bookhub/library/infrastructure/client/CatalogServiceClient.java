package com.bookhub.library.infrastructure.client;

import com.bookhub.library.application.error.CatalogIntegrationException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CatalogServiceClient {

  private final RestClient restClient;

  public CatalogServiceClient(
      @Value("${catalog.service.base-url}") final String catalogBaseUrl,
      final RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl(catalogBaseUrl).build();
  }

  public Optional<CatalogBook> findBookById(final UUID bookId) {
    try {
      return Optional.ofNullable(
          restClient
              .get()
              .uri("/api/v1/internal/books/{bookId}", bookId)
              .retrieve()
              .onStatus(
                  statusCode -> statusCode.value() == 404,
                  (request, response) -> {
                    throw new CatalogBookNotFoundException();
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
}
