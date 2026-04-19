#!/usr/bin/env bash

ENV_FILE="${1:-.env}"

if [ ! -f "$ENV_FILE" ]; then
  echo "Error: $ENV_FILE not found."
  return 1 2>/dev/null || exit 1
fi

# Load variables
set -a
source "$ENV_FILE"
set +a

# Required variables
REQUIRED_VARS=(
  "JWT_ISSUER"
  "JWT_AUDIENCE"
  "JWT_RSA_PRIVATE_KEY"
  "JWT_RSA_PUBLIC_KEY"
  "PASSWORD_RESET_HASH_SECRET"
)

# Check placeholders and missing vars
for var in "${REQUIRED_VARS[@]}"; do
  # Indirect reference to check if the variable is empty
  if [ -z "${!var}" ]; then
    echo "Error: $var is empty or missing in $ENV_FILE"
    return 1 2>/dev/null || exit 1
  fi
  
  # Check if it contains placeholder strings
  if [[ "${!var}" == "replace-with-"* ]] || [[ "${!var}" == *"PASTE_YOUR_LOCAL_"* ]]; then
    echo "Error: $var contains a placeholder in $ENV_FILE. Please replace it with a real value."
    return 1 2>/dev/null || exit 1
  fi
done

# Validate RSA formats (very basic check to ensure they start with BEGIN)
if [[ ! "$JWT_RSA_PRIVATE_KEY" =~ "BEGIN PRIVATE KEY" ]]; then
  echo "Error: JWT_RSA_PRIVATE_KEY does not look like a PKCS#8 PEM private key."
  return 1 2>/dev/null || exit 1
fi

if [[ ! "$JWT_RSA_PUBLIC_KEY" =~ "BEGIN PUBLIC KEY" ]]; then
  echo "Error: JWT_RSA_PUBLIC_KEY does not look like an X.509 PEM public key."
  return 1 2>/dev/null || exit 1
fi

echo "Loaded environment from $ENV_FILE successfully."
