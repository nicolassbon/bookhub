# Local Identity Service Setup with RS256

## Goal

Run `identity-service` locally using RS256 without committing secrets or embedding PEM material in tracked files.

## Prerequisites

- OpenSSL installed
- Maven available as `mvn`
- A local `.env` file based on `.env.example`

## 1. Generate a local RSA key pair

Generate a 2048-bit RSA private key and the matching public key:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private_key.pem
openssl rsa -pubout -in private_key.pem -out public_key.pem
```

The application expects:

- `JWT_RSA_PRIVATE_KEY` as a **PKCS#8 PEM** private key
- `JWT_RSA_PUBLIC_KEY` as an **X.509 PEM** public key

## 2. Populate `.env`

Start from `.env.example` and replace all placeholder values in `.env`.

Important fields for `identity-service`:

- `JWT_ISSUER`
- `JWT_AUDIENCE`
- `JWT_RSA_PRIVATE_KEY`
- `JWT_RSA_PUBLIC_KEY`
- `PASSWORD_RESET_HASH_SECRET`

Do **not** commit `.env`. It is intentionally ignored by Git.

## 3. Load your local environment

Use the provided loader script from the repository root:

```bash
source scripts/load-local-env.sh
```

What the script does:

- sources `.env`
- verifies required variables exist
- detects placeholder values that were never replaced
- validates that the RSA values look like PEM blocks
- avoids printing secret values to the terminal

You can also load a different file:

```bash
source scripts/load-local-env.sh .env.local
```

## 4. Start the service

After loading the environment:

```bash
mvn -pl services/identity-service spring-boot:run
```

## 5. Common failure modes

### Missing key material

If `JWT_RSA_PRIVATE_KEY` or `JWT_RSA_PUBLIC_KEY` is blank, the application fails fast during startup.

### Wrong PEM format

If the private key is not PKCS#8 or the public key does not match the private key, startup fails by design.

### Weak RSA key

If the RSA modulus is smaller than 2048 bits, startup fails by design.

### Placeholder values still present

The loader script blocks startup if `.env` still contains placeholders from `.env.example`.

## Security notes

- Keep `private_key.pem`, `public_key.pem`, and `.env` local-only.
- Never paste real keys into `application.yml`, `application-test.yml`, or committed Java test fixtures.
- If you need Docker or IntelliJ later, reuse the same environment variables instead of duplicating secrets into tracked files.
