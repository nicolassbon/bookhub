import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { buildBaseConfig, envInt, envNumber, envString } from '../lib/config.js';
import {
  bootstrapReplayRefreshToken,
  readErrorCode,
  refreshSession,
} from '../lib/auth-api.js';

const baseConfig = buildBaseConfig();

const replayConcurrency = envInt('REFRESH_REPLAY_PRESSURE_CONCURRENCY', 12);
const replayMaxDuration = envString('REFRESH_REPLAY_PRESSURE_MAX_DURATION', '30s');
const replayUserPrefix = envString('REFRESH_REPLAY_PRESSURE_USER_PREFIX', 'k6-refresh-replay-pressure');
const minCheckPassRate = envNumber('REFRESH_REPLAY_PRESSURE_MIN_CHECK_RATE', 0.99);
const minRejectedRate = envNumber('REFRESH_REPLAY_PRESSURE_MIN_REJECTED_RATE', 0.5);

const replayAttemptCount = new Counter('refresh_replay_pressure_attempt_count');
const replaySuccessCount = new Counter('refresh_replay_pressure_success_count');
const replayRejectedRate = new Rate('refresh_replay_pressure_rejected_rate');
const replayInvalidTokenRejectedRate = new Rate('refresh_replay_pressure_invalid_token_rejected_rate');
const replayRateLimitedRate = new Rate('refresh_replay_pressure_rate_limited_rate');
const replayUnexpectedStatusRate = new Rate('refresh_replay_pressure_unexpected_status_rate');

export const options = {
  scenarios: {
    refresh_replay_pressure: {
      executor: 'per-vu-iterations',
      vus: replayConcurrency,
      iterations: 1,
      maxDuration: replayMaxDuration,
      gracefulStop: '0s',
    },
  },
  thresholds: {
    checks: [`rate>${minCheckPassRate}`],
    refresh_replay_pressure_attempt_count: ['count>=2'],
    refresh_replay_pressure_success_count: ['count<=1'],
    refresh_replay_pressure_rejected_rate: [`rate>${minRejectedRate}`],
    refresh_replay_pressure_unexpected_status_rate: ['rate==0'],
  },
};

export function setup() {
  const bootstrapResult = bootstrapReplayRefreshToken(baseConfig, {
    scenarioLabel: 'replay pressure',
    userPrefix: replayUserPrefix,
    bootstrapEmailEnvName: 'REFRESH_REPLAY_BOOTSTRAP_EMAIL',
    bootstrapPasswordEnvName: 'REFRESH_REPLAY_BOOTSTRAP_PASSWORD',
  });

  return {
    refreshToken: bootstrapResult.refreshToken,
  };
}

export default function refreshReplayPressureScenario(data) {
  const response = refreshSession(baseConfig, data.refreshToken);

  replayAttemptCount.add(1);

  const isSuccess = response.status === 200;
  const isInvalidTokenReject = response.status === 401;
  const isRateLimited = response.status === 429;
  const isRejected = isInvalidTokenReject || isRateLimited;
  const isExpectedStatus = isSuccess || isRejected;

  check(response, {
    'replay pressure status is 200, 401, or 429': () => isExpectedStatus,
  });

  replayUnexpectedStatusRate.add(!isExpectedStatus);
  replayRejectedRate.add(isRejected);
  replayInvalidTokenRejectedRate.add(isInvalidTokenReject);
  replayRateLimitedRate.add(isRateLimited);

  if (isSuccess) {
    replaySuccessCount.add(1);

    check(response, {
      'replay pressure success includes refresh cookie rotation': (res) =>
        String(res.headers['Set-Cookie'] || '').includes(`${baseConfig.refreshCookieName}=`),
    });

    return;
  }

  if (isInvalidTokenReject) {
    check(response, {
      'replay pressure reject 401 returns INVALID_REFRESH_TOKEN': (res) => readErrorCode(res) === 'INVALID_REFRESH_TOKEN',
    });
    return;
  }

  if (isRateLimited) {
    check(response, {
      'replay pressure reject 429 returns RATE_LIMIT_EXCEEDED': (res) => readErrorCode(res) === 'RATE_LIMIT_EXCEEDED',
    });
  }
}
