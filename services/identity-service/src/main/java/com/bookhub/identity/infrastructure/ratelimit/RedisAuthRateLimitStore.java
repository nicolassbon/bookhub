package com.bookhub.identity.infrastructure.ratelimit;

import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStore;
import com.bookhub.identity.application.auth.ratelimit.AuthRateLimitStoreUnavailableException;
import com.bookhub.identity.application.auth.ratelimit.RateLimitDecision;
import com.bookhub.identity.config.AuthRateLimitProperties;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisAuthRateLimitStore implements AuthRateLimitStore {

  private static final DefaultRedisScript<List<Long>> CONSUME_SCRIPT = buildConsumeScript();
  private static final int COUNT_INDEX = 0;
  private static final int TTL_INDEX = 1;

  private final StringRedisTemplate redisTemplate;
  private final String keyPrefix;
  private final AuthRateLimitProperties.FailureMode failureMode;

  public RedisAuthRateLimitStore(
      final StringRedisTemplate redisTemplate,
      final AuthRateLimitProperties authRateLimitProperties) {
    this.redisTemplate = redisTemplate;
    this.keyPrefix = authRateLimitProperties.redis().keyPrefix();
    this.failureMode = authRateLimitProperties.redis().failureMode();
  }

  @Override
  public RateLimitDecision consume(
      final String bucketKey, final int maxAttempts, final Duration window) {
    try {
      return consumeOrThrow(bucketKey, maxAttempts, window);
    } catch (DataAccessException exception) {
      if (failureMode == AuthRateLimitProperties.FailureMode.FAIL_OPEN) {
        log.warn("Auth rate-limit store unavailable, failing open bucket={}", bucketKey, exception);
        return RateLimitDecision.allowed(maxAttempts, window);
      }
      throw new AuthRateLimitStoreUnavailableException(
          "Authentication rate limiting is temporarily unavailable", exception);
    }
  }

  private RateLimitDecision consumeOrThrow(
      final String bucketKey, final int maxAttempts, final Duration window) {
    final List<Long> result =
        redisTemplate.execute(
            CONSUME_SCRIPT, List.of(redisKey(bucketKey)), String.valueOf(window.toSeconds()));

    final long count = numberAt(result, COUNT_INDEX);
    final long ttlSeconds = numberAt(result, TTL_INDEX);
    final Duration ttl = Duration.ofSeconds(Math.max(ttlSeconds, 0));
    if (count > maxAttempts) {
      return RateLimitDecision.blocked(ttl);
    }

    return RateLimitDecision.allowed((int) (maxAttempts - count), ttl);
  }

  private String redisKey(final String bucketKey) {
    return keyPrefix + ":" + bucketKey;
  }

  private long numberAt(final List<Long> result, final int index) {
    if (result == null || result.size() <= index || result.get(index) == null) {
      throw new AuthRateLimitStoreUnavailableException(
          "Authentication rate limiting returned an invalid Redis response", null);
    }
    return result.get(index);
  }

  private static DefaultRedisScript<List<Long>> buildConsumeScript() {
    final DefaultRedisScript<List<Long>> script = new DefaultRedisScript<>();
    @SuppressWarnings("unchecked")
    final Class<List<Long>> listLongType = (Class<List<Long>>) (Class<?>) List.class;
    script.setResultType(listLongType);
    script.setScriptText(
        """
        local count = redis.call('INCR', KEYS[1])
        if count == 1 then
          redis.call('EXPIRE', KEYS[1], ARGV[1])
        end
        local ttl = redis.call('TTL', KEYS[1])
        return { count, ttl }
        """);
    return script;
  }
}
