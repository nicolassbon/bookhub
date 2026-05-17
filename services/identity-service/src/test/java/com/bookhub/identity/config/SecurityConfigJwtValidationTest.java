package com.bookhub.identity.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookhub.identity.support.PostgreSqlIntegrationTest;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@TestPropertySource(properties = "management.health.mail.enabled=false")
class SecurityConfigJwtValidationTest extends PostgreSqlIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private JwtEncoder jwtEncoder;

  @Test
  @DisplayName("Should authenticate RS256 token with valid issuer and audience")
  void shouldAcceptRs256Token() throws Exception {
    final JwtClaimsSet claims =
        baseClaimsBuilder()
            .issuer("bookhub-identity")
            .audience(java.util.List.of("bookhub-api"))
            .build();
    final String token =
        jwtEncoder
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Should reject legacy HS256 token")
  void shouldRejectLegacyHs256Token() throws Exception {
    final byte[] hmacKeyBytes = new byte[32];
    new SecureRandom().nextBytes(hmacKeyBytes);
    final NimbusJwtEncoder hsEncoder =
        new NimbusJwtEncoder(new ImmutableSecret<>(new SecretKeySpec(hmacKeyBytes, "HmacSHA256")));

    final JwtClaimsSet claims =
        baseClaimsBuilder()
            .issuer("bookhub-identity")
            .audience(java.util.List.of("bookhub-api"))
            .build();
    final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    final String token =
        hsEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should reject token with invalid issuer")
  void shouldRejectTokenWithInvalidIssuer() throws Exception {
    final JwtClaimsSet claims =
        baseClaimsBuilder()
            .issuer("other-issuer")
            .audience(java.util.List.of("bookhub-api"))
            .build();
    final String token =
        jwtEncoder
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Should reject token with invalid audience")
  void shouldRejectTokenWithInvalidAudience() throws Exception {
    final JwtClaimsSet claims =
        baseClaimsBuilder()
            .issuer("bookhub-identity")
            .audience(java.util.List.of("other-audience"))
            .build();
    final String token =
        jwtEncoder
            .encode(
                JwtEncoderParameters.from(JwsHeader.with(SignatureAlgorithm.RS256).build(), claims))
            .getTokenValue();

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void shouldPermitHealthAndInfoEndpointsWithoutAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
  }

  @Test
  void shouldProtectOtherActuatorEndpoints() throws Exception {
    mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
  }

  private JwtClaimsSet.Builder baseClaimsBuilder() {
    return JwtClaimsSet.builder()
        .subject("6676f2d8-0f65-40ae-b102-66145e24f3fd")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
        .claim("username", "nico")
        .claim("displayName", "Nicolas Bon")
        .claim("role", "USER")
        .claim("email", "nico@example.com");
  }
}
