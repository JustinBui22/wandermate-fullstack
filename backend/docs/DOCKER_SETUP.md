# Docker Setup

## Services and ports

| Service | Container | Default host mapping |
|---|---|---|
| MariaDB 11 | `traveling-app-db` | `3307:3306` |
| Spring Boot | `traveling-app-backend` | `8082:8080` |

The backend container connects to MariaDB using `db:3306`, the Compose service hostname and container port.

## Prepare environment

```bash
cd backend
cp .env.example .env
```

Replace placeholders. Required Docker values include:

```text
DB_NAME
DB_HOST_PORT
DB_USERNAME
DB_PASSWORD
DB_ROOT_PASSWORD
BACKEND_HOST_PORT
DB_URL=jdbc:mariadb://db:3306/traveling_app
JWT_SECRET
REFRESH_TOKEN_HASH_SECRET
```

Secret requirements:

```text
JWT_SECRET >= 64 UTF-8 bytes
REFRESH_TOKEN_HASH_SECRET >= 32 UTF-8 bytes
```

Optional Cloudinary/email values control those integrations.

## Why `.env` works here

Docker Compose automatically reads `.env` from the directory containing `docker-compose.yml`, substitutes `${NAME}` expressions, and passes configured values into containers.

The same file is not automatically loaded when Spring Boot is launched directly from IntelliJ or Maven.

## Start

```bash
docker compose up --build
```

Useful URLs:

```text
Health:  http://localhost:8082/Wandermate/api/v1/health
Swagger: http://localhost:8082/Wandermate/swagger-ui/index.html
```

The Swagger URL works unless `SPRING_PROFILES_ACTIVE=prod` is passed.

## Common commands

```bash
# Start existing images/containers
docker compose up

# Rebuild after backend/pom/Dockerfile changes
docker compose up --build

# Background mode
docker compose up -d --build

# Logs
docker compose logs -f backend
docker compose logs -f db

# Stop/remove containers and network, keep DB volume
docker compose down

# Stop/remove and delete DB volume
docker compose down -v

# Inspect status
docker compose ps
```

## Seed behavior

`docker/init/init.sql` is mounted to `/docker-entrypoint-initdb.d`. MariaDB executes it only for a new data directory. Reset the named volume to rerun it.

## Run frontend against Docker

Android emulator:

```text
EXPO_PUBLIC_APP_ENV=local-docker
EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8082/Wandermate
```

Physical device:

```text
EXPO_PUBLIC_API_BASE_URL=http://<computer-lan-ip>:8082/Wandermate
```

Ensure the firewall permits the port and both devices are on the same network.

## Troubleshooting

### Backend cannot connect to DB

Inside the backend container, use:

```text
jdbc:mariadb://db:3306/traveling_app
```

Do not use `localhost:3307` inside the container.

### Direct IntelliJ backend cannot connect

From the host, use:

```text
jdbc:mariadb://localhost:3307/traveling_app
```

### Secret placeholder/startup failure

`replace_me` is not a valid token secret. Configure real values with the minimum byte lengths.

### Seed edits do not appear

The volume already exists. Reset it only if losing local data is acceptable:

```bash
docker compose down -v
docker compose up --build
```
