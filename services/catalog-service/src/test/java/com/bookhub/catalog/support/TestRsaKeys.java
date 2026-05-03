package com.bookhub.catalog.support;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

public final class TestRsaKeys {

  private static final KeyPair KEY_PAIR_2048 = generateKeyPair(2048);

  public static final String PUBLIC_2048 = toPublicPem(KEY_PAIR_2048.getPublic());

  private TestRsaKeys() {}

  private static KeyPair generateKeyPair(final int keySize) {
    try {
      final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(keySize);
      return generator.generateKeyPair();
    } catch (Exception exception) {
      throw new IllegalStateException("Failed to generate RSA key pair for tests", exception);
    }
  }

  private static String toPublicPem(final PublicKey publicKey) {
    final String base64 =
        Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded());
    return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
  }
}
