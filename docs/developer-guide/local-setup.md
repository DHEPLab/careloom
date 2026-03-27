# Local Setup

This guide walks you through running CareLoom on your development machine.

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Java | JDK 8+ | The API targets Java 8 (`sourceCompatibility = '1.8'`). JDK 11 or 17 also work. |
| Node.js | 20+ | Required by admin-web (`engines.node >= 20.0.0`) |
| Yarn | 1.x (Classic) | Used by admin-web and mobile app |
| Docker | Latest | For running MySQL locally |
| Android Studio or Expo Go | Latest | For running the mobile app (Android only) |

## Clone the Repository

```bash
git clone https://github.com/DHEPLab/careloom.git
cd careloom
```

## Start the Database

Use Docker Compose to start a MySQL 5.7 instance:

```bash
docker compose up -d
```

This starts MySQL on port 3306 with default credentials:
- **Database:** `careloom`
- **User:** `careloom`
- **Password:** `careloom`
- **Root password:** `careloom`

## Run the API

```bash
cd services/api

# Create a local config file (first time only)
cp src/main/resources/application-local.sample.yml src/main/resources/application-local.yml
# Edit application-local.yml with your database credentials if different from defaults

# Run with dev + local profiles
DATABASE_URL="jdbc:mysql://localhost:3306/careloom?useSSL=false&allowPublicKeyRetrieval=true" \
JWT_SECRET_KEY="dev-secret-change-me" \
GOOGLE_MAP_API_KEY="optional" \
./gradlew bootRun --args='--spring.profiles.active=dev,local'
```

The API starts on **http://localhost:8080**. Flyway runs database migrations automatically on startup.

Verify it is running:

```bash
curl http://localhost:8080/actuator/health
```

## Run the Admin Dashboard

```bash
cd services/admin-web

# Install dependencies
yarn install

# Start the dev server
yarn start
```

The admin dashboard starts on **http://localhost:5173** (Vite's default). It proxies API requests to `http://localhost:8080`.

Log in with the default credentials: `admin` / `admin`.

## Run the Mobile App

```bash
cd services/app

# Install dependencies
yarn install

# Start Expo
npx expo start
```

Options for running the mobile app:
- **Android Emulator:** Press `a` in the Expo CLI to open in a connected Android emulator
- **Physical device:** Install Expo Go on your Android phone and scan the QR code
- **Development build:** See [Mobile App](mobile-app.md) for building a custom dev client

Set the API URL for local development:

```bash
EXPO_PUBLIC_API_URL=http://<your-local-ip>:8080 npx expo start
```

Use your machine's local network IP (not `localhost`) so the mobile device can reach the API.

## Running All Three Together

For typical development, you need three terminal windows:

```
Terminal 1: docker compose up        (database)
Terminal 2: cd services/api && ./gradlew bootRun ...  (API on :8080)
Terminal 3: cd services/admin-web && yarn start        (dashboard on :5173)
```

Add a fourth terminal for the mobile app if you are working on it.

## Common Issues

### Port conflicts

If port 3306, 8080, or 5173 is already in use, either stop the conflicting service or change the port:
- MySQL: edit `docker-compose.yml` port mapping
- API: set `server.port` in `application-local.yml`
- Admin-web: Vite will automatically try the next available port

### Database connection refused

Make sure Docker is running and MySQL has finished starting:

```bash
docker compose ps    # Check container status
docker compose logs db  # Check MySQL logs
```

### Flyway migration errors

If you see Flyway errors on API startup, the database may have stale migration state. For local development, you can reset:

```bash
docker compose down -v   # Removes the database volume
docker compose up -d     # Fresh database
```

### Gradle wrapper permission denied

```bash
chmod +x services/api/gradlew
```
