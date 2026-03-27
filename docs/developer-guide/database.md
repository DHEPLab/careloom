# Database

CareLoom uses MySQL 5.7 with Flyway for schema migrations. The API service connects via JPA (Hibernate).

## Running MySQL Locally

```bash
# Start MySQL in Docker
docker compose up -d

# Connection details
Host: localhost
Port: 3306
Database: careloom
User: careloom
Password: careloom
```

## Schema Overview

The database organizes around these core domain areas:

### Projects and Users

| Table | Description |
|-------|-------------|
| `project` | Top-level container grouping curricula, users, and families |
| `user` | All user accounts (admins, supervisors, CHWs) |
| `community_house_worker` | CHW-specific profile data, linked to a user |

### Curriculum

| Table | Description |
|-------|-------------|
| `curriculum` | Curriculum definition (name, status: DRAFT/PUBLISHED) |
| `module` | A single visit session within a curriculum (ordered, with topic and components) |
| `lesson` | Immutable snapshot of a module, created when curriculum is published |
| `lesson_schedule` | Maps lessons to baby ages (when each visit should occur) |
| `questionnaire` | Survey instruments attached to curricula |
| `tag` | Tags for categorizing content |

### Families and Visits

| Table | Description |
|-------|-------------|
| `baby` | Family record (baby name, stage: UNBORN/BORN, location) |
| `carer` | Caregiver associated with a baby (mother, grandmother, etc.) |
| `visit` | A scheduled or completed home visit linking a baby to a lesson |
| `visit_report` | Completion data for a visit |
| `visit_position_record` | GPS coordinates recorded during a visit |
| `questionnaire_record` | Questionnaire responses collected during a visit |

### System

| Table | Description |
|-------|-------------|
| `error_log` | Application error tracking |
| `account_operation_record` | Audit log of account changes |

### History Tables

Most core tables have corresponding history tables (e.g., `baby_history`, `visit_history`, `curriculum_history`). These capture previous states of records for auditing.

| History Table | Tracks Changes To |
|---------------|-------------------|
| `baby_history` | `baby` |
| `carer_history` | `carer` |
| `community_house_worker_history` | `community_house_worker` |
| `curriculum_history` | `curriculum` |
| `lesson_history` | `lesson` |
| `lesson_schedule_history` | `lesson_schedule` |
| `module_history` | `module` |
| `questionnaire_history` | `questionnaire` |
| `tag_history` | `tag` |
| `user_history` | `user` |
| `visit_history` | `visit` |
| `visit_report_history` | `visit_report` |
| `baby_modify_record` | Tracks specific field changes on baby records |

## Key Relationships

```
project
  ├── curriculum ──── module ──── (published as) ──── lesson
  │                                                      │
  │                                              lesson_schedule
  │
  ├── user ──── community_house_worker
  │
  └── baby ──── carer
         │
         └── visit ──── visit_report
                   ├── questionnaire_record
                   └── visit_position_record
```

- A **project** contains curricula, users, and babies.
- A **curriculum** contains ordered **modules**. Publishing creates immutable **lessons**.
- **Lesson schedules** map lessons to baby ages.
- A **visit** connects a baby to a lesson at a scheduled time.
- **CHWs** (via `community_house_worker`) are linked to `user` accounts and assigned to babies.

## Entity Conventions

All entities share these patterns:

- **Soft deletes:** Most tables have a `deleted` boolean column. Records are marked as deleted rather than removed.
- **Audit fields:** `created_date`, `last_modified_date`, `created_by`, `last_modified_by` are populated automatically by JPA auditing.
- **Auto-increment IDs:** All primary keys use `BIGINT AUTO_INCREMENT`.

## Key Enumerations

| Enum | Values | Used In |
|------|--------|---------|
| `BabyStage` | `UNBORN`, `BORN` | Baby stage tracking |
| `VisitStatus` | Status values for visit lifecycle | Visit records |
| `CurriculumStatus` | `DRAFT`, `PUBLISHED` | Curriculum publishing state |
| `CurriculumBranch` | `MASTER` and variants | Branching logic |
| `ModuleComponentType` | `Text`, `Media`, `Switch`, `PageFooter` | Module content blocks |
| `FamilyTies` | `MOTHER`, `GRANDMOTHER`, etc. | Carer relationship to baby |
| `Gender` | `MALE`, `FEMALE` | Baby gender |

## Flyway Migrations

Migration files are in `services/api/src/main/resources/db/migration/`.

### Naming Convention

```
V{YYYYMMDDHHmmss}__{description}.sql
```

The initial schema migration is `V1__healthy.sql`. Subsequent migrations use timestamps.

### Rules

1. **Never modify an existing migration.** Flyway checksums each file; changes cause startup failures.
2. **Always add a new migration** for any schema change.
3. **Use descriptive names** (e.g., `V20260327120000__add_example_table.sql`).
4. **Test on a fresh database** before submitting.

### Checking Migration Status

```bash
# Connect to the database
docker exec -it careloom-db mysql -u careloom -pcareloom careloom

# View applied migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Resetting for Development

If you need a clean database during development:

```bash
docker compose down -v    # Removes the data volume
docker compose up -d      # Fresh database, migrations re-run on API start
```
