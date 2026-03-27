# Security

This guide covers essential security practices for running CareLoom in production.

## Change Default Passwords

CareLoom ships with default credentials that must be changed before going live.

### Admin Dashboard Password

1. Log in with the default credentials (`admin` / `admin`).
2. Go to the profile area (top-right corner).
3. Click **Change Password**.
4. Set a strong password (at least 12 characters, mix of letters, numbers, and symbols).

### Database Passwords

Database passwords are set in the `.env` file used during deployment. Change these values from their defaults:

| Variable | Default | Action |
|----------|---------|--------|
| `MYSQL_ROOT_PASSWORD` | `careloom` | Change to a strong, unique password |
| `MYSQL_PASSWORD` | `careloom` | Change to a strong, unique password |

After changing `.env` values, restart the services:

```bash
docker compose -f docker-compose.full.yml down
docker compose -f docker-compose.full.yml up -d
```

### JWT Secret Key

The JWT secret key signs authentication tokens. The default value (`dev-secret-do-not-use-in-prod`) is insecure.

Generate a secure key:

```bash
openssl rand -hex 32
```

Set this value as `JWT_SECRET_KEY` in your `.env` file and restart the services.

## HTTPS Setup

CareLoom does not include built-in HTTPS. For production deployments, place a reverse proxy in front of the admin dashboard and API.

### Using Nginx with Let's Encrypt

1. Install Nginx and Certbot on your server.
2. Obtain an SSL certificate:
   ```bash
   certbot --nginx -d admin.yourorg.example.com
   ```
3. Configure Nginx to proxy requests to the CareLoom services:
   ```nginx
   server {
       listen 443 ssl;
       server_name admin.yourorg.example.com;

       ssl_certificate /etc/letsencrypt/live/admin.yourorg.example.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/admin.yourorg.example.com/privkey.pem;

       location / {
           proxy_pass http://localhost:3000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }

       location /api/ {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```
4. Set up automatic certificate renewal:
   ```bash
   certbot renew --dry-run
   ```

### Mobile App API URL

After setting up HTTPS, update the mobile app's API URL to use the HTTPS endpoint. This is configured when building the APK through the `EXPO_PUBLIC_API_URL` environment variable.

## Backup Procedures

### Database Backups

Back up the MySQL database regularly. With Docker:

```bash
# Create a backup
docker exec careloom-db mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" careloom > backup_$(date +%Y%m%d).sql

# Restore from backup
docker exec -i careloom-db mysql -u root -p"$MYSQL_ROOT_PASSWORD" careloom < backup_20250101.sql
```

Set up a daily cron job for automated backups:

```bash
# Add to crontab (crontab -e)
0 2 * * * docker exec careloom-db mysqldump -u root -p"YOUR_PASSWORD" careloom > /backups/careloom_$(date +\%Y\%m\%d).sql
```

### Media File Backups

If you are using AWS S3 for media storage, files are stored in the configured S3 bucket. Enable S3 versioning for protection against accidental deletions.

If using local storage, back up the media directory alongside database backups.

## Access Control

### Role-Based Access

CareLoom has four roles with increasing levels of access:

| Role | What They Can Do |
|------|-----------------|
| **CHW** | Mobile app only. Conduct visits, register families, sync data. |
| **Supervisor** | Monitor CHW activity, view visit reports for their project. |
| **Admin** | Manage curricula, users, and reports within an assigned project. |
| **Super Admin** | Full access to all projects and system settings. |

### Best Practices

- **Minimize Super Admin accounts.** Only one or two people should have Super Admin access.
- **Use project-level isolation.** If you run multiple programs, create separate projects. Admins assigned to one project cannot see data from another.
- **Review user accounts quarterly.** Remove accounts for staff who have left the program.
- **Do not share accounts.** Each person should have their own login credentials.

## Network Security

- **Do not expose the database port (3306) to the public internet.** In Docker, this is handled by only mapping ports to `localhost` or removing the port mapping entirely in production.
- **Use a firewall.** Only expose ports 80 (HTTP) and 443 (HTTPS) to the internet if using a reverse proxy.
- **Keep software updated.** Regularly pull the latest CareLoom Docker images and update your server's operating system.

## Data Handling

CareLoom may store sensitive health and family data. Follow your organization's data protection policies and any applicable regulations (local data protection laws, IRB requirements for research use).

- Store backups in encrypted, access-controlled storage.
- Limit who has database access to only those who need it.
- If using CareLoom for research, ensure your data handling meets IRB requirements.
