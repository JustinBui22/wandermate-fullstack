# WanderMate Backend

Spring Boot API for WanderMate authentication, trip planning, collaboration, profile management and image upload.

## Stack

- Java 21
- Spring Boot 3.5.4
- Spring Web and Spring Security
- Spring Data JPA / Hibernate
- MariaDB runtime database
- H2 in MariaDB compatibility mode for selected tests
- Flyway database migrations
- JJWT 0.11.5
- Cloudinary Java SDK
- Spring Mail with Gmail OAuth2 support
- Springdoc OpenAPI
- Maven Wrapper, Docker and Docker Compose
- JUnit 5, Mockito, Spring Boot integration tests and JaCoCo

## Structure

```text
backend/
├── docs/
├── src/main/java/com/example/travellingapp/
│   ├── config/
│   ├── controller/ and controller/impl/
│   ├── dto/
│   ├── entity/
│   ├── exception_handler/
│   ├── mapper/
│   ├── repository/
│   ├── security/
│   ├── service/ and service/impl/
│   └── validator/
├── src/main/resources/db/migration/    Flyway V1–V6
├── src/test/
├── docker/init/init.sql                Legacy reference only; not mounted by Compose
├── .env.example
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Base paths

```text
Context path: /Wandermate
Direct local: http://localhost:8080/Wandermate
Docker host: http://localhost:8082/Wandermate
Render: https://wandermate-fullstack.onrender.com/Wandermate
```

Health:

```text
/Wandermate/api/v1/health
```

Local Swagger:

```text
http://localhost:8080/Wandermate/swagger-ui/index.html
```

Swagger is disabled with the `prod` profile.

## Required environment variables

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
OTP_HASH_SECRET
```

Secret requirements:

- `JWT_SECRET`: at least 64 UTF-8 bytes.
- `REFRESH_TOKEN_HASH_SECRET`: at least 32 UTF-8 bytes.
- `OTP_HASH_SECRET`: at least 32 UTF-8 bytes and distinct from the token secrets.

Optional variables configure Cloudinary, email OAuth, CORS, rate limits, timezone, Flyway baselining and the server port. See `.env.example`.

Spring Boot does not load `backend/.env` by itself. Docker Compose loads it automatically; IntelliJ and direct Maven runs require environment variables to be injected separately.

## Run with Docker Compose

```bash
cp .env.example .env
# Replace placeholders
docker compose up --build
```

Default mappings:

- MariaDB: host `3307` → container `3306`
- Backend: host `8082` → container `8080`

Stop services:

```bash
docker compose down
```

Delete all local database data and rebuild from Flyway:

```bash
docker compose down -v
docker compose up --build
```

## Run directly

Start MariaDB, set the required environment variables and run:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

When using the Docker database from the host:

```text
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
```

## Authentication

Protected requests:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

Refresh requests:

```http
Refresh-Token: <refresh-token>
Session-Token: <session-token>
```

Registration and password reset support email OTP and a phone-OTP demo path. Email OTP is delivered through the configured mail provider. `SmsServiceImpl` currently returns a simulated success without calling an SMS gateway because a paid provider is not configured; it must be replaced before phone OTP is treated as production-ready.

## Modules

| Module | Current behavior |
|---|---|
| Users | Register, detail verification, login, password reset, logout, profile/settings, protected generic user lookup |
| OTP | Email delivery, demo-only simulated SMS path, cooldown, send/verify limits, purpose-bound HMAC storage, expiry and consume-on-success |
| Tokens | HS512 access JWT, hashed refresh/session tokens, rotation, revocation, locking and reuse detection |
| Trips | CRUD, accessible listing, status and overlap checks |
| Destinations | CRUD with calendar-date range validation |
| Activities | CRUD with local date-time and overlap validation |
| Collaboration | Invitations, requests, members, roles, share codes, warnings and summary |
| Uploads | Authenticated Cloudinary uploads for profiles and trip covers |

## Permission model

| Action | OWNER | EDITOR | VIEWER |
|---|---:|---:|---:|
| View accessible plan | Yes | Yes | Yes |
| Edit trip/destination/activity content | Yes | Yes | No |
| Delete trip | Yes | No | No |
| Invite/manage requests/share codes | Yes | No | No |
| Change roles/remove members | Yes | No | No |

The service layer is authoritative. Frontend visibility is a usability layer only.

## Database lifecycle

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.jdbc.time_zone=UTC
```

- Flyway owns all schema changes.
- `flyway_schema_history` records applied versions.
- Hibernate validates and never repairs the schema.
- Existing migration files must not be edited after deployment.
- New changes require a new migration version.
- `docker/init/init.sql` is legacy V1-era reference material and is not used by the active Compose file.

## Testing

```bash
./mvnw clean verify
```

Current included Surefire evidence:

```text
487 tests, 0 failures, 0 errors, 0 skipped
```

Coverage includes controllers, services, validators, security filters, exception handling, token/OTP hashing, transactions, refresh-token concurrency and share-code concurrency.

## Production profile

`application-prod.properties`:

- disables SQL output and bind logging;
- keeps application/security logging at `INFO`;
- disables request-detail and HTTP wire/header logging;
- disables Swagger/OpenAPI;
- keeps Flyway enabled;
- keeps Hibernate in `validate` mode.

Activate with:

```text
SPRING_PROFILES_ACTIVE=prod
```

## Documentation

- [Documentation index](docs/README.md)
- [API guide](docs/API_GUIDE.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Authentication flow](docs/AUTH_FLOW.md)
- [Date and time model](docs/DATE_TIME_MODEL.md)
- [Docker setup](docs/DOCKER_SETUP.md)
- [Frontend integration](docs/FRONTEND_INTEGRATION.md)
- [Operations](docs/OPERATIONS.md)
- [Postman guide](docs/POSTMAN_GUIDE.md)
- [Production API documentation](docs/PRODUCTION_API_DOCS.md)
- [Database backup and recovery](docs/DATABASE_BACKUP_AND_RECOVERY.md)
- [CI/CD](docs/CI_CD.md)
- [Security scanning](docs/SECURITY_SCANNING.md)
- [Roadmap and maintenance](docs/ROADMAP.md)
