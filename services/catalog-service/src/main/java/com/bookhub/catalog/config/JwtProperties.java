package com.bookhub.catalog.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(@NotBlank String issuer, @NotBlank String audience, @Valid Rsa rsa) {

  public record Rsa(@NotBlank String publicKey) {}
}
