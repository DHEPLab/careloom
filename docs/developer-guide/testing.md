# Testing runbook

## Safe default checks

Type: how-to.

Use a separate checkout with synthetic fixtures. The component test commands below use mocked services or the API test profile; they do not require a running research API or MySQL database.

1. In `services/admin-web`, install locked dependencies:
   ```bash
   yarn install --frozen-lockfile
   ```
   Recovery: If dependency installation fails, inspect the error and retain the lockfile. Use a writable local package cache if needed.
2. In `services/admin-web`, run dashboard tests and its production build:
   ```bash
   yarn test:ci
   yarn build
   ```
   Pass signal: both commands exit zero with no failed tests. Recovery: Fix the reported failure before using the built dashboard; passing tests alone do not verify nginx routing.
3. In `services/app`, install locked dependencies and run the mobile tests:
   ```bash
   yarn install --frozen-lockfile
   yarn test:ci --runInBand
   ```
   Pass signal: no failed suites or tests; preserve the skipped-test count. Recovery: Inspect the failure rather than hiding it by changing the CI timezone.
4. In `services/app`, check local-calendar portability:
   ```bash
   TZ=America/New_York yarn test:ci --runInBand
   TZ=UTC yarn test:ci --runInBand
   TZ=Asia/Shanghai yarn test:ci --runInBand
   ```
   Pass signal: all three runs pass. Recovery: Compare fixtures and UI inputs; UTC-midnight date strings are not necessarily the intended local calendar day.
5. In `services/api`, run backend tests with JDK 11:
   ```bash
   SPRING_PROFILES_ACTIVE=test ./gradlew --no-daemon --max-workers=2 test
   ```
   Pass signal: `BUILD SUCCESSFUL`, with zero failed tests. Recovery: If no compatible JDK exists, use the [Docker build](../getting-started/self-hosted-deploy.md), whose build stage runs these tests. Do not switch tests to an operational datasource.

The API test profile uses H2 and disables Flyway. It does not validate MySQL migrations. Dashboard/mobile fixtures mock HTTP; they do not establish real-device readiness or clinical validity.

## Test boundaries

Type: reference.

Safe in the isolated checkout:

- Component unit tests and static builds.
- `docker compose -p careloom-local-evaluation -f docker-compose.full.yml config --quiet` after reviewing `.env`.
- `python3 scripts/deployment-smoke.py --help`, which makes no requests.

Require a deliberate disposable instance or separate operational authorization:

- The deployment smoke check below writes synthetic records.
- Starting a stack against a database runs migrations.
- Volume deletion, backup restore and password changes affect stored state.
- EAS builds use a separate hosted build service. Running or distributing an app can contact its configured API.

Never substitute a research endpoint or participant records for a missing fixture. Do not log returned tokens, database passwords or family details.

## Synthetic installation smoke check

Type: how-to.

1. In the repository root, follow the [local setup](../getting-started/self-hosted-deploy.md) with a new Compose project and new volume.
   Recovery: If the project or volume already contains operational data, stop and choose a new identity.
2. In the repository root, inspect its identity and ports:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml ps
   ```
   Recovery: If the ports belong to another instance, do not run the smoke script against them.
3. In the repository root, opt in to synthetic writes on that instance:
   ```bash
   python3 scripts/deployment-smoke.py --allow-synthetic-writes \
     --base-url http://127.0.0.1:3000 \
     --api-url http://127.0.0.1:8080
   ```
   Pass signal: exit zero and JSON containing `"result": "passed"`. Recovery: Use your chosen ports if different; inspect the failing check and private service logs. Preserve the database for diagnosis rather than retrying against another endpoint.

The script accepts plain HTTP localhost origins only, checks status-only health and authentication through nginx, and creates/reads/updates/deletes a generated `SYNTHETIC-SMOKE-` project. Initial credentials are the migration defaults of a fresh database. The script keeps tokens in memory and attempts to delete its synthetic project. A failed run may leave synthetic rows; the disposable volume is the final cleanup boundary.

This check does not verify media storage, maps, offline device behavior, cloud deployment, backups or production access isolation.

## Cleanup

Type: how-to.

1. In the repository root, stop the confirmed evaluation project while preserving its volume:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml down
   ```
   Recovery: If unsure which project owns the resources, inspect `docker compose ls`; do not remove anything.
2. Only after confirming every record in this exact project is disposable synthetic data, explicitly delete its volume:
   ```bash
   docker compose -p careloom-local-evaluation -f docker-compose.full.yml down --volumes
   ```
   Recovery: If there is any uncertainty about ownership or data, omit `--volumes` and ask the database owner to review. Do not use volume deletion as an upgrade, password rotation or migration repair.

Use the same project name and configuration used at startup. For database-only native development, use `-p careloom-native-evaluation -f docker-compose.yml` instead.

## Verification records

Type: reference.

Record the source commit, tool versions, command, timezone, exit code, test/skipped counts and synthetic scope in the review receipt. Distinguish commands actually run from instructions inferred from configuration. A build or test result applies only to its tested tree; a source publication, hosted release or operational rollout requires its own evidence and approval.
