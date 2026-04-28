import http from 'k6/http';
import { check } from 'k6';

function absoluteUrl(baseUrl, path) {
  return `${baseUrl}${path}`;
}

function requestParams(tags, timeout) {
  return {
    headers: {
      'Content-Type': 'application/json',
    },
    tags,
    timeout,
  };
}

export function extractCookieValue(response, cookieName) {
  const setCookieHeader = response.headers['Set-Cookie'];
  if (!setCookieHeader) {
    return '';
  }

  const headerValues = Array.isArray(setCookieHeader) ? setCookieHeader : [setCookieHeader];
  const prefix = `${cookieName}=`;

  for (const headerValue of headerValues) {
    const parts = String(headerValue).split(';');
    if (parts.length === 0) {
      continue;
    }

    const cookiePair = parts[0].trim();
    if (!cookiePair.startsWith(prefix)) {
      continue;
    }

    const tokenValue = cookiePair.slice(prefix.length);
    if (tokenValue.length > 0) {
      return tokenValue;
    }
  }

  return '';
}

function readOptionalEnv(name) {
  const value = __ENV[name];
  if (value === undefined || value === null) {
    return '';
  }

  return String(value).trim();
}

function generateRuntimePassword(seed) {
  const randomPart = Math.random().toString(36).slice(2, 10);
  return `Bh!${seed}${randomPart}Aa1`;
}

export function buildRandomUser(prefix = 'k6') {
  const vu = typeof __VU !== 'undefined' ? __VU : 'setup';
  const iter = typeof __ITER !== 'undefined' ? __ITER : Math.floor(Math.random() * 1000000);
  const uniquePart = `${Date.now()}-${vu}-${iter}`;
  const username = `${prefix}-${vu}-${Math.abs(Date.now() % 100000)}`;
  const configuredPassword = readOptionalEnv('K6_TEST_PASSWORD') || readOptionalEnv('IDENTITY_TEST_PASSWORD');
  return {
    username,
    email: `${prefix}.${uniquePart}@example.test`,
    password: configuredPassword || generateRuntimePassword(uniquePart),
    displayName: `K6 ${uniquePart}`,
  };
}

export function registerUser(baseConfig, user) {
  return http.post(
    absoluteUrl(baseConfig.baseUrl, '/api/v1/auth/register'),
    JSON.stringify(user),
    requestParams({ endpoint: 'register' }, baseConfig.timeout),
  );
}

export function loginUser(baseConfig, email, password) {
  return http.post(
    absoluteUrl(baseConfig.baseUrl, '/api/v1/auth/login'),
    JSON.stringify({ email, password }),
    requestParams({ endpoint: 'login' }, baseConfig.timeout),
  );
}

export function refreshSession(baseConfig, refreshTokenValue) {
  return http.post(
    absoluteUrl(baseConfig.baseUrl, '/api/v1/auth/refresh'),
    null,
    {
      headers: {
        Cookie: `${baseConfig.refreshCookieName}=${refreshTokenValue}`,
      },
      tags: {
        endpoint: 'refresh',
      },
      timeout: baseConfig.timeout,
    },
  );
}

export function assertBootstrapLogin(response, refreshCookieName) {
  const refreshToken = extractCookieValue(response, refreshCookieName);

  const isValid = check(response, {
    'bootstrap login returned HTTP 200': (res) => res.status === 200,
    'bootstrap login returned refresh cookie': () => refreshToken.length > 0,
  });

  if (!isValid) {
    const body = response.body;
    throw new Error(`Unable to bootstrap refresh token. status=${response.status}, body=${JSON.stringify(body)}`);
  }

  return refreshToken;
}

export function bootstrapReplayRefreshToken(baseConfig, options) {
  const scenarioLabel = options?.scenarioLabel || 'replay setup';
  const userPrefix = options?.userPrefix || 'k6-refresh-replay';
  const bootstrapEmailEnvName = options?.bootstrapEmailEnvName || 'REFRESH_REPLAY_BOOTSTRAP_EMAIL';
  const bootstrapPasswordEnvName = options?.bootstrapPasswordEnvName || 'REFRESH_REPLAY_BOOTSTRAP_PASSWORD';

  const configuredEmail = readOptionalEnv(bootstrapEmailEnvName);
  const configuredPassword = readOptionalEnv(bootstrapPasswordEnvName);

  if ((configuredEmail && !configuredPassword) || (!configuredEmail && configuredPassword)) {
    throw new Error(
      `${scenarioLabel} setup requires both ${bootstrapEmailEnvName} and ${bootstrapPasswordEnvName} when using env bootstrap credentials.`,
    );
  }

  if (configuredEmail && configuredPassword) {
    const loginResponse = loginUser(baseConfig, configuredEmail, configuredPassword);
    const refreshToken = assertBootstrapLogin(loginResponse, baseConfig.refreshCookieName);

    return {
      refreshToken,
      bootstrapMode: 'env-login',
    };
  }

  const user = buildRandomUser(userPrefix);
  const registerResponse = registerUser(baseConfig, user);

  check(registerResponse, {
    [`${scenarioLabel} setup register returns 201`]: (res) => res.status === 201,
  });

  if (registerResponse.status !== 201) {
    const body = registerResponse.body;
    throw new Error(
      `${scenarioLabel} setup failed during register. status=${registerResponse.status}, body=${JSON.stringify(body)}`,
    );
  }

  const loginResponse = loginUser(baseConfig, user.email, user.password);
  const refreshToken = assertBootstrapLogin(loginResponse, baseConfig.refreshCookieName);

  return {
    refreshToken,
    bootstrapMode: 'register-login',
  };
}
