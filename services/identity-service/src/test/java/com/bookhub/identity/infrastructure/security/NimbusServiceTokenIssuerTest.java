package com.bookhub.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.application.auth.TokenIssuer.IssuedTokenPair;
import com.bookhub.identity.config.JwtProperties;
import com.bookhub.identity.domain.auth.ServicePrincipal;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class NimbusServiceTokenIssuerTest {

  @Mock private JwtEncoder jwtEncoder;

  @Test
  @DisplayName("Should issue token with ROLE_SERVICE and no human claims for a service principal")
  void shouldIssueTokenWithRoleServiceAndNoHumanClaims() {
    final NimbusServiceTokenIssuer tokenIssuer =
        new NimbusServiceTokenIssuer(jwtEncoder, jwtProperties(3600));

    final ServicePrincipal principal = new ServicePrincipal("library-service");

    when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(
            Jwt.withTokenValue("svc-jwt-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("library-service")
                .claim("role", "SERVICE")
                .build());

    final IssuedTokenPair issuedToken = tokenIssuer.issueFor(principal);

    final ArgumentCaptor<JwtEncoderParameters> parametersCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    final JwtClaimsSet claims = parametersCaptor.getValue().getClaims();
    final Object algorithm = parametersCaptor.getValue().getJwsHeader().getHeaders().get("alg");

    assertThat(issuedToken.accessToken()).isEqualTo("svc-jwt-token");
    assertThat(issuedToken.expiresIn()).isEqualTo(3600);
    assertThat(claims.getSubject()).isEqualTo("library-service");
    assertThat((String) claims.getClaim("role")).isEqualTo("SERVICE");
    assertThat(claims.getClaims()).doesNotContainKey("username");
    assertThat(claims.getClaims()).doesNotContainKey("email");
    assertThat(claims.getClaims()).doesNotContainKey("displayName");
    assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
    assertThat((String) claims.getClaims().get("iss")).isEqualTo("bookhub-identity");
    assertThat(claims.getAudience()).containsExactly("bookhub-api");
    assertThat(algorithm).isEqualTo(SignatureAlgorithm.RS256);
  }

  @Test
  @DisplayName(
      "Should issue token with correct expiration for service principal with different TTL")
  void shouldIssueTokenWithCorrectExpirationForDifferentTtl() {
    final NimbusServiceTokenIssuer tokenIssuer =
        new NimbusServiceTokenIssuer(jwtEncoder, jwtProperties(1800));

    final ServicePrincipal principal = new ServicePrincipal("catalog-service");

    when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(
            Jwt.withTokenValue("svc-jwt-token-2")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .subject("catalog-service")
                .claim("role", "ROLE_SERVICE")
                .build());

    final IssuedTokenPair issuedToken = tokenIssuer.issueFor(principal);

    assertThat(issuedToken.accessToken()).isEqualTo("svc-jwt-token-2");
    assertThat(issuedToken.expiresIn()).isEqualTo(1800);
  }

  private JwtProperties jwtProperties(final long expirationSeconds) {
    return new JwtProperties(
        "bookhub-identity",
        "bookhub-api",
        expirationSeconds,
        new JwtProperties.Rsa("ignored", "ignored"));
  }
}
