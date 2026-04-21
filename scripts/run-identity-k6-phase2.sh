#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
SCENARIO_NAME="${1:-all}"

if ! command -v k6 >/dev/null 2>&1; then
  printf 'k6 is not installed. Install it from https://grafana.com/docs/k6/latest/set-up/install-k6/\n' >&2
  exit 1
fi

declare -A SCENARIO_PATHS
SCENARIO_PATHS[login-rate-limit]="$ROOT_DIR/tests/k6/identity-service/scenarios/login-rate-limit.js"
SCENARIO_PATHS[refresh-rate-limit]="$ROOT_DIR/tests/k6/identity-service/scenarios/refresh-rate-limit.js"
SCENARIO_PATHS[refresh-replay-semantics]="$ROOT_DIR/tests/k6/identity-service/scenarios/refresh-replay-semantics.js"
SCENARIO_PATHS[refresh-replay-pressure]="$ROOT_DIR/tests/k6/identity-service/scenarios/refresh-replay-pressure.js"
# Backward-compatible alias. Prefer refresh-replay-semantics.
SCENARIO_PATHS[refresh-replay]="${SCENARIO_PATHS[refresh-replay-semantics]}"

run_scenario() {
  local scenario_key="$1"
  local scenario_path="${SCENARIO_PATHS[$scenario_key]}"

  if [[ ! -f "$scenario_path" ]]; then
    printf 'Scenario script not found: %s\n' "$scenario_path" >&2
    exit 1
  fi

  printf '\n=== Running k6 scenario: %s ===\n' "$scenario_key"
  k6 run "$scenario_path" "${EXTRA_ARGS[@]}"
}

EXTRA_ARGS=()
if [[ $# -gt 1 ]]; then
  EXTRA_ARGS=("${@:2}")
fi

if [[ "$SCENARIO_NAME" == "all" ]]; then
  run_scenario login-rate-limit
  run_scenario refresh-rate-limit
  run_scenario refresh-replay-semantics
  run_scenario refresh-replay-pressure
  exit 0
fi

if [[ "$SCENARIO_NAME" == "rate-limit-suite" ]]; then
  run_scenario login-rate-limit
  run_scenario refresh-rate-limit
  exit 0
fi

if [[ "$SCENARIO_NAME" == "replay-suite" ]]; then
  run_scenario refresh-replay-semantics
  run_scenario refresh-replay-pressure
  exit 0
fi

if [[ -z "${SCENARIO_PATHS[$SCENARIO_NAME]+x}" ]]; then
  printf 'Unknown scenario: %s\n' "$SCENARIO_NAME" >&2
  printf 'Allowed values: all | rate-limit-suite | replay-suite | login-rate-limit | refresh-rate-limit | refresh-replay-semantics | refresh-replay-pressure | refresh-replay\n' >&2
  exit 1
fi

run_scenario "$SCENARIO_NAME"
