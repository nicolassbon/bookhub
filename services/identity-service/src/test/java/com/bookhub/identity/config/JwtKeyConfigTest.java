package com.bookhub.identity.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bookhub.identity.support.TestRsaKeys;
import org.junit.jupiter.api.Test;

class JwtKeyConfigTest {

    private final JwtKeyConfig jwtKeyConfig = new JwtKeyConfig();

    @Test
    void shouldFailWhenPrivateKeyIsMissing() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa(" ", TestRsaKeys.PUBLIC_2048));

        assertThatThrownBy(() -> jwtKeyConfig.jwtSigningPrivateKey(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("private key");
    }

    @Test
    void shouldFailWhenPublicKeyIsMissing() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa(TestRsaKeys.PRIVATE_2048, ""));

        assertThatThrownBy(() -> jwtKeyConfig.jwtVerificationPublicKey(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("public key");
    }

    @Test
    void shouldFailWhenPrivateKeyIsMalformed() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa("not-a-valid-private-key", TestRsaKeys.PUBLIC_2048));

        assertThatThrownBy(() -> jwtKeyConfig.jwtSigningPrivateKey(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid RSA private key");
    }

    @Test
    void shouldFailWhenModulusIsLessThan2048Bits() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa(TestRsaKeys.PRIVATE_1024, TestRsaKeys.PUBLIC_2048));

        assertThatThrownBy(() -> jwtKeyConfig.jwtSigningPrivateKey(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 2048 bits");
    }

    @Test
    void shouldFailWhenKeyPairDoesNotMatch() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa(TestRsaKeys.PRIVATE_2048, TestRsaKeys.PUBLIC_OTHER_2048));

        assertThatThrownBy(() -> jwtKeyConfig.jwtEncoder(
                jwtKeyConfig.jwtSigningPrivateKey(properties),
                jwtKeyConfig.jwtVerificationPublicKey(properties)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("do not match");
    }

    @Test
    void shouldLoadValidKeyPair() {
        final JwtProperties properties = new JwtProperties(
                "bookhub-identity",
                "bookhub-api",
                3600,
                new JwtProperties.Rsa(TestRsaKeys.PRIVATE_2048, TestRsaKeys.PUBLIC_2048));

        assertThat(jwtKeyConfig.jwtSigningPrivateKey(properties).getModulus().bitLength()).isGreaterThanOrEqualTo(2048);
        assertThat(jwtKeyConfig.jwtVerificationPublicKey(properties).getModulus().bitLength())
                .isGreaterThanOrEqualTo(2048);
    }
}
