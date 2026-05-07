package com.bookhub.library.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.bookhub.library.application.error.CatalogIntegrationException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class CatalogServiceClientTest {

  @Mock private ServiceTokenProvider serviceTokenProvider;

  private static final UUID BOOK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final String CATALOG_BASE_URL = "http://localhost:19999";
  private static final String INTERNAL_BOOKS_URI =
      CATALOG_BASE_URL + "/api/v1/internal/books/00000000-0000-0000-0000-000000000001";

  private MockRestServiceServer mockServer;
  private CatalogServiceClient catalogServiceClient;

  @BeforeEach
  void setUp() {
    final RestClient.Builder builder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(builder).build();
    catalogServiceClient = new CatalogServiceClient(CATALOG_BASE_URL, builder, serviceTokenProvider);
  }

  @Test
  @DisplayName("Should forward service token as Bearer header in internal catalog requests")
  void shouldForwardServiceTokenAsBearerHeader() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("svc-jwt-token");

    final String responseBody =
        """
        {"bookId":"00000000-0000-0000-0000-000000000001","title":"Test Book","coverUrl":"https://example.com/cover.jpg","pageCount":300}
        """;

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token"))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    final Optional<CatalogBook> result = catalogServiceClient.findBookById(BOOK_ID);

    assertThat(result).isPresent();
    assertThat(result.get().bookId()).isEqualTo(BOOK_ID);
    assertThat(result.get().title()).isEqualTo("Test Book");
    mockServer.verify();
  }

  @Test
  @DisplayName("Should return empty when catalog returns 404")
  void shouldReturnEmptyWhenCatalogReturns404() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("svc-jwt-token");

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    final Optional<CatalogBook> result = catalogServiceClient.findBookById(BOOK_ID);

    assertThat(result).isEmpty();
    mockServer.verify();
  }

  @Test
  @DisplayName("Should propagate catalog service error when service returns 5xx")
  void shouldPropagateCatalogError() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("svc-jwt-token");

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token"))
        .andRespond(withServerError());

    assertThatThrownBy(() -> catalogServiceClient.findBookById(BOOK_ID))
        .isInstanceOf(CatalogIntegrationException.class)
        .hasMessageContaining("Catalog service returned status 500");
  }

  @Test
  @DisplayName("Should throw when service token cannot be acquired")
  void shouldThrowWhenServiceTokenCannotBeAcquired() {
    when(serviceTokenProvider.getServiceToken())
        .thenThrow(new ServiceTokenAcquisitionException("Failed to acquire service token"));

    assertThatThrownBy(() -> catalogServiceClient.findBookById(BOOK_ID))
        .isInstanceOf(ServiceTokenAcquisitionException.class)
        .hasMessageContaining("Failed to acquire service token");
  }

  @Test
  @DisplayName("Should NOT forward user JWT: only service token Authorization header present")
  void shouldNotForwardUserJwtWhenCallingInternalCatalog() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("svc-jwt-token");

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(
            request -> {
              var authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
              assertThat(authHeaders)
                  .as(
                      "User JWT MUST NOT be forwarded: must carry exactly one Authorization header (the service token)")
                  .hasSize(1);
              assertThat(authHeaders.getFirst())
                  .as("The only Authorization header value MUST be the service token, not a user JWT")
                  .isEqualTo("Bearer svc-jwt-token");
            })
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    catalogServiceClient.findBookById(BOOK_ID);
    mockServer.verify();
  }

  @Test
  @DisplayName("Should target internal catalog API path, proving non-internal/public behavior unchanged")
  void shouldTargetInternalCatalogApiPath() {
    when(serviceTokenProvider.getServiceToken()).thenReturn("svc-jwt-token");

    mockServer
        .expect(
            request -> {
              String path = request.getURI().getPath();
              assertThat(path)
                  .as(
                      "Must target internal API route (/api/v1/internal/), proving non-internal/public behavior is unchanged")
                  .startsWith("/api/v1/internal/");
            })
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    catalogServiceClient.findBookById(BOOK_ID);
    mockServer.verify();
  }

  @Test
  @DisplayName("Should invalidate cached token and retry once on 401")
  void shouldRetryOn401WithFreshToken() {
    when(serviceTokenProvider.getServiceToken())
        .thenReturn("svc-jwt-token-expired")
        .thenReturn("svc-jwt-token-fresh");

    final String responseBody =
        """
        {"bookId":"00000000-0000-0000-0000-000000000001","title":"Retried Book","coverUrl":"https://example.com/cover.jpg","pageCount":150}
        """;

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-expired"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-fresh"))
        .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

    final Optional<CatalogBook> result = catalogServiceClient.findBookById(BOOK_ID);

    assertThat(result).isPresent();
    assertThat(result.get().title()).isEqualTo("Retried Book");
    verify(serviceTokenProvider, times(2)).getServiceToken();
    mockServer.verify();
  }

  @Test
  @DisplayName("Should invalidate cached token and retry once on 403")
  void shouldRetryOn403WithFreshToken() {
    when(serviceTokenProvider.getServiceToken())
        .thenReturn("svc-jwt-token-expired")
        .thenReturn("svc-jwt-token-fresh");

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-expired"))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-fresh"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    final Optional<CatalogBook> result = catalogServiceClient.findBookById(BOOK_ID);

    assertThat(result).isPresent();
    verify(serviceTokenProvider, times(2)).getServiceToken();
    mockServer.verify();
  }

  @Test
  @DisplayName("Should propagate error after retry when fresh token also gets 401")
  void shouldFailAfterRetryWhenFreshTokenAlsoRejected() {
    when(serviceTokenProvider.getServiceToken())
        .thenReturn("svc-jwt-token-1")
        .thenReturn("svc-jwt-token-2");

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-1"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-2"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> catalogServiceClient.findBookById(BOOK_ID))
        .isInstanceOf(CatalogIntegrationException.class)
        .hasMessageContaining("Catalog service rejected service token after retry");

    verify(serviceTokenProvider, times(2)).getServiceToken();
    mockServer.verify();
  }

  @Test
  @DisplayName("Should throw when service token cannot be acquired on retry")
  void shouldThrowWhenFreshTokenAcquisitionFails() {
    when(serviceTokenProvider.getServiceToken())
        .thenReturn("svc-jwt-token-1")
        .thenThrow(new ServiceTokenAcquisitionException("Failed to acquire fresh token"));

    mockServer
        .expect(requestTo(INTERNAL_BOOKS_URI))
        .andExpect(header("Authorization", "Bearer svc-jwt-token-1"))
        .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

    assertThatThrownBy(() -> catalogServiceClient.findBookById(BOOK_ID))
        .isInstanceOf(ServiceTokenAcquisitionException.class)
        .hasMessageContaining("Failed to acquire fresh token");

    verify(serviceTokenProvider, times(2)).getServiceToken();
    mockServer.verify();
  }
}
