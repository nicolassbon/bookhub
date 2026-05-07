package com.bookhub.catalog.support;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public final class TestRsaKeys {

  private static final KeyPair KEY_PAIR_2048 = generateKeyPair(2048);

  public static final String PRIVATE_2048 = toPrivatePem(KEY_PAIR_2048.getPrivate());
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

  private static String toPrivatePem(final PrivateKey privateKey) {
    return toPem("PRIVATE KEY", privateKey.getEncoded());
  }

  private static String toPublicPem(final PublicKey publicKey) {
    return toPem("PUBLIC KEY", publicKey.getEncoded());
  }

  private static String toPem(final String keyType, final byte[] encodedKey) {
    final String base64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encodedKey);
    return "-----BEGIN " + keyType + "-----\n" + base64 + "\n-----END " + keyType + "-----";
  }
}
