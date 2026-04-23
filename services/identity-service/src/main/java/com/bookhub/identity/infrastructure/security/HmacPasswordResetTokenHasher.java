package com.bookhub.identity.infrastructure.security;

import com.bookhub.identity.application.auth.PasswordResetTokenHasher;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacPasswordResetTokenHasher implements PasswordResetTokenHasher {

  private static final String ALGORITHM = "HmacSHA256";
  private static final HexFormat HEX_FORMAT = HexFormat.of();

  private final byte[] secret;

  public HmacPasswordResetTokenHasher(final String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public String hash(final String rawToken) {
    try {
      final Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(new SecretKeySpec(secret, ALGORITHM));
      final byte[] digest = mac.doFinal(rawToken.getBytes(StandardCharsets.UTF_8));
      return HEX_FORMAT.formatHex(digest);
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to hash password reset token", exception);
    }
  }
}
