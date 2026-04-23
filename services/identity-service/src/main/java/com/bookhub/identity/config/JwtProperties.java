package com.bookhub.identity.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
    @NotBlank String issuer, @NotBlank String audience, @Min(1) long expiration, @Valid Rsa rsa) {

  public record Rsa(@NotBlank String privateKey, @NotBlank String publicKey) {}
}
