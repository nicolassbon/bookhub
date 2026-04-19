package com.bookhub.identity.web;

import java.time.Instant;
import java.util.List;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
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
        return createAccessToken(
                jwtEncoder,
                subject,
                username,
                displayName,
                role,
                email,
                issuedAt,
                expiresAt,
                "bookhub-identity",
                "bookhub-api");
    }

    public static String createAccessToken(
            final JwtEncoder jwtEncoder,
            final String subject,
            final String username,
            final String displayName,
            final String role,
            final String email,
            final Instant issuedAt,
            final Instant expiresAt,
            final String issuer,
            final String audience) {
        final JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(subject)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .issuer(issuer)
                .audience(List.of(audience))
                .claim("username", username)
                .claim("displayName", displayName)
                .claim("role", role)
                .claim("email", email)
                .build();

        final JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
