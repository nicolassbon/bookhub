package com.bookhub.identity.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtKeyConfig {

  private static final int MIN_RSA_BITS = 2048;

  @Bean
  public RSAPrivateKey jwtSigningPrivateKey(final JwtProperties jwtProperties) {
    final String privateKeyPem = jwtProperties.rsa().privateKey();
    requireValue(privateKeyPem, "RSA private key must be provided");
    final RSAPrivateKey privateKey = parsePrivateKey(privateKeyPem);
    validateMinimumBitLength(privateKey.getModulus().bitLength(), "private");
    return privateKey;
  }

  @Bean
  public RSAPublicKey jwtVerificationPublicKey(final JwtProperties jwtProperties) {
    final String publicKeyPem = jwtProperties.rsa().publicKey();
    requireValue(publicKeyPem, "RSA public key must be provided");
    final RSAPublicKey publicKey = parsePublicKey(publicKeyPem);
    validateMinimumBitLength(publicKey.getModulus().bitLength(), "public");
    return publicKey;
  }

  @Bean
  public JwtEncoder jwtEncoder(final RSAPrivateKey privateKey, final RSAPublicKey publicKey) {
    if (!privateKey.getModulus().equals(publicKey.getModulus())) {
      throw new IllegalStateException("RSA private/public keys do not match");
    }

    final RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();

    return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
  }

  private RSAPrivateKey parsePrivateKey(final String pemValue) {
    try {
      final byte[] keyBytes =
          decodePem(pemValue, "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");
      final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
      return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid RSA private key configuration", exception);
    }
  }

  private RSAPublicKey parsePublicKey(final String pemValue) {
    try {
      final byte[] keyBytes =
          decodePem(pemValue, "-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----");
      final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid RSA public key configuration", exception);
    }
  }

  private byte[] decodePem(final String pemValue, final String begin, final String end) {
    final String normalized = pemValue.replace(begin, "").replace(end, "").replaceAll("\\s", "");
    return Base64.getDecoder().decode(normalized);
  }

  private void requireValue(final String value, final String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(message);
    }
  }

  private void validateMinimumBitLength(final int bitLength, final String keyType) {
    if (bitLength < MIN_RSA_BITS) {
      throw new IllegalStateException("RSA " + keyType + " key must be at least 2048 bits");
    }
  }
}
