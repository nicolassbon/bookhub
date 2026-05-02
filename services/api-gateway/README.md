# api-gateway

Single public HTTP entrypoint for BookHub backend services.

## Responsibilities

- route external traffic to the correct backend service
- centralize CORS policy
- provide a single host/port for local and deployed clients

## Route map

- `/api/v1/auth/**` -> `identity-service`
- `/api/v1/users/**` -> `identity-service`
- `/api/v1/books/**` -> `catalog-service`
- `/api/v1/library/**` -> `library-service`
- `/api/v1/goals/**` -> `library-service`
- `/api/v1/reviews/**` -> `library-service`
- `/api/v1/notifications/**` -> `library-service`

## Configuration highlights

- Runs on port `8080` by default.
- Uses Spring Cloud Gateway route definitions from `src/main/resources/application.yml`.
- Environment variables select downstream service URLs:
  - `IDENTITY_SERVICE_URL`
  - `CATALOG_SERVICE_URL`
  - `LIBRARY_SERVICE_URL`
- Cross-origin policy is configured through `GATEWAY_CORS_ALLOWED_ORIGINS`.

## Local profile note

The local profile in `src/main/resources/application-local.yml` must stay aligned with the route map above whenever domains are added or removed.

## Base package

`com.bookhub.gateway`
