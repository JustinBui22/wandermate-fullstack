# Database Seed

This document explains how database seed files are used in the Docker demo environment.

---

## Seed File Location

The Docker demo database imports SQL files from:

```text
docker/init/
```

The main safe seed file is:

```text
docker/init/init.sql
```

This folder is mounted into the MariaDB container:

```yaml
volumes:
  - traveling-db-data:/var/lib/mysql
  - ./docker/init:/docker-entrypoint-initdb.d
```

---

## When `init.sql` Runs

MariaDB only runs `init.sql` the first time the database volume is created.

This runs the seed:

```bash
docker compose down -v
docker compose up --build
```

This does not re-run the seed:

```bash
docker compose down
docker compose up
```

because the existing Docker volume already contains database data.

---

## What the Safe Seed Should Include

The public GitHub seed can include:

```text
✅ Table structures
✅ Error code rows
✅ Non-secret configuration rows
✅ Public endpoint config
✅ OTP/email template content
✅ Demo-safe values
```

---

## What the Safe Seed Must Not Include

The public GitHub seed must not include:

```text
❌ Real users
❌ Password hashes from real users
❌ OTP records
❌ Refresh token records
❌ Session token records
❌ Real Gmail OAuth access tokens
❌ Real Gmail OAuth refresh tokens
❌ Real client secrets
❌ Real API keys
❌ Private phone numbers/emails
```

---

## Safe Email/OAuth Values

For public demo seed, OAuth values should be placeholders:

```text
EMAIL_OAUTH_REFRESH_ENABLED = false
EMAIL_CLIENT_ID = replace_me
EMAIL_CLIENT_SECRET = replace_me
EMAIL_REFRESH_TOKEN = replace_me
EMAIL_ACCESS_TOKEN_CONFIG = replace_me
EMAIL_ADDRESS_CONFIG = demo@example.com
```

For private local testing, real values should come from `.env`, not from the public SQL seed.

---

## Why This Matters

The project uses database-backed configuration, so the app needs some configuration rows to start and run properly.

At the same time, GitHub must not expose real secrets.

The clean solution is:

```text
Public GitHub init.sql
→ safe config and demo data only

Private .env / cloud secrets
→ real passwords, OAuth secrets, tokens, DB credentials
```

---

## Production Note

In production, the backend would normally connect to a real cloud database.

The local Docker `init.sql` is mainly for:

- local demo
- portfolio setup
- running the project on another machine
- testing without using the real cloud database
