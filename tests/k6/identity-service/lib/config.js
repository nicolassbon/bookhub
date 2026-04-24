const DEFAULT_BASE_URL = 'http://localhost:8080';
const DEFAULT_TIMEOUT = '10s';

export function envString(name, defaultValue) {
  const value = __ENV[name];
  if (value === undefined || value === null || value === '') {
    return defaultValue;
  }
  return value;
}

export function envInt(name, defaultValue) {
  const value = __ENV[name];
  if (value === undefined || value === null || value === '') {
    return defaultValue;
  }

  const parsedValue = Number.parseInt(value, 10);
  if (Number.isNaN(parsedValue)) {
    throw new Error(`Environment variable ${name} must be an integer. Received: ${value}`);
  }

  return parsedValue;
}

export function envNumber(name, defaultValue) {
  const value = __ENV[name];
  if (value === undefined || value === null || value === '') {
    return defaultValue;
  }

  const parsedValue = Number(value);
  if (Number.isNaN(parsedValue)) {
    throw new Error(`Environment variable ${name} must be numeric. Received: ${value}`);
  }

  return parsedValue;
}

export function buildBaseConfig() {
  return {
    baseUrl: envString('BASE_URL', DEFAULT_BASE_URL),
    timeout: envString('HTTP_TIMEOUT', DEFAULT_TIMEOUT),
    refreshCookieName: envString('REFRESH_COOKIE_NAME', 'refresh_token'),
  };
}
