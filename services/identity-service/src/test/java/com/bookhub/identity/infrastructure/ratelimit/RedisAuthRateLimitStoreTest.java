package com.bookhub.identity.infrastructure.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.AuthRateLimitProperties;
import com.bookhub.identity.support.RedisIntegrationTest;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisAuthRateLimitStoreTest extends RedisIntegrationTest {

  private static final String KEY_PREFIX = "bookhub:test:auth-rate-limit:v1";

  private static LettuceConnectionFactory connectionFactory;

  private StringRedisTemplate redisTemplate;

  @BeforeAll
  static void startRedis() {
    connectionFactory =
        new LettuceConnectionFactory(
            REDIS_CONTAINER.getHost(), REDIS_CONTAINER.getFirstMappedPort());
    connectionFactory.afterPropertiesSet();
  }

  @AfterAll
  static void closeConnectionFactory() {
    if (connectionFactory != null) {
      connectionFactory.destroy();
    }
  }

  @BeforeEach
  void cleanRedis() {
    redisTemplate = new StringRedisTemplate(connectionFactory);
    redisTemplate.afterPropertiesSet();
    redisTemplate.execute((RedisCallback<Void>) conn -> {
      conn.serverCommands().flushDb();
      return null;
    });
  }

  @Test
  void sharesCounterAcrossStoreInstances() {
    final RedisAuthRateLimitStore firstStore =
        new RedisAuthRateLimitStore(redisTemplate, properties());
    final RedisAuthRateLimitStore secondStore =
        new RedisAuthRateLimitStore(redisTemplate, properties());

    final RateLimitDecision firstAttempt =
        firstStore.consume("login:203.0.113.10", 2, Duration.ofSeconds(30));
    final RateLimitDecision secondAttempt =
        secondStore.consume("login:203.0.113.10", 2, Duration.ofSeconds(30));
    final RateLimitDecision thirdAttempt =
        firstStore.consume("login:203.0.113.10", 2, Duration.ofSeconds(30));

    assertThat(firstAttempt.allowed()).isTrue();
    assertThat(firstAttempt.remainingAttempts()).isEqualTo(1);
    assertThat(secondAttempt.allowed()).isTrue();
    assertThat(secondAttempt.remainingAttempts()).isZero();
    assertThat(thirdAttempt.allowed()).isFalse();
    assertThat(thirdAttempt.remainingAttempts()).isZero();
  }

  @Test
  void resetsCounterAfterWindowTtlExpires() throws InterruptedException {
    final RedisAuthRateLimitStore store = new RedisAuthRateLimitStore(redisTemplate, properties());

    final RateLimitDecision firstAttempt =
        store.consume("register:198.51.100.10", 1, Duration.ofSeconds(1));
    final RateLimitDecision blockedAttempt =
        store.consume("register:198.51.100.10", 1, Duration.ofSeconds(1));

    Thread.sleep(1_200);

    final RateLimitDecision nextWindowAttempt =
        store.consume("register:198.51.100.10", 1, Duration.ofSeconds(1));

    assertThat(firstAttempt.allowed()).isTrue();
    assertThat(blockedAttempt.allowed()).isFalse();
    assertThat(nextWindowAttempt.allowed()).isTrue();
  }

  @Test
  void storesCountersUnderConfiguredPrefix() {
    final RedisAuthRateLimitStore store = new RedisAuthRateLimitStore(redisTemplate, properties());

    store.consume("refresh:192.0.2.15", 5, Duration.ofSeconds(60));

    assertThat(redisTemplate.hasKey(KEY_PREFIX + ":refresh:192.0.2.15")).isTrue();
  }

  @Test
  void failOpenAllowsRequestWhenRedisUnavailable() {
    final StringRedisTemplate brokenTemplate = mock(StringRedisTemplate.class);
    when(brokenTemplate.execute(any(), anyList(), any()))
        .thenThrow(new QueryTimeoutException("Redis connection timeout"));

    final RedisAuthRateLimitStore store =
        new RedisAuthRateLimitStore(brokenTemplate, failOpenProperties());

    final RateLimitDecision decision = store.consume("login:192.0.2.1", 5, Duration.ofSeconds(60));

    assertThat(decision.allowed()).isTrue();
    assertThat(decision.remainingAttempts()).isEqualTo(5);
  }

  private AuthRateLimitProperties properties() {
    return new AuthRateLimitProperties(
        null,
        null,
        null,
        null,
        null,
        true,
        List.of(),
        new AuthRateLimitProperties.Redis(
            KEY_PREFIX, AuthRateLimitProperties.FailureMode.FAIL_CLOSED));
  }

  private AuthRateLimitProperties failOpenProperties() {
    return new AuthRateLimitProperties(
        null,
        null,
        null,
        null,
        null,
        true,
        List.of(),
        new AuthRateLimitProperties.Redis(
            KEY_PREFIX, AuthRateLimitProperties.FailureMode.FAIL_OPEN));
  }
}
