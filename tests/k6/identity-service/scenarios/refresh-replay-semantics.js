import { check } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { buildBaseConfig, envInt, envNumber, envString } from '../lib/config.js';
import {
  bootstrapReplayRefreshToken,
  readErrorCode,
  refreshSession,
} from '../lib/auth-api.js';

const baseConfig = buildBaseConfig();

const replayConcurrency = envInt('REFRESH_REPLAY_SEMANTICS_CONCURRENCY', 2);
const replayMaxDuration = envString('REFRESH_REPLAY_SEMANTICS_MAX_DURATION', '30s');
const replayUserPrefix = envString('REFRESH_REPLAY_SEMANTICS_USER_PREFIX', 'k6-refresh-replay-semantics');
const minCheckPassRate = envNumber('REFRESH_REPLAY_SEMANTICS_MIN_CHECK_RATE', 0.99);
const minInvalidTokenRejectedRate = envNumber('REFRESH_REPLAY_SEMANTICS_MIN_INVALID_TOKEN_REJECTED_RATE', 0.49);

const replaySuccessCount = new Counter('refresh_replay_semantics_success_count');
const replayInvalidTokenRejectedRate = new Rate('refresh_replay_semantics_invalid_token_rejected_rate');
const replayRateLimitedRate = new Rate('refresh_replay_semantics_rate_limited_rate');
const replayUnexpectedStatusRate = new Rate('refresh_replay_semantics_unexpected_status_rate');

export const options = {
  scenarios: {
    refresh_replay_semantics: {
      executor: 'per-vu-iterations',
      vus: replayConcurrency,
      iterations: 1,
      maxDuration: replayMaxDuration,
      gracefulStop: '0s',
    },
  },
  thresholds: {
    checks: [`rate>${minCheckPassRate}`],
    refresh_replay_semantics_success_count: ['count>=1', 'count<=1'],
    refresh_replay_semantics_invalid_token_rejected_rate: [`rate>${minInvalidTokenRejectedRate}`],
    refresh_replay_semantics_rate_limited_rate: ['rate==0'],
    refresh_replay_semantics_unexpected_status_rate: ['rate==0'],
  },
};

export function setup() {
  const bootstrapResult = bootstrapReplayRefreshToken(baseConfig, {
    scenarioLabel: 'replay semantics',
    userPrefix: replayUserPrefix,
    bootstrapEmailEnvName: 'REFRESH_REPLAY_BOOTSTRAP_EMAIL',
    bootstrapPasswordEnvName: 'REFRESH_REPLAY_BOOTSTRAP_PASSWORD',
  });

  return {
    refreshToken: bootstrapResult.refreshToken,
  };
}

export default function refreshReplaySemanticsScenario(data) {
  const response = refreshSession(baseConfig, data.refreshToken);

  const isSuccess = response.status === 200;
  const isInvalidTokenReject = response.status === 401;
  const isRateLimited = response.status === 429;
  const isExpectedStatus = isSuccess || isInvalidTokenReject || isRateLimited;

  check(response, {
    'replay semantics status is 200, 401, or 429': () => isExpectedStatus,
  });

  replayUnexpectedStatusRate.add(!isExpectedStatus);
  replayInvalidTokenRejectedRate.add(isInvalidTokenReject);
  replayRateLimitedRate.add(isRateLimited);

  if (isSuccess) {
    replaySuccessCount.add(1);

    check(response, {
      'replay semantics success includes refresh cookie rotation': (res) =>
        String(res.headers['Set-Cookie'] || '').includes(`${baseConfig.refreshCookieName}=`),
    });

    return;
  }

  if (isInvalidTokenReject) {
    check(response, {
      'replay semantics reject 401 returns INVALID_REFRESH_TOKEN': (res) => readErrorCode(res) === 'INVALID_REFRESH_TOKEN',
    });
    return;
  }

  if (isRateLimited) {
    check(response, {
      'replay semantics reject 429 returns RATE_LIMIT_EXCEEDED': (res) => readErrorCode(res) === 'RATE_LIMIT_EXCEEDED',
    });
  }
}
