package com.bookhub.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookhub.identity.config.JwtProperties;
import com.bookhub.identity.domain.user.User;
import com.bookhub.identity.domain.user.UserRole;
import java.time.Instant;
import java.util.UUID;
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
class NimbusJwtTokenIssuerTest {

  @Mock private JwtEncoder jwtEncoder;

  @Test
  @DisplayName(
      "Should issue RS256 access token with expected user claims and configured expiration")
  void shouldIssueRs256AccessTokenWithExpectedUserClaimsAndConfiguredExpiration() {
    final NimbusJwtTokenIssuer tokenIssuer =
        new NimbusJwtTokenIssuer(jwtEncoder, jwtProperties(3600));

    final User user =
        User.rehydrate(
            UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
            "nico",
            "nico@example.com",
            "hash",
            "Nicolas Bon",
            UserRole.USER);

    when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(
            Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .subject("6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .claim("username", "nico")
                .claim("displayName", "Nicolas Bon")
                .claim("role", "USER")
                .claim("email", "nico@example.com")
                .build());

    final var issuedToken = tokenIssuer.issueFor(user);

    final ArgumentCaptor<JwtEncoderParameters> parametersCaptor =
        ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    final JwtClaimsSet claims = parametersCaptor.getValue().getClaims();
    final Object algorithm = parametersCaptor.getValue().getJwsHeader().getHeaders().get("alg");

    assertThat(issuedToken.accessToken()).isEqualTo("jwt-token");
    assertThat(issuedToken.expiresIn()).isEqualTo(3600);
    assertThat(claims.getSubject()).isEqualTo("6676f2d8-0f65-40ae-b102-66145e24f3fd");
    assertThat((String) claims.getClaim("username")).isEqualTo("nico");
    assertThat((String) claims.getClaim("displayName")).isEqualTo("Nicolas Bon");
    assertThat((String) claims.getClaim("role")).isEqualTo("USER");
    assertThat((String) claims.getClaim("email")).isEqualTo("nico@example.com");
    assertThat(claims.getExpiresAt()).isAfter(claims.getIssuedAt());
    assertThat((String) claims.getClaims().get("iss")).isEqualTo("bookhub-identity");
    assertThat(claims.getAudience()).containsExactly("bookhub-api");
    assertThat(algorithm).isEqualTo(SignatureAlgorithm.RS256);
  }

  @Test
  @DisplayName("Should not include refresh token in issued contract")
  void shouldNotIncludeRefreshTokenInIssuedContract() {
    final NimbusJwtTokenIssuer tokenIssuer =
        new NimbusJwtTokenIssuer(jwtEncoder, jwtProperties(1800));

    final User user =
        User.rehydrate(
            UUID.fromString("6676f2d8-0f65-40ae-b102-66145e24f3fd"),
            "nico",
            "nico@example.com",
            "hash",
            "Nicolas Bon",
            UserRole.USER);

    when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
        .thenReturn(
            Jwt.withTokenValue("jwt-token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1800))
                .subject("6676f2d8-0f65-40ae-b102-66145e24f3fd")
                .build());

    final var issuedToken = tokenIssuer.issueFor(user);

    assertThat(issuedToken.accessToken()).isNotBlank();
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
