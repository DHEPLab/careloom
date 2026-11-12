# Environment variables

Type: reference.

Use [`.env.example`](../../.env.example) for local evaluation. Keep real secrets outside Git. Compose reads `.env`; it does not export those values into your terminal for native commands.

## Database initialization

Type: reference.

| Variable | Meaning |
|----------|---------|
| `MYSQL_ROOT_PASSWORD` | Root password used to initialize a new MySQL data directory |
| `MYSQL_DATABASE` | Initial database name, default `careloom` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Application account initialized in a new database |
| `DB_PORT` | Database-only Compose host port; bound to loopback; default 3306 |

The full Compose stack leaves MySQL internal and does not publish `DB_PORT`. These initialization variables do not rotate passwords in an existing database. See [existing-database maintenance](../implementer-guide/security.md#existing-database-credentials).

## API service

Type: reference.

| Variable | Meaning |
|----------|---------|
| `SPRING_PROFILES_ACTIVE=docker` | Reads the datasource and optional service settings below; also usable for native local development |
| `SPRING_DATASOURCE_URL` | Full JDBC URL in the docker profile |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Existing database credentials used by the API |
| `JWT_SECRET_KEY` | Signing key; required by the application; example is evaluation-only |
| `AWS_REGION` | AWS client region; local Compose defaults to `us-east-1` |
| `AWS_EC2_METADATA_DISABLED=true` | Prevents metadata credential discovery in local evaluation |
| `AWS_S3_BUCKET` | Bucket owned/configured by the operator; local default is an unusable placeholder |
| `GOOGLE_MAP_API_KEY` | Map service configuration; blank by default in local Docker profile |
| `API_PORT` | Full Compose API host port; loopback only; default 8080 |
| `DATABASE_URL` | Legacy base-profile JDBC setting; use the explicit docker datasource variables for the documented local path |

Media and map operations require additional operator-owned service configuration. Do not copy credentials or bucket names from another deployment. The local health check exposes status only at `/actuator/health`.

## Admin dashboard

Type: reference.

| Variable | Scope | Meaning |
|----------|-------|---------|
| `API_URL` | Container nginx runtime | Full API origin including scheme and port, reachable from nginx; full Compose supplies `http://api:8080` |
| `ADMIN_PORT` | Full Compose host | Loopback dashboard port; default 3000 |
| `BACKEND_PROXY_TARGET` | Vite development/preview process | API origin for `/api` and `/admin` proxies; default `http://localhost:8080` |

Browsers send API requests to the dashboard origin. A container-internal hostname is not a mobile endpoint. Changing container runtime variables requires recreating the affected container; `docker compose restart` alone is not a configuration update procedure.

## Android build configuration

Type: reference.

These public values are embedded in the app and must not contain secrets:

| Variable | Meaning |
|----------|---------|
| `EXPO_PUBLIC_APP_NAME` | Display name |
| `EXPO_PUBLIC_APP_VERSION` | Component build label; not a repository-wide release policy |
| `EXPO_PUBLIC_API_URL` | Your API origin reachable from the device |
| `EXPO_PUBLIC_ANDROID_PACKAGE` | Android identity; profile-specific names permit side-by-side installs |

Use the [mobile build guide](../developer-guide/mobile-app.md#building-apks) for profile rules. Development and preview builds require an explicitly selected isolated endpoint. A distinct package name does not isolate data when two builds share an API.
