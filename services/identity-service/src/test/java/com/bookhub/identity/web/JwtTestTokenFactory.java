package com.bookhub.identity.web;

import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

public final class JwtTestTokenFactory {

    private JwtTestTokenFactory() {
    }

    public static String createAccessToken(
            final JwtEncoder jwtEncoder,
            final String subject,
            final String username,
            final String displayName,
            final String role,
            final String email,
            final Instant issuedAt,
            final Instant expiresAt) {
        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", username)
                .claim("displayName", displayName)
                .claim("role", role)
                .claim("email", email)
                .build();

        final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
