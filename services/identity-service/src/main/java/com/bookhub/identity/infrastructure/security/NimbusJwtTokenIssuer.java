package com.bookhub.identity.infrastructure.security;

import com.bookhub.identity.application.auth.TokenIssuer;
import com.bookhub.identity.domain.user.User;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class NimbusJwtTokenIssuer implements TokenIssuer {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public NimbusJwtTokenIssuer(
            final JwtEncoder jwtEncoder,
            @Value("${jwt.expiration}") final long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public IssuedTokenPair issueFor(final User user) {
        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", user.getUsername())
                .claim("displayName", user.getDisplayName())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .build();

        final JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        final String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return IssuedTokenPair.builder()
                .accessToken(accessToken)
                .expiresIn(expirationSeconds)
                .build();
    }
}
