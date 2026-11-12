# Security

This guide covers essential security practices for running CareLoom in production.

## Change Default Passwords

CareLoom ships with default credentials that must be changed before going live.

### Admin Dashboard Password

1. Log in with the default credentials (`admin` / `admin`).
2. Go to the profile area (top-right corner).
3. Click **Change Password**.
4. Set a strong password (at least 12 characters, mix of letters, numbers, and symbols).

### New database credentials

Type: how-to.

Before the first startup of a new database, set `MYSQL_ROOT_PASSWORD` and `MYSQL_PASSWORD` in your deployment's private configuration. The checked-in example values are only for isolated synthetic evaluation. Keep real credentials outside Git and public logs.

Recovery: If a database volume already exists, stop using the initialization procedure and follow the existing-database procedure below. Do not delete its volume to change a password.

### Existing database credentials

Type: explanation.

Editing `.env` and restarting containers does **not** rotate passwords in an existing MySQL database. The [official MySQL image documentation](https://hub.docker.com/_/mysql) states that initialization environment variables do not change an already populated data directory.

For an operational database, the database owner must plan a maintenance window, verify a restorable backup, identify the affected MySQL account and host, change that account through their approved database procedure, and update the API's connection configuration in coordination. Verify a new connection before retiring the old credential. Root credentials and application-user credentials are separate accounts. This repository does not supply a tested operational rotation script.

If credentials in `.env` and the database differ, treat that as a configuration mismatch. Do not retry password guesses, delete volumes or run broad reset commands. Have the database owner reconcile the configuration.

### JWT Secret Key

The JWT secret key signs authentication tokens. The checked-in evaluation example is public and unsuitable for operational use.

Generate a secure key:

```bash
openssl rand -hex 32
```

For a new deployment, set the generated value as `JWT_SECRET_KEY` in private configuration before startup. For an existing deployment, have the operator coordinate the key change and recreate the API container with the new environment. A key change affects existing tokens; a plain container restart does not reload Compose configuration.

## HTTPS and endpoint configuration

Type: reference.

The local Compose path binds the dashboard/API to loopback and keeps MySQL on its internal network. It does not provision public TLS. Before an operational deployment, have the infrastructure owner configure HTTPS, database access, backups and host/network controls for that environment.

- Container `API_URL` is the full internal API origin used by nginx for both `/admin/` and `/api/` paths. A browser uses the dashboard origin.
- Android builds use `EXPO_PUBLIC_API_URL`, the API endpoint reachable from that device. Select your own deployment and verify its certificate and identity before distribution.
- Dev/preview builds must use an isolated test backend. Different Android package names do not make a shared backend safe for testing.
- Do not expose a database port to the internet. Changing the local Compose bind address is an infrastructure decision, not a setup shortcut.

## Backup and restore preparation

Type: how-to.

1. In the deployment's operations record, identify the database, media store, owner and retention rules.
   Recovery: If any destination or owner is unknown, keep the deployment in synthetic evaluation.
2. In the owner's backup system, configure encrypted, access-controlled backups of both database and uploaded media.
   Recovery: If a media service is not configured, do not claim media backup coverage. Local placeholder settings are not a media store.
3. In an isolated restore environment, restore a backup and verify synthetic or authorized validation records and media.
   Recovery: If verification fails, resolve the backup/restore process before operational use. Do not test a restore over the running database.

This repository does not include a tested operational backup scheduler or rotation script. Use project-scoped Compose service names when identifying a local container; do not assume a global `careloom-db` name. Keep credentials out of cron command lines and shared logs.

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
- **Verify project access with synthetic accounts.** Create the intended projects and test each role's allowed and denied actions before using real data. A role description is not proof that every endpoint enforces the intended boundary.
- **Review user accounts quarterly.** Remove accounts for staff who have left the program.
- **Do not share accounts.** Each person should have their own login credentials.

## Network Security

- **Do not expose the database port (3306) to the public internet.** The full local Compose file leaves MySQL unexposed on the host.
- **Use a firewall.** Only expose ports 80 (HTTP) and 443 (HTTPS) to the internet if using a reverse proxy.
- **Review updates before applying them.** Use a reviewed source commit or approved artifact and test migrations, compatibility and rollback in isolation. No prebuilt image release is established by this guide.

## Data Handling

CareLoom may store sensitive health and family data. Follow your organization's data protection policies and any applicable regulations (local data protection laws, IRB requirements for research use).

- Store backups in encrypted, access-controlled storage.
- Limit who has database access to only those who need it.
- If using CareLoom for research, ensure your data handling meets IRB requirements.
