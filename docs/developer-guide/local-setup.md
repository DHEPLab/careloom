# Local development

Type: explanation.

Use a separate checkout and synthetic records. The [full Docker setup](../getting-started/self-hosted-deploy.md) is the primary fresh-instance path: it builds Java inside the image and starts an isolated database, API and dashboard. This page covers component development after that distinction is clear.

## Prerequisites

Type: reference.

| Component | Tooling |
|-----------|---------|
| Full local stack | Docker Engine with Compose v2 or Docker Desktop; Git |
| Native API | JDK 11 and the checked-in Gradle wrapper; Java source target remains 8 |
| Admin/mobile JavaScript | Node 20 used by CI; Yarn Classic 1.x; committed lockfiles |
| Android runtime | A compatible development build/device setup; see [Mobile App](mobile-app.md) |

Do not assume newer Java versions or the current Expo Go app are compatible with this pinned stack. Keep local overrides and credentials outside Git.

## Native database and API

Type: how-to.

1. In the repository root, review `.env` using the [evaluation example](../../.env.example), then start a uniquely named database-only project:
   ```bash
   docker compose -p careloom-native-evaluation -f docker-compose.yml up -d
   ```
   Recovery: If `DB_PORT` is occupied, set an unused value in `.env` and retry. Do not stop another database. This file publishes MySQL only on loopback.
2. In `services/api`, start the API with the `docker` configuration profile and your own local database values. For an unchanged evaluation example on port 3306:
   ```bash
   SPRING_PROFILES_ACTIVE=docker \
   SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/careloom?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf-8' \
   SPRING_DATASOURCE_USERNAME=careloom \
   SPRING_DATASOURCE_PASSWORD=local-evaluation-user-change-me \
   JWT_SECRET_KEY=local-evaluation-signing-key-change-before-any-real-use \
   AWS_REGION=us-east-1 AWS_EC2_METADATA_DISABLED=true \
   ./gradlew bootRun
   ```
   Recovery: If your `.env` differs, supply matching values here, including `DB_PORT` in the JDBC URL. If Java is unavailable, use the full Docker path. Do not copy account-specific settings from an existing research deployment.
3. On the local machine, check `http://127.0.0.1:8080/actuator/health`.
   Recovery: If health fails, inspect API startup logs and the scoped database logs. Do not delete a volume or edit past migrations to bypass an error.

The example media bucket is a placeholder. Native API startup does not establish that media uploads, maps or operational backup procedures work.

## Admin dashboard development

Type: how-to.

1. In `services/admin-web`, install the locked dependencies:
   ```bash
   yarn install --frozen-lockfile
   ```
   Recovery: If the lockfile cannot be satisfied, stop and inspect the install error; do not regenerate it as a setup workaround.
2. In `services/admin-web`, start Vite:
   ```bash
   yarn start
   ```
   Recovery: If port 3000 is unavailable, use the port reported by Vite or choose an explicit unused port. Do not assume port 5173.

Vite defaults to `http://localhost:3000` and proxies `/api` and `/admin` to `http://localhost:8080`. Use `BACKEND_PROXY_TARGET` for another deliberate local API origin. Packaged nginx uses the separate runtime `API_URL` setting.

## Mobile development

Type: how-to.

Follow the [mobile guide](mobile-app.md) to select an isolated API endpoint and compatible Android build. Sign in and prepare curriculum data online before testing offline behavior. Use the [testing runbook](testing.md) for mocked upload failures and local-calendar tests without contacting a backend.

Recovery: If a device cannot reach a loopback-bound API, configure a deliberate test transport with the operator. Do not point the app at an existing production endpoint or expose a database to work around connectivity.

## Stop the native database

Type: how-to.

In the repository root, run:

```bash
docker compose -p careloom-native-evaluation -f docker-compose.yml down
```

The volume remains. Recovery: If you used another project name, confirm it with `docker compose ls` and stop that exact project only. [Deleting a synthetic test volume](testing.md#cleanup) is separate from stopping services.
