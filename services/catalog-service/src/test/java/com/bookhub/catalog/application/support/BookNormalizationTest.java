package com.bookhub.catalog.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BookNormalizationTest {

  @Test
  void shouldNormalizeIsbn13ByRemovingHyphensAndSpaces() {
    final String normalized = BookNormalization.normalizeIsbn13(" 978-0-261-10334-4 ");

    assertThat(normalized).isEqualTo("9780261103344");
  }

  @Test
  void shouldReturnNullWhenIsbn13IsBlank() {
    final String normalized = BookNormalization.normalizeIsbn13("   ");

    assertThat(normalized).isNull();
  }

  @Test
  void shouldNormalizeSourceReferenceToUpperCaseWithoutPrefixNoise() {
    final String normalized = BookNormalization.normalizeSourceReference("/works/ol262758w");

    assertThat(normalized).isEqualTo("OL262758W");
  }
}
