# Docker Setup

This guide explains how to run the WanderMate backend and MariaDB locally with Docker Compose.

## Requirements

```text
Docker Desktop
Java 21 only if running backend outside Docker
Maven wrapper included in backend project
```

## Files

Important backend Docker files:

```text
backend/Dockerfile
backend/docker-compose.yml
backend/docker/init/init.sql
backend/.env.example
```

Do not commit:

```text
backend/.env
backend/docker/init/full-init.sql
raw database dumps
```

## Environment Variables

Create local env from example:

```bash
cd backend
cp .env.example .env
```

Minimum required variables:

```env
DB_URL=jdbc:mariadb://db:3306/TravellingApp
DB_USERNAME=app_user
DB_PASSWORD=app_password
MARIADB_DATABASE=TravellingApp
MARIADB_USER=app_user
MARIADB_PASSWORD=app_password
MARIADB_ROOT_PASSWORD=root_password
SPRING_PROFILES_ACTIVE=dev
```

Cloudinary variables if testing uploads through Docker:

```env
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
CLOUDINARY_BASE_FOLDER=wandermate
```

## Run Docker

From backend folder:

```bash
docker compose up --build
```

Clean reset:

```bash
docker compose down -v
docker compose up --build
```

## Local URLs

Backend via Docker:

```text
http://localhost:8082/The-Project
```

Health:

```text
http://localhost:8082/The-Project/api/v1/health
```

Swagger UI in dev/local:

```text
http://localhost:8082/The-Project/swagger-ui/index.html
```

## Expected Startup

Successful startup should show:

```text
MariaDB container running
Backend container running
No SQL init duplicate/constraint errors
Health endpoint returns success
```

## Common Issues

### SQL duplicate table/constraint error

Cause:

```text
Both init.sql and old full-init.sql are inside backend/docker/init.
```

Fix:

```text
Keep only backend/docker/init/init.sql.
Delete backend/docker/init/full-init.sql.
```

Then run:

```bash
docker compose down -v
docker compose up --build
```

### Cloudinary upload fails in Docker

Check `docker-compose.yml` passes:

```yaml
CLOUDINARY_CLOUD_NAME: ${CLOUDINARY_CLOUD_NAME}
CLOUDINARY_API_KEY: ${CLOUDINARY_API_KEY}
CLOUDINARY_API_SECRET: ${CLOUDINARY_API_SECRET}
CLOUDINARY_BASE_FOLDER: ${CLOUDINARY_BASE_FOLDER}
```

### Backend cannot connect to database

Inside Docker, database host should be the service name, usually:

```text
db
```

Example:

```env
DB_URL=jdbc:mariadb://db:3306/TravellingApp
```

Outside Docker, database host is normally:

```text
localhost
```

Example:

```env
DB_URL=jdbc:mariadb://localhost:3306/TravellingApp
```

### Swagger not visible in production

This is expected. Production profile disables Swagger:

```properties
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false
```

Use Swagger only locally.
