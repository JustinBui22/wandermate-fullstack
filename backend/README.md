# WanderMate Backend

Spring Boot API for WanderMate's authentication, trip planning, collaboration, profile, and image-upload features.

## Stack

- Java 21
- Spring Boot 3.5.4
- Spring Web and Spring Security
- Spring Data JPA / Hibernate
- MariaDB runtime database
- H2 test database in MariaDB compatibility mode
- JJWT 0.11.5
- Cloudinary Java SDK
- Spring Mail with Gmail OAuth2 support
- Springdoc OpenAPI
- Maven Wrapper, Docker, Docker Compose
- JUnit 5, Mockito, Spring Boot integration tests

## Project structure

```text
backend/
├── docker/init/init.sql
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
│   ├── util/
│   └── validator/
├── src/main/resources/
├── src/test/
├── .env.example
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## Base paths

```text
Context path: /Wandermate
Local direct base URL: http://localhost:8080/Wandermate
Docker host base URL: http://localhost:8082/Wandermate
Configured Render base URL: https://wandermate-fullstack.onrender.com/Wandermate
```

Local Swagger UI:

```text
http://localhost:8080/Wandermate/swagger-ui/index.html
```

Swagger is disabled when the `prod` profile is active.

## Required environment variables

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
```

Requirements enforced by `TokenSecretProvider`:

- `JWT_SECRET`: at least 64 UTF-8 bytes for HS512 signing.
- `REFRESH_TOKEN_HASH_SECRET`: at least 32 UTF-8 bytes for HMAC-SHA256 refresh-token hashing.

Optional feature variables:

```text
CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
CLOUDINARY_BASE_FOLDER
CORS_ALLOWED_ORIGINS
EMAIL_OAUTH_REFRESH_ENABLED
EMAIL_CLIENT_ID
EMAIL_CLIENT_SECRET
EMAIL_REFRESH_TOKEN
EMAIL_TOKEN_URL
EMAIL_ADDRESS_CONFIG
PORT
SPRING_PROFILES_ACTIVE
```

### How `.env` is used

`backend/.env` is automatically read by Docker Compose. Spring Boot does not automatically load that file when the app is launched directly from IntelliJ or Maven.

For direct IntelliJ execution, place the values in **Run → Edit Configurations → Environment variables**, or configure an env-file loader. Render stores/injects the same names from its service environment settings.

## Run with Docker Compose

```bash
cp .env.example .env
# Replace placeholder values in .env
docker compose up --build
```

Services:

- MariaDB container: host port `3307` → container port `3306` by default.
- Backend container: host port `8082` → container port `8080` by default.

Stop containers:

```bash
docker compose down
```

Reset the database volume and rerun the seed:

```bash
docker compose down -v
docker compose up --build
```

## Run directly

Start MariaDB first, then export/set the environment variables. For the Docker database accessed from the host:

```text
DB_URL=jdbc:mariadb://localhost:3307/traveling_app
```

Run:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Authentication headers

Protected endpoints:

```http
Authorization: Bearer <access-token>
Session-Token: <session-token>
```

Refresh endpoint:

```http
Refresh-Token: <refresh-token>
Session-Token: <session-token>
```

## Core modules

| Module | Current behavior |
|---|---|
| Users | Register, pre-validate details, login, forgot password, logout, profile/settings, user lookup |
| OTP | Email/phone send, cooldown, send/verify limits, expiration, block/restriction, consume on success |
| Tokens | HS512 access JWT, hashed refresh tokens, hashed session tokens, rotation/revocation/reuse detection |
| Trips | CRUD, accessible-trip listing, status, overlap checks, search/suggestions |
| Destinations | CRUD under a trip with range/overlap validation |
| Activities | CRUD under a destination with range/overlap validation |
| Collaboration | Invitations, direct/share-code join requests, members, role changes, overlap warnings, summary |
| Uploads | Authenticated Cloudinary multipart upload for profile images and trip covers |

## Permission model

| Action | OWNER | EDITOR | VIEWER |
|---|---:|---:|---:|
| View trip plan | Yes | Yes | Yes |
| Edit trip/destination/activity content | Yes | Yes | No |
| Delete trip | Yes | No | No |
| Invite/manage requests/share codes | Yes | No | No |
| Change roles/remove members | Yes | No | No |

The service layer is authoritative; frontend visibility rules are only a usability layer.

## Database behavior

- `spring.jpa.hibernate.ddl-auto=update` is active in the base configuration.
- `backend/docker/init/init.sql` creates and seeds the initial schema only when MariaDB initializes a fresh volume.
- The seed contains reference/config/error data but no runtime user/trip/token records.
- There is currently no Flyway or Liquibase dependency/migration directory.

## Testing

```bash
./mvnw test
```

The included Surefire reports cover 38 test classes and record:

```text
443 tests, 0 failures, 0 errors, 0 skipped
```

Coverage areas include controllers, services, validators, public-endpoint matching, security filtering, token hashing, and transaction behavior for OTP failures, password reset, refresh-token reuse, share-code attempts, and token revocation.

## Production profile

`application-prod.properties`:

- disables SQL output;
- reduces project/security logging to `INFO`;
- disables Swagger UI and OpenAPI JSON;
- reads Cloudinary and token secrets from environment variables.

Activate with:

```text
SPRING_PROFILES_ACTIVE=prod
```

## Documentation

- [Documentation index](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/README.md)
- [API guide](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/API_GUIDE.md)
- [Architecture](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/ARCHITECTURE.md)
- [Authentication flow](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/AUTH_FLOW.md)
- [Cloudinary image storage](docs/CLOUDINARY_IMAGE_STORAGE.md)
- [Database seed](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/DATABASE_SEED.md)
- [Docker setup](docs/DOCKER_SETUP.md)
- [Frontend integration](docs/FRONTEND_INTEGRATION.md)
- [Operations](docs/OPERATIONS.md)
- [Postman guide](docs/POSTMAN_GUIDE.md)
- [Production API documentation](docs/PRODUCTION_API_DOCS.md)
- [Roadmap](../../Downloads/wandermate-updated-docs(1)/wandermate-updated-docs/backend/docs/ROADMAP.md)
