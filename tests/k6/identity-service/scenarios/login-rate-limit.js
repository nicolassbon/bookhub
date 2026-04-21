import { check } from 'k6';
import { Rate } from 'k6/metrics';
import { buildBaseConfig, envInt, envNumber, envString } from '../lib/config.js';
import { loginUser, readErrorCode } from '../lib/auth-api.js';

const baseConfig = buildBaseConfig();

const loginRatePerSecond = envInt('LOGIN_RATE_PER_SECOND', 20);
const loginDuration = envString('LOGIN_DURATION', '30s');
const loginPreAllocatedVus = envInt('LOGIN_PRE_ALLOCATED_VUS', 8);
const loginMaxVus = envInt('LOGIN_MAX_VUS', 24);

const minRateLimitedRatio = envNumber('LOGIN_MIN_RATE_LIMITED_RATIO', 0.5);
const minCheckPassRate = envNumber('LOGIN_MIN_CHECK_RATE', 0.99);

const invalidEmail = envString('LOGIN_INVALID_EMAIL', 'rate-limit-login@example.test');
const invalidPassword = envString('LOGIN_INVALID_PASSWORD', 'invalid-password-1');

const rateLimitHitRate = new Rate('login_rate_limit_hit_rate');
const expectedAuthFailureRate = new Rate('login_expected_auth_failure_rate');
const unexpectedStatusRate = new Rate('login_unexpected_status_rate');
const invalidRateLimitErrorRate = new Rate('login_invalid_rate_limit_error_rate');

export const options = {
  scenarios: {
    login_rate_limit: {
      executor: 'constant-arrival-rate',
      rate: loginRatePerSecond,
      timeUnit: '1s',
      duration: loginDuration,
      preAllocatedVUs: loginPreAllocatedVus,
      maxVUs: loginMaxVus,
    },
  },
  thresholds: {
    checks: [`rate>${minCheckPassRate}`],
    login_rate_limit_hit_rate: [`rate>${minRateLimitedRatio}`],
    login_unexpected_status_rate: ['rate==0'],
    login_invalid_rate_limit_error_rate: ['rate==0'],
  },
};

export default function loginRateLimitScenario() {
  const response = loginUser(baseConfig, invalidEmail, invalidPassword);

  const isUnauthorized = response.status === 401;
  const isRateLimited = response.status === 429;
  const isExpectedStatus = isUnauthorized || isRateLimited;

  check(response, {
    'login status is 401 or 429': () => isExpectedStatus,
  });

  rateLimitHitRate.add(isRateLimited);
  expectedAuthFailureRate.add(isUnauthorized);
  unexpectedStatusRate.add(!isExpectedStatus);

  if (isRateLimited) {
    const errorCode = readErrorCode(response);
    const hasValidErrorCode = errorCode === 'RATE_LIMIT_EXCEEDED';

    check(response, {
      'login 429 returns RATE_LIMIT_EXCEEDED': () => hasValidErrorCode,
    });

    invalidRateLimitErrorRate.add(!hasValidErrorCode);
    return;
  }

  if (isUnauthorized) {
    const errorCode = readErrorCode(response);
    const hasExpectedErrorCode = errorCode === 'INVALID_CREDENTIALS';

    check(response, {
      'login 401 returns INVALID_CREDENTIALS': () => hasExpectedErrorCode,
    });
  }
}
