package com.bookhub.identity.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookhub.identity.support.TestRsaKeys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class JwtKeyConfigContextValidationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  ConfigurationPropertiesAutoConfiguration.class,
                  ValidationAutoConfiguration.class))
          .withUserConfiguration(JwtKeyConfig.class)
          .withPropertyValues(
              "jwt.issuer=bookhub-identity", "jwt.audience=bookhub-api", "jwt.expiration=3600");

  @Test
  void shouldFailContextStartupWhenPublicKeyIsMalformed() {
    contextRunner
        .withPropertyValues(
            "jwt.rsa.private-key=" + toProperty(TestRsaKeys.PRIVATE_2048),
            "jwt.rsa.public-key=not-a-valid-public-key")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .isInstanceOf(org.springframework.beans.factory.BeanCreationException.class)
                  .hasMessageContaining("Invalid RSA public key configuration");
            });
  }

  @Test
  void shouldFailContextStartupWhenRsaKeyPairDoesNotMatch() {
    contextRunner
        .withPropertyValues(
            "jwt.rsa.private-key=" + toProperty(TestRsaKeys.PRIVATE_2048),
            "jwt.rsa.public-key=" + toProperty(TestRsaKeys.PUBLIC_OTHER_2048))
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .isInstanceOf(org.springframework.beans.factory.BeanCreationException.class)
                  .hasMessageContaining("RSA private/public keys do not match");
            });
  }

  @Test
  void shouldLoadContextWhenRsaKeyPairIsValid() {
    contextRunner
        .withPropertyValues(
            "jwt.rsa.private-key=" + toProperty(TestRsaKeys.PRIVATE_2048),
            "jwt.rsa.public-key=" + toProperty(TestRsaKeys.PUBLIC_2048))
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context)
                  .hasSingleBean(org.springframework.security.oauth2.jwt.JwtEncoder.class);
            });
  }

  private String toProperty(final String pem) {
    return pem.replace("\r", "").replace("\n", "");
  }
}
