# API Reference

The CareLoom API is a REST service running on port 8080. All endpoints accept and return JSON. Authentication uses JWT Bearer tokens.

## Base URL

```
http://localhost:8080/api
```

In production, this will be behind your reverse proxy (e.g., `https://api.yourorg.example.com/api`).

## Authentication

### Login

```
POST /api/authenticate
```

Request body:

```json
{
  "username": "admin",
  "password": "admin"
}
```

Returns a JWT token. Include this token in all subsequent requests:

```
Authorization: Bearer <token>
```

Tokens are valid for 30 days by default.

## Endpoint Groups

### Authentication & Account

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| `POST` | `/api/authenticate` | Login, returns JWT | No |
| `GET` | `/api/account` | Get current user info | Yes |
| `POST` | `/api/account/change-password` | Change password | Yes |

### Admin: Users

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/users` | List all users (paginated) |
| `POST` | `/api/admin/users` | Create a new user |
| `GET` | `/api/admin/users/{id}` | Get user by ID |
| `PUT` | `/api/admin/users/{id}` | Update a user |
| `DELETE` | `/api/admin/users/{id}` | Delete a user |

### Admin: Curricula

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/curriculums` | List curricula |
| `POST` | `/api/admin/curriculums` | Create a curriculum |
| `GET` | `/api/admin/curriculums/{id}` | Get curriculum by ID |
| `PUT` | `/api/admin/curriculums/{id}` | Update a curriculum |
| `DELETE` | `/api/admin/curriculums/{id}` | Delete a curriculum |

### Admin: Modules

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/modules` | List modules |
| `POST` | `/api/admin/modules` | Create a module |
| `GET` | `/api/admin/modules/{id}` | Get module by ID |
| `PUT` | `/api/admin/modules/{id}` | Update a module |
| `DELETE` | `/api/admin/modules/{id}` | Delete a module |

### Admin: Babies (Families)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/babies` | List all families (paginated) |
| `GET` | `/api/admin/babies/{id}` | Get baby/family by ID |

### Admin: Carers

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/carers` | List carers |

### Admin: Projects

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/projects` | List projects |
| `POST` | `/api/admin/projects` | Create a project |

### Admin: Questionnaires

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/questionnaires` | List questionnaires |
| `POST` | `/api/admin/questionnaires` | Create a questionnaire |

### Admin: Tags

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/tags` | List tags |
| `POST` | `/api/admin/tags` | Create a tag |

### Admin: Reports

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/admin/reports` | Generate reports |

### Admin: Files

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/admin/files` | Upload a file (media for curriculum content) |

### Admin: Init

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/admin/init` | Initialize system (first-time setup) |

### CHW/App Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/babies` | Get babies assigned to current CHW |
| `POST` | `/api/babies` | Register a new baby |
| `GET` | `/api/visits` | Get visits for current CHW |
| `POST` | `/api/visits` | Create/complete a visit |
| `GET` | `/api/lesson-resources` | Get curriculum lesson resources |
| `POST` | `/api/questionnaire-records` | Submit questionnaire responses |

### System

| Method | Path | Description | Auth Required |
|--------|------|-------------|---------------|
| `GET` | `/actuator/health` | Health check | No |

## Pagination

Paginated endpoints use Spring Data's `Pageable` convention:

| Parameter | Description | Example |
|-----------|-------------|---------|
| `page` | Page number (0-indexed) | `?page=0` |
| `size` | Items per page | `?size=20` |
| `sort` | Sort field and direction | `?sort=createdDate,desc` |

Response includes pagination metadata:

```json
{
  "content": [...],
  "totalPages": 5,
  "totalElements": 100,
  "number": 0,
  "size": 20
}
```

## Error Responses

Errors follow the Problem Details format (RFC 7807):

```json
{
  "type": "https://www.jhipster.tech/problem/problem-with-message",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "path": "/api/admin/users"
}
```

## Swagger UI

The API includes Swagger UI for interactive documentation. When the API is running, access it at:

```
http://localhost:8080/swagger-ui.html
```

Swagger provides a complete, auto-generated reference of all endpoints, request/response schemas, and allows you to test calls directly from the browser.

## Notes

- All admin endpoints (`/api/admin/*`) require an authenticated user with the `ADMIN` or `SUPER_ADMIN` role.
- App endpoints (`/api/babies`, `/api/visits`, etc.) require an authenticated user with any role and return data scoped to the current user's assignments.
- File uploads use `multipart/form-data`.
- Most delete operations are soft deletes (records are marked as deleted, not removed).
