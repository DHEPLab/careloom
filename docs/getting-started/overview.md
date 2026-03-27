# Architecture Overview

This document describes how CareLoom's services fit together, the data model, and key technical decisions.

## System Architecture

CareLoom is a three-service mono-repo. The API is the central service; both the admin dashboard and mobile app communicate with it over HTTP.

```
                                    Internet
                                       |
                    +------------------+------------------+
                    |                                     |
             Program Managers                       CHWs (field)
                    |                                     |
            +-------+--------+                  +---------+---------+
            | Admin Dashboard |                  |    Mobile App     |
            | (React/Vite)   |                  | (Expo/React Native)|
            | Port 3000      |                  |    Android APK     |
            +-------+--------+                  +---------+---------+
                    |                                     |
                    |          REST API (JSON)             |
                    +------------------+------------------+
                                       |
                              +--------+--------+
                              |   API Service    |
                              | (Spring Boot)    |
                              |   Port 8080      |
                              +--------+---------+
                                       |
                                  JDBC / JPA
                                       |
                              +--------+---------+
                              |     MySQL 5.7    |
                              |   Port 3306      |
                              +------------------+
```

### Service Responsibilities

**API Service** (`services/api`)
- Authentication and authorization (JWT)
- Curriculum CRUD operations
- Visit scheduling and tracking
- CHW and family (baby/carer) management
- Questionnaire management
- Report generation and data export
- Database migrations via Flyway

**Admin Dashboard** (`services/admin-web`)
- Curriculum design (modules, components, branching logic)
- CHW account management and assignment
- Visit monitoring and reporting
- Project and tag management
- Questionnaire builder

**Mobile App** (`services/app`)
- Home visit execution with curriculum content
- Offline-first data storage (visits can be completed without connectivity)
- Visit scheduling and reminders
- Family information collection
- Data sync when connectivity is restored

## Authentication

CareLoom uses JWT (JSON Web Token) authentication.

### Flow

```
1. Client sends POST /api/authenticate
   Body: { "username": "...", "password": "..." }

2. API validates credentials against the User table
   Passwords are stored as bcrypt hashes

3. API returns a JWT signed with JWT_SECRET_KEY
   Token contains: username, roles, expiration

4. Client includes token in subsequent requests
   Header: Authorization: Bearer <token>

5. API validates token signature and expiration on each request
```

### Roles

| Role | Description | Access |
|------|-------------|--------|
| `SUPER_ADMIN` | System administrator | Full access to all projects and settings |
| `ADMIN` | Program administrator | Manage curricula, CHWs, and view reports within a project |
| `SUPERVISOR` | CHW supervisor | Monitor CHW activity and visit completion |
| `CHW` | Community health worker | Mobile app access for conducting visits |

## Data Model

### Core Entities

```
Project
  |
  +-- Curriculum (name, description, status: DRAFT/PUBLISHED)
  |     |
  |     +-- Module (number, topic, components[])
  |     |     |
  |     |     +-- Component (type: Text | Media | Switch | PageFooter)
  |     |
  |     +-- Questionnaire (branching logic)
  |
  +-- User (username, role, project assignment)
  |     |
  |     +-- CommunityHouseWorker (CHW profile, linked to User)
  |
  +-- Baby (identity, stage: UNBORN | BORN, area, location)
  |     |
  |     +-- Carer (family tie: February, MOTHER, GRANDMOTHER, etc.)
  |
  +-- Visit (baby, lesson, status, scheduled time)
        |
        +-- VisitReport (completion data)
        +-- QuestionnaireRecord (responses)
```

### Key Relationships

- A **Project** groups curricula, users, and families for a single deployment or study site
- A **Curriculum** contains ordered **Modules**, each with rich content **Components**
- **Modules** support branching via `CurriculumBranch` (MASTER or variant paths)
- A **Visit** links a **Baby** to a **Lesson** (exported snapshot of a module) at a scheduled time
- **CHWs** are assigned to families and conduct visits according to the curriculum schedule
- The system maintains full **history tables** (e.g., `BabyHistory`, `VisitHistory`) for audit trails

### Curriculum Content Model

Modules are the building blocks of a curriculum. Each module contains an ordered list of **Components**:

| Component Type | Description |
|----------------|-------------|
| `Text` | Rich text content displayed to the CHW during a visit |
| `Media` | Images or other media embedded in the visit flow |
| `Switch` | Conditional branching based on CHW input (e.g., baby age, responses) |
| `PageFooter` | Navigation and action buttons at the bottom of a page |

Modules are versioned. When a curriculum is published, the current state of each module is exported as a `Lesson` (an immutable snapshot). This ensures that in-progress visits are not disrupted by curriculum edits.

### Stage-Based Scheduling

Families are tracked through two stages:
- **UNBORN** -- prenatal visits on a pregnancy-based schedule
- **BORN** -- postnatal visits on an age-based schedule

The system calculates visit schedules based on the baby's stage and the curriculum's module sequence.

## Database

- **Engine:** MySQL 5.7
- **ORM:** JPA (Hibernate) via Spring Data
- **Migrations:** Flyway with 62 versioned SQL migrations
- **Soft deletes:** Most entities use a `deleted` boolean flag rather than hard deletes
- **Audit fields:** `createdDate`, `lastModifiedDate`, `createdBy`, `lastModifiedBy` on all entities

### Migration Naming Convention

Migrations follow the pattern `V{timestamp}__{description}.sql`:
```
V1__healthy.sql                              (initial schema)
V20201013134244__modify_table_lesson_schedule.sql
V20241010162200__alter_care_history_wechat.sql  (latest)
```

Never modify an existing migration file. To change the schema, add a new migration.

## API Conventions

- Base path: `/api/`
- Admin endpoints: `/api/admin/`
- Authentication: `/api/authenticate`
- Health check: `/actuator/health`
- Request/response format: JSON
- Pagination: Spring Data's `Pageable` (page, size, sort parameters)

## Internationalization

The admin dashboard and mobile app support:
- **English** (default)
- **Chinese (Mandarin)**

Translations are managed via:
- Admin dashboard: React i18n files in `services/admin-web/src/`
- Mobile app: i18n files in `services/app/`
- API: Error messages support locale-based responses

Adding a new language requires updating translation files in both frontend services.
