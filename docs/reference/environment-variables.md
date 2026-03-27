# Environment Variables

All environment variables used across CareLoom services, with descriptions, defaults, and which service uses them.

## Database (Docker Compose)

These variables are used in `docker-compose.yml` and `docker-compose.full.yml` to configure the MySQL container.

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `careloom` | MySQL root password |
| `MYSQL_DATABASE` | `careloom` | Database name created on first startup |
| `MYSQL_USER` | `careloom` | Application database user |
| `MYSQL_PASSWORD` | `careloom` | Application database user password |

## API Service (`services/api`)

Set these when running the API, either as environment variables or in `application-local.yml`.

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | (none, required) | JDBC connection URL. Example: `jdbc:mysql://localhost:3306/careloom?useSSL=false&allowPublicKeyRetrieval=true` |
| `JWT_SECRET_KEY` | (none, required) | Secret key for signing JWT tokens. Generate with `openssl rand -hex 32` |
| `GOOGLE_MAP_API_KEY` | (none, optional) | Google Maps API key for location features |
| `SPRING_PROFILES_ACTIVE` | (none) | Comma-separated Spring profiles to activate (e.g., `dev,local` or `docker`) |
| `SPRING_DATASOURCE_URL` | (none) | Alternative datasource URL used in Docker profile, overrides `DATABASE_URL` |
| `SPRING_DATASOURCE_USERNAME` | (none) | Database username, used in Docker profile |
| `SPRING_DATASOURCE_PASSWORD` | (none) | Database password, used in Docker profile |

### API Configuration (application.yml)

These are configured in YAML files rather than environment variables but are important to know:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | HTTP port the API listens on |
| `application.token-validity-day` | `30` | Number of days a JWT token remains valid |
| `application.aws.bucket-name` | `visit-link-bucket-dev` | AWS S3 bucket name for media storage |
| `application.cron.visit-expired` | `0 59 23 * * ?` | Cron expression for the visit expiration scheduled task |

## Admin Dashboard (`services/admin-web`)

| Variable | Default | Description |
|----------|---------|-------------|
| `API_URL` | `http://api:8080` | URL of the API service. Used in the Docker container's Nginx proxy config. |

In development, the API URL is configured in `vite.config.ts` (proxy settings).

## Mobile App (`services/app`)

These are set at build time and baked into the APK. Configure them in `eas.json` under each build profile.

| Variable | Default | Description |
|----------|---------|-------------|
| `EXPO_PUBLIC_APP_NAME` | `Healthy Future` | Display name of the mobile app |
| `EXPO_PUBLIC_APP_VERSION` | (from `package.json`) | Version string shown in the app |
| `EXPO_PUBLIC_API_URL` | `https://healthy-future.dhep.org` | Backend API URL the app connects to |
| `EXPO_PUBLIC_ANDROID_PACKAGE` | `edu.stanford.fsi.reap.app` | Android package name (different per build profile) |

### Build Profiles (eas.json)

| Profile | App Name | API URL | Package |
|---------|----------|---------|---------|
| `production` | Healthy Future | `https://healthy-future.dhep.org` | `edu.stanford.fsi.reap.app` |
| `preview` | Healthy Future | `https://healthy-future.dhep.org` | `edu.stanford.fsi.reap.apppreview` |
| `development` | Healthy Future(Dev) | `https://healthy-future.dhep.org` | `edu.stanford.fsi.reap.appdev` |

Different package names allow installing multiple build profiles side-by-side on the same device.

## Docker Compose Full Stack (.env)

When using `docker-compose.full.yml`, all variables are read from a `.env` file. Copy `.env.example` and edit:

```bash
cp .env.example .env
```

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `careloom` | MySQL root password |
| `MYSQL_DATABASE` | `careloom` | Database name |
| `MYSQL_USER` | `careloom` | Database user |
| `MYSQL_PASSWORD` | `careloom` | Database user password |
| `JWT_SECRET_KEY` | `dev-secret-do-not-use-in-prod` | JWT signing secret |

For production deployments, **change all default values**.

## Spring Profiles Reference

| Profile | Purpose | Key Differences |
|---------|---------|-----------------|
| `dev` | Local development | Debug-level logging |
| `local` | Local overrides | Developer-specific settings (not committed) |
| `docker` | Docker containers | Reads datasource config from environment variables |
| `stg` | Staging | Staging-specific settings |
| `prod` | Production | Production-optimized settings |
