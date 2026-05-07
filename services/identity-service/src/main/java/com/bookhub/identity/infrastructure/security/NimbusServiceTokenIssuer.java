package com.bookhub.identity.infrastructure.security;

import com.bookhub.identity.application.auth.ServiceTokenIssuer;
import com.bookhub.identity.application.auth.TokenIssuer.IssuedTokenPair;
import com.bookhub.identity.config.JwtProperties;
import com.bookhub.identity.domain.auth.ServicePrincipal;
import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class NimbusServiceTokenIssuer implements ServiceTokenIssuer {

  private static final String ROLE_SERVICE = "SERVICE";

  private final JwtEncoder jwtEncoder;
  private final long expirationSeconds;
  private final String issuer;
  private final String audience;

  public NimbusServiceTokenIssuer(final JwtEncoder jwtEncoder, final JwtProperties jwtProperties) {
    this.jwtEncoder = jwtEncoder;
    this.expirationSeconds = jwtProperties.expiration();
    this.issuer = jwtProperties.issuer();
    this.audience = jwtProperties.audience();
  }

  @Override
  public IssuedTokenPair issueFor(final ServicePrincipal principal) {
    final Instant issuedAt = Instant.now();
    final Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

    final JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .subject(principal.clientId())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .issuer(issuer)
            .audience(List.of(audience))
            .claim("role", ROLE_SERVICE)
            .build();

    final JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();
    final String accessToken =
        jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    return IssuedTokenPair.builder().accessToken(accessToken).expiresIn(expirationSeconds).build();
  }
}
