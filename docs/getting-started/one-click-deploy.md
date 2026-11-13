# Cloud deployment notes

Type: explanation.

The repository includes [Render](../../render.yaml) and [Railway](../../railway.toml) configuration starting points. These paths have not been acceptance-tested in this release preparation. They require separate database, network and secret configuration; they are not a verified one-click installation. Provider prices and provisioning times are not promised here.

First verify the [local Docker setup](self-hosted-deploy.md) with synthetic data. A successful local test does not establish cloud readiness.

## Service configuration

Type: reference.

| Service | Build context | Required configuration |
|---------|---------------|------------------------|
| API | `services/api` | Dockerfile builds and tests the JAR; `SPRING_PROFILES_ACTIVE=docker`, database connection, JWT signing key and AWS region |
| Admin dashboard | `services/admin-web` | Dockerfile builds static assets; runtime `API_URL` is the full API origin reachable from nginx, including scheme and port |
| Database | Your separately provisioned MySQL instance | Account, database, networking and TLS settings appropriate to your host |
| Mobile app | `services/app` | Separate Android build targeting your deployment's public API endpoint |

The dashboard browser sends `/admin/` and `/api/` requests to its own origin. Container nginx forwards them to `API_URL`. An internal API hostname belongs in nginx configuration, not in the Android build. A public API address for devices must be reachable from those devices.

The status-only API health path is `/actuator/health`. Media uploads and maps require your own service configuration. The local placeholder bucket is not a media service.

## Railway

Type: reference.

[`railway.toml`](../../railway.toml) describes a manually configured multi-service project. Set the API and dashboard build roots independently, provide your database variables to the API, and set a full internal API origin for the dashboard's `API_URL`. Recheck the provider's current database, networking, port and secret requirements before provisioning.

## Render

Type: reference.

[`render.yaml`](../../render.yaml) describes the API and dashboard services. Supply your database connection and secrets explicitly. Set the dashboard's `API_URL` to a reachable API origin with a scheme and port; do not assume a host/port value alone is a URL. Recheck the provider's current database and private networking requirements before provisioning.

## Before operational use

Type: how-to.

1. In an isolated cloud test project, run health, login, profile and synthetic-record checks through the dashboard origin.
   Recovery: If a check fails, keep the deployment private and inspect that service's logs. Do not enter real family data.
2. In the same test project, verify persistence after a restart and the operator's backup/restore procedure.
   Recovery: If records do not survive or a restore fails, stop the rollout and resolve storage configuration.
3. In your Android build configuration, select your own public API endpoint and use the [EAS build instructions](../developer-guide/mobile-app.md#building-apks).
   Recovery: If the endpoint or build identity is uncertain, do not distribute the build.
4. In your operational review, complete [security and maintenance preparation](../implementer-guide/security.md).
   Recovery: If ownership, access or maintenance procedures are missing, keep the system in synthetic evaluation.

A downloadable curriculum PDF does not populate the app. Configure and review [structured modules](../implementer-guide/curriculum-management.md) before testing curriculum delivery.
