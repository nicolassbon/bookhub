import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { buildBaseConfig, envInt, envNumber, envString } from '../lib/config.js';
import { readErrorCode, refreshSession } from '../lib/auth-api.js';

const baseConfig = buildBaseConfig();

const refreshRatePerSecond = envInt('REFRESH_RATE_PER_SECOND', 20);
const refreshDuration = envString('REFRESH_DURATION', '30s');
const refreshPreAllocatedVus = envInt('REFRESH_PRE_ALLOCATED_VUS', 8);
const refreshMaxVus = envInt('REFRESH_MAX_VUS', 24);

const minRateLimitedRatio = envNumber('REFRESH_MIN_RATE_LIMITED_RATIO', 0.5);
const minCheckPassRate = envNumber('REFRESH_MIN_CHECK_RATE', 0.99);

const invalidRefreshToken = envString('REFRESH_INVALID_TOKEN', '11111111-1111-4111-8111-111111111111');

const rateLimitHitRate = new Rate('refresh_rate_limit_hit_rate');
const invalidTokenRate = new Rate('refresh_invalid_token_rate');
const unexpectedStatusRate = new Rate('refresh_unexpected_status_rate');
const invalidRateLimitErrorRate = new Rate('refresh_invalid_rate_limit_error_rate');

export const options = {
  scenarios: {
    refresh_rate_limit: {
      executor: 'constant-arrival-rate',
      rate: refreshRatePerSecond,
      timeUnit: '1s',
      duration: refreshDuration,
      preAllocatedVUs: refreshPreAllocatedVus,
      maxVUs: refreshMaxVus,
    },
  },
  thresholds: {
    checks: [`rate>${minCheckPassRate}`],
    refresh_rate_limit_hit_rate: [`rate>${minRateLimitedRatio}`],
    refresh_unexpected_status_rate: ['rate==0'],
    refresh_invalid_rate_limit_error_rate: ['rate==0'],
  },
};

export default function refreshRateLimitScenario() {
  const response = refreshSession(baseConfig, invalidRefreshToken);

  const isUnauthorized = response.status === 401;
  const isRateLimited = response.status === 429;
  const isExpectedStatus = isUnauthorized || isRateLimited;

  check(response, {
    'refresh status is 401 or 429': () => isExpectedStatus,
  });

  rateLimitHitRate.add(isRateLimited);
  invalidTokenRate.add(isUnauthorized);
  unexpectedStatusRate.add(!isExpectedStatus);

  if (isRateLimited) {
    const errorCode = readErrorCode(response);
    const hasValidErrorCode = errorCode === 'RATE_LIMIT_EXCEEDED';

    check(response, {
      'refresh 429 returns RATE_LIMIT_EXCEEDED': () => hasValidErrorCode,
    });

    invalidRateLimitErrorRate.add(!hasValidErrorCode);
    return;
  }

  if (isUnauthorized) {
    const errorCode = readErrorCode(response);
    const hasExpectedErrorCode = errorCode === 'INVALID_REFRESH_TOKEN';

    check(response, {
      'refresh 401 returns INVALID_REFRESH_TOKEN': () => hasExpectedErrorCode,
    });
  }
}
