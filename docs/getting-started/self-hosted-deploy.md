# Local self-hosted setup

Type: explanation.

This path builds the API, admin dashboard and a new MySQL database for local evaluation with synthetic records. It does not install the Android app or establish production readiness. The API Dockerfile builds its JAR from source and runs backend tests; no host Java installation or prebuilt JAR is required for this path.

## Start an isolated instance

Type: how-to.

Run each command from the repository root. Use Docker Engine with Compose v2 or Docker Desktop. Image and dependency downloads need network access.

1. In a new local checkout, create an evaluation configuration:
   ```bash
   cp .env.example .env
   ```
   Recovery: If `.env` already exists, review it rather than overwriting it. Confirm it does not point at an existing research database or service.
2. In `.env`, choose unused `ADMIN_PORT` and `API_PORT` values and review the evaluation-only database/JWT settings.
   Recovery: If ports are occupied, choose different ports for this project. Do not stop another project's services.
3. In the repository root, validate configuration for a unique Compose project:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml config --quiet
   ```
   Recovery: If Compose reports a missing value, set it in `.env` and rerun this command. Do not print a rendered secret-bearing configuration into shared logs.
4. In the repository root, build and start that project:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml up --build -d
   ```
   Recovery: If building or startup fails, inspect the scoped logs below. Do not reuse a database from another project to bypass a failure.
5. In the repository root, inspect startup:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml ps
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml logs --tail=100 api
   ```
   Recovery: If the API is unhealthy, check migrations and database connectivity in the private logs. Do not edit historical migration files or reset an existing database.
6. On the local machine, open the dashboard at `http://127.0.0.1:3000` and check API status at `http://127.0.0.1:8080/actuator/health` when using the example ports.
   Recovery: If you changed ports, use the values in `.env`. If the dashboard loads but login fails, inspect nginx/API logs and the `/admin/` and `/api/` proxy path.

The local bootstrap account is `admin` / `admin`. These credentials and the example signing key are for isolated evaluation. Do not expose this instance or put real family data in it. See [First Setup](../implementer-guide/first-setup.md) for program configuration after validation.

## Network and feature boundaries

Type: reference.

- Dashboard and API host ports bind to `127.0.0.1`; MySQL is internal to the Compose network.
- The dashboard container uses `API_URL=http://api:8080`; its browser sends same-origin requests through nginx.
- Media uploads and maps require your own separately configured services. A successful health/login test does not verify those features.
- An Android device cannot reach your machine through the device's `localhost`. Configure a deliberate test endpoint using the [mobile guide](../developer-guide/mobile-app.md).
- The curriculum PDF is separate from app data. Build and publish [structured modules](../implementer-guide/curriculum-management.md) for delivery.
- Cloud templates remain [unverified starting points](one-click-deploy.md).

## Stop and preserve local data

Type: how-to.

In the repository root, stop only this evaluation project:

```bash
docker compose -p careloom-local-evaluation -f docker-compose.full.yml down
```

The database volume remains. If you used a different project name, use that same name here. Recovery: If unsure which project owns the resources, inspect `docker compose ls` and do not remove volumes.

Deleting an evaluation database is a separate destructive action. See the [testing runbook](../developer-guide/testing.md#cleanup) for the explicit synthetic-only cleanup gate. Do not use `down -v` to repair a shared or operational database.

## Existing deployments

Type: explanation.

This new-instance guide does not describe an upgrade or credential rotation. Changing MySQL initialization values in `.env` does not change account passwords in an existing database. Use the operator-owned [maintenance procedure](../implementer-guide/security.md#existing-database-credentials) and verify backups before an operational change.
