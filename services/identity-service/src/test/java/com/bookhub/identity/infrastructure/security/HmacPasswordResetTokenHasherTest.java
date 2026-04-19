package com.bookhub.identity.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class HmacPasswordResetTokenHasherTest {

    @Test
    void shouldProduceDeterministicHashForSameToken() {
        final String hashSecret = "test-" + UUID.randomUUID();
        final HmacPasswordResetTokenHasher hasher =
                new HmacPasswordResetTokenHasher(hashSecret);

        final String firstHash = hasher.hash("raw-token");
        final String secondHash = hasher.hash("raw-token");

        assertThat(firstHash).isEqualTo(secondHash);
        assertThat(firstHash).isNotEqualTo("raw-token");
    }

    @Test
    void shouldProduceDifferentHashForDifferentTokens() {
        final String hashSecret = "test-" + UUID.randomUUID();
        final HmacPasswordResetTokenHasher hasher =
                new HmacPasswordResetTokenHasher(hashSecret);

        final String firstHash = hasher.hash("raw-token-one");
        final String secondHash = hasher.hash("raw-token-two");

        assertThat(firstHash).isNotEqualTo(secondHash);
    }
}
