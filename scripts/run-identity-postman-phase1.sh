#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
COLLECTION_PATH="$ROOT_DIR/tests/postman/identity-service/bookhub-identity-auth-phase1.postman_collection.json"
DEFAULT_ENV_PATH="$ROOT_DIR/tests/postman/identity-service/bookhub-identity-local.postman_environment.json"

ENV_PATH="${1:-$DEFAULT_ENV_PATH}"

if [[ ! -f "$COLLECTION_PATH" ]]; then
  printf 'Collection file not found: %s\n' "$COLLECTION_PATH" >&2
  exit 1
fi

if [[ ! -f "$ENV_PATH" ]]; then
  printf 'Environment file not found: %s\n' "$ENV_PATH" >&2
  exit 1
fi

if ! command -v newman >/dev/null 2>&1; then
  printf 'newman is not installed. Install it with: npm install -g newman\n' >&2
  exit 1
fi

EXTRA_ARGS=()
if [[ $# -gt 1 ]]; then
  EXTRA_ARGS=("${@:2}")
fi

AUTOMATED_ITEMS=(
  "01 - Register"
  "02 - Login"
  "03 - Get Current User (Me)"
  "04 - Refresh"
  "05 - Logout"
  "06 - Forgot Password"
)

FOLDER_ARGS=()
for item in "${AUTOMATED_ITEMS[@]}"; do
  FOLDER_ARGS+=(--folder "$item")
done

newman run "$COLLECTION_PATH" -e "$ENV_PATH" --reporters cli "${FOLDER_ARGS[@]}" "${EXTRA_ARGS[@]}"
