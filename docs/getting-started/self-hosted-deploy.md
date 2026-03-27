# Self-Hosted Deploy

Run CareLoom on your own server using Docker Compose. This is the recommended approach for organizations that need full control over their data.

## Prerequisites

- **Docker Desktop** (or Docker Engine + Docker Compose v2) -- [Install Docker](https://docs.docker.com/get-docker/)
- **Git**
- **2 GB RAM** minimum (4 GB recommended)
- Ports 3000, 3306, and 8080 available

## Quick Start

```bash
# Clone the repository
git clone https://github.com/DHEPLab/careloom.git
cd careloom

# Copy and edit environment variables
cp .env.example .env
# Edit .env with your preferred editor — change ALL default values for production

# Start all services (DB + API + Admin Dashboard)
docker compose -f docker-compose.full.yml up --build -d
```

Wait 1-2 minutes for the API to finish running database migrations, then access:

| Service | URL | Notes |
|---------|-----|-------|
| Admin Dashboard | http://localhost:3000 | Program manager interface |
| API | http://localhost:8080 | Backend API |
| MySQL | localhost:3306 | Database (not exposed in production) |

Default login: **admin** / **admin**

## Environment Variables

All variables are defined in `.env`. Copy `.env.example` and change every value before deploying to production.

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `careloom` | MySQL root password |
| `MYSQL_DATABASE` | `careloom` | Database name |
| `MYSQL_USER` | `careloom` | Database user for the API |
| `MYSQL_PASSWORD` | `careloom` | Database user password |
| `JWT_SECRET_KEY` | `dev-secret-do-not-use-in-prod` | JWT signing secret. Generate with `openssl rand -hex 32` |
| `API_URL` | `http://localhost:8080` | API URL for the admin dashboard to connect to |

## Deploy to a VPS

For a production deployment on a Linux server (Ubuntu 22.04+ recommended).

### 1. Server Setup

```bash
# Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# Log out and back in for group change to take effect

# Install Caddy (reverse proxy with automatic HTTPS)
sudo apt install -y debian-keyring debian-archive-keyring apt-transport-https
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' | sudo tee /etc/apt/sources.list.d/caddy-stable.list
sudo apt update
sudo apt install caddy
```

### 2. Clone and Configure

```bash
git clone https://github.com/DHEPLab/careloom.git /opt/careloom
cd /opt/careloom

cp .env.example .env
# IMPORTANT: Change ALL values in .env
# Generate a strong JWT secret:
#   openssl rand -hex 32
```

### 3. Start Services

```bash
docker compose -f docker-compose.full.yml up --build -d
```

### 4. Configure Caddy (Reverse Proxy + HTTPS)

Create `/etc/caddy/Caddyfile`:

```
careloom.example.com {
    handle /api/* {
        reverse_proxy localhost:8080
    }
    handle /actuator/* {
        respond 404
    }
    handle {
        reverse_proxy localhost:3000
    }
}

api.careloom.example.com {
    reverse_proxy localhost:8080
}
```

Replace `careloom.example.com` with your domain. Caddy automatically provisions TLS certificates via Let's Encrypt.

```bash
sudo systemctl reload caddy
```

### 5. Update API_URL

Edit `.env` and set `API_URL` to your public API URL:

```
API_URL=https://api.careloom.example.com
```

Restart the admin dashboard to pick up the change:

```bash
docker compose -f docker-compose.full.yml up -d --no-deps admin-web
```

## Managing the Deployment

### View Logs

```bash
# All services
docker compose -f docker-compose.full.yml logs -f

# Single service
docker compose -f docker-compose.full.yml logs -f api
```

### Stop Services

```bash
docker compose -f docker-compose.full.yml down
```

### Restart Services

```bash
docker compose -f docker-compose.full.yml restart
```

### Update to Latest Version

```bash
cd /opt/careloom
git pull
docker compose -f docker-compose.full.yml up --build -d
```

### Database Backup

```bash
docker exec careloom-db mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" careloom > backup-$(date +%Y%m%d).sql
```

### Database Restore

```bash
docker exec -i careloom-db mysql -u root -p"$MYSQL_ROOT_PASSWORD" careloom < backup-20250101.sql
```

## Troubleshooting

### Port 3306 already in use
Another MySQL instance is running on the host. Either stop it (`sudo systemctl stop mysql`) or change the host port mapping in `docker-compose.full.yml`:
```yaml
ports:
  - "3307:3306"  # Map to 3307 on the host
```

### API won't start / "Communications link failure"
The API depends on MySQL being fully ready. Check that the database container is healthy:
```bash
docker compose -f docker-compose.full.yml ps
```
If the `db` service shows `unhealthy`, check its logs for errors. Common cause: insufficient disk space or memory.

### Out of memory
MySQL and the Spring Boot API together need at least 1.5 GB of RAM. If running on a constrained server, add swap:
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### Admin dashboard loads but shows blank page
Check browser console for errors. The most common cause is `API_URL` pointing to the wrong address. The admin dashboard container needs to reach the API container. When using Docker Compose, the default `http://api:8080` should work. When accessing from outside Docker (e.g., via Caddy), use the public URL.

### Flyway migration errors
Never modify existing migration files. If a migration fails partway, you may need to manually fix the database state and update the `flyway_schema_history` table. Check the API logs for the specific migration that failed.
