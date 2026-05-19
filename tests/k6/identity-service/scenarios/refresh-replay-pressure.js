import { check } from 'k6';
import { Trend, Rate } from 'k6/metrics';
import { buildBaseConfig, envInt, envString, envNumber } from '../lib/config.js';
import {
  bootstrapReplayRefreshToken,
  refreshSession,
} from '../lib/auth-api.js';

const baseConfig = buildBaseConfig();

const concurrency = envInt('REFRESH_REPLAY_PRESSURE_CONCURRENCY', 12);
const maxDuration = envString('REFRESH_REPLAY_PRESSURE_MAX_DURATION', '30s');
const userPrefix = envString('REFRESH_REPLAY_PRESSURE_USER_PREFIX', 'k6-refresh-replay-pressure');
const p95LatencyThreshold = envNumber('REFRESH_REPLAY_PRESSURE_P95_LATENCY_MS', 500);

const refreshLatency = new Trend('refresh_replay_pressure_latency_ms', true);
const serverErrorRate = new Rate('refresh_replay_pressure_server_error_rate');

export const options = {
  scenarios: {
    refresh_replay_pressure: {
      executor: 'per-vu-iterations',
      vus: concurrency,
      iterations: 1,
      maxDuration,
      gracefulStop: '0s',
    },
  },
  thresholds: {
    // Infrastructure must respond within acceptable latency under pressure
    refresh_replay_pressure_latency_ms: [`p(95)<${p95LatencyThreshold}`],
    // The server must never respond with 5xx — 4xx are business rejections, not failures
    refresh_replay_pressure_server_error_rate: ['rate==0'],
  },
};

export function setup() {
  const bootstrapResult = bootstrapReplayRefreshToken(baseConfig, {
    scenarioLabel: 'replay pressure',
    userPrefix,
    bootstrapEmailEnvName: 'REFRESH_REPLAY_BOOTSTRAP_EMAIL',
    bootstrapPasswordEnvName: 'REFRESH_REPLAY_BOOTSTRAP_PASSWORD',
  });

  return { refreshToken: bootstrapResult.refreshToken };
}

export default function refreshReplayPressureScenario(data) {
  const response = refreshSession(baseConfig, data.refreshToken);

  refreshLatency.add(response.timings.duration);
  serverErrorRate.add(response.status >= 500);

  // The only structural check: the server must not return 5xx.
  // 200 (success), 401 (token already used/revoked), and 429 (rate limited) are
  // all valid outcomes — their correctness is verified in Java integration tests.
  check(response, {
    'server did not return 5xx': () => response.status < 500,
  });
}
