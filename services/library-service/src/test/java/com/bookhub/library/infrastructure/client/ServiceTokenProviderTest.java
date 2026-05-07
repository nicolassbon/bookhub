package com.bookhub.library.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class ServiceTokenProviderTest {

  @Mock private IdentityServiceClient identityServiceClient;

  private ServiceTokenProvider serviceTokenProvider;

  private static final IssuedServiceToken TOKEN_1 =
      new IssuedServiceToken("svc-jwt-token-1", 3600);
  private static final IssuedServiceToken TOKEN_2 =
      new IssuedServiceToken("svc-jwt-token-2", 3600);

  @BeforeEach
  void setUp() {
    serviceTokenProvider = new ServiceTokenProvider(identityServiceClient, "test-client", "test-secret");
  }

  @Test
  @DisplayName("Should cache token after first fetch and return cached on subsequent calls")
  void shouldCacheTokenAfterFirstFetch() {
    when(identityServiceClient.requestServiceToken("test-client", "test-secret"))
        .thenReturn(TOKEN_1);

    final String firstCall = serviceTokenProvider.getServiceToken();
    final String secondCall = serviceTokenProvider.getServiceToken();

    assertThat(firstCall).isEqualTo("svc-jwt-token-1");
    assertThat(secondCall).isEqualTo("svc-jwt-token-1");

    verify(identityServiceClient, times(1)).requestServiceToken("test-client", "test-secret");
  }

  @Test
  @DisplayName("Should refresh token when cached token is expired")
  void shouldRefreshTokenWhenExpired() {
    when(identityServiceClient.requestServiceToken("test-client", "test-secret"))
        .thenReturn(TOKEN_1)
        .thenReturn(TOKEN_2);

    serviceTokenProvider.getServiceToken();

    serviceTokenProvider.invalidateToken();

    final String refreshedToken = serviceTokenProvider.getServiceToken();

    assertThat(refreshedToken).isEqualTo("svc-jwt-token-2");

    verify(identityServiceClient, times(2)).requestServiceToken("test-client", "test-secret");
  }

  @Test
  @DisplayName("Should throw when identity service is unreachable")
  void shouldThrowWhenIdentityUnreachable() {
    when(identityServiceClient.requestServiceToken("test-client", "test-secret"))
        .thenThrow(new ResourceAccessException("Connection refused"));

    assertThatThrownBy(() -> serviceTokenProvider.getServiceToken())
        .isInstanceOf(ServiceTokenAcquisitionException.class)
        .hasMessageContaining("Failed to acquire service token");
  }

  @Test
  @DisplayName("Should throw when no token cached and attempt does not produce one")
  void shouldThrowWhenNoTokenCachedAndNullReturned() {
    when(identityServiceClient.requestServiceToken("test-client", "test-secret"))
        .thenReturn(null);

    assertThatThrownBy(() -> serviceTokenProvider.getServiceToken())
        .isInstanceOf(ServiceTokenAcquisitionException.class)
        .hasMessageContaining("Failed to acquire service token");
  }
}
