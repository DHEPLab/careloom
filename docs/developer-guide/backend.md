# Backend (API Service)

The API service is a Spring Boot 2.2.6 application targeting Java 8. It serves REST endpoints consumed by both the admin dashboard and the mobile app.

## Running Locally

```bash
cd services/api

DATABASE_URL="jdbc:mysql://localhost:3306/careloom?useSSL=false&allowPublicKeyRetrieval=true" \
JWT_SECRET_KEY="dev-secret-change-me" \
GOOGLE_MAP_API_KEY="optional" \
./gradlew bootRun --args='--spring.profiles.active=dev,local'
```

The API starts on port 8080 by default.

## Spring Profiles

| Profile | File | Purpose |
|---------|------|---------|
| `dev` | `application-dev.yml` | Debug logging for the application package |
| `local` | `application-local.yml` | Local overrides (not committed to git, copy from `application-local.sample.yml`) |
| `docker` | `application-docker.yml` | Used inside Docker containers |
| `stg` | `application-stg.yml` | Staging environment |
| `prod` | `application-prod.yml` | Production environment |

## Package Structure

```
edu.stanford.fsi.reap/
├── config/         # @Configuration classes
├── converter/      # JPA AttributeConverters (e.g., JSON column to Java object)
├── dto/            # Request/response DTOs
├── entity/         # JPA @Entity classes
│   └── enumerations/  # Enum types (BabyStage, VisitStatus, etc.)
├── handler/        # @ExceptionHandler classes
├── jwt/            # JWT token provider, filter
├── logging/        # Request/response logging filter
├── pojo/           # Plain objects (non-entity, non-DTO)
├── repository/     # Spring Data JPA repositories
├── security/       # Security config, UserDetailsService
├── service/        # Business logic (@Service classes)
├── task/           # @Scheduled tasks
├── utils/          # Utility classes
└── web/rest/       # REST controllers
    ├── admin/      # Admin endpoints (/api/admin/*)
    └── errors/     # Error response types
```

## Adding a New Endpoint

1. **Create or update a DTO** in `dto/` for the request/response shape.

2. **Add business logic** in a `@Service` class in `service/`.

3. **Create the controller method** in the appropriate controller:
   - Public/CHW endpoints: `web/rest/`
   - Admin-only endpoints: `web/rest/admin/`

4. Example:

```java
// In web/rest/admin/ExampleResource.java
@RestController
@RequestMapping("/api/admin/examples")
public class ExampleResource {

    private final ExampleService exampleService;

    public ExampleResource(ExampleService exampleService) {
        this.exampleService = exampleService;
    }

    @GetMapping
    public List<ExampleDTO> getAll() {
        return exampleService.findAll();
    }

    @PostMapping
    public ExampleDTO create(@RequestBody @Valid CreateExampleDTO dto) {
        return exampleService.create(dto);
    }
}
```

### Endpoint conventions

- Base path: `/api/`
- Admin endpoints: `/api/admin/`
- Authentication: `POST /api/authenticate`
- Health check: `GET /actuator/health`
- Use `@Valid` for request body validation
- Return DTOs, not entities
- Spring Data `Pageable` for paginated endpoints (query params: `page`, `size`, `sort`)

## Adding a New Entity

1. Create the entity class in `entity/`:

```java
@Entity
@Data
public class Example extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "boolean default false")
    private Boolean deleted = false;
}
```

2. Create a repository in `repository/`:

```java
public interface ExampleRepository extends JpaRepository<Example, Long> {
    List<Example> findByDeletedFalse();
}
```

3. Create a Flyway migration (see next section).

### Entity conventions

- Extend `AbstractAuditingEntity` for automatic `createdDate`, `lastModifiedDate`, `createdBy`, `lastModifiedBy` fields.
- Use `deleted` boolean for soft deletes (do not hard-delete records).
- Use Lombok `@Data` for getters/setters.
- History tables (e.g., `BabyHistory`) track changes for auditing.

## Flyway Migrations

Database schema changes are managed by Flyway. Migration files live in:

```
services/api/src/main/resources/db/migration/
```

### Creating a new migration

Name the file using the timestamp pattern:

```
V{YYYYMMDDHHmmss}__{description}.sql
```

Example:

```
V20260327120000__add_example_table.sql
```

```sql
CREATE TABLE example (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    last_modified_by VARCHAR(50)
);
```

### Migration rules

- **Never modify an existing migration file.** Flyway tracks which migrations have been applied by checksum. Changing a file will cause startup failures.
- **Always add a new migration** for schema changes, even small ones.
- **Use descriptive names** that explain what the migration does.
- **Test locally** by running the API against a fresh database before submitting a PR.

## Key Dependencies

| Dependency | Purpose |
|------------|---------|
| Spring Boot 2.2.6 | Web framework, DI, auto-configuration |
| Spring Data JPA | Database access via repositories |
| Spring Security | Authentication and authorization |
| Flyway 6.4.2 | Database migration management |
| jjwt 0.9.1 | JWT token creation and validation |
| Springfox Swagger 2.9.2 | API documentation (Swagger UI) |
| ModelMapper 2.3.8 | Entity-to-DTO mapping |
| Lombok | Boilerplate reduction (@Data, @Builder, etc.) |
| AWS S3 SDK | Media file storage |
| Apache POI | Excel export generation |
| Google Guava | Utility library |

## Code Style

The project uses [Spotless](https://github.com/diffplug/spotless) with Google Java Format:

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

Spotless runs automatically via the `lefthook.yml` pre-commit hook.
