# Database Seed Guide

This document explains the purpose of the SQL seed files used by the local Docker MariaDB setup.

---

## Seed Location

Docker Compose mounts this folder into the MariaDB container:

```text
backend/docker/init
```

MariaDB automatically runs SQL files in:

```text
/docker-entrypoint-initdb.d
```

only when the database volume is first created.

---

## Main Safe Seed File

The safe local demo seed should be:

```text
backend/docker/init/init.sql
```

This file should contain safe schema/configuration/demo data only.

It can include:

- Table structure
- Error-code rows
- Public/non-sensitive configuration keys
- Email/SMS template rows with placeholder values
- Search/suggest seed data for cities/restaurants/accommodations

It should not include:

- Real passwords
- Real users
- OTP records
- Session tokens
- Refresh tokens
- Real OAuth tokens
- Real email credentials
- Personal/private data

---

## Private Dump File

The project currently may contain or locally generate files such as:

```text
backend/docker/init/full-init.sql
```

This type of file should be treated as private/local if it came from a real or cloud database dump. It should not be committed if it contains real data or environment-specific details.

Recommended approach:

```text
init.sql       → committed safe demo seed
full-init.sql  → ignored/private local dump
```

---

## Why Seed Data Matters

The backend uses database-backed configuration for several behaviours, including:

- Public/non-authenticated URLs
- Token expiration times
- OTP retry limits
- OTP restriction duration
- OTP/email/SMS template data
- Error code lookups
- Search/suggest data

Without seed configuration, some flows may fail at runtime because the app expects configuration rows to exist.

---

## Docker Import Behaviour

MariaDB imports seed files only when the DB volume is empty.

If `init.sql` changes but your container already has an existing volume, the changes will not re-import automatically.

Reset the DB volume:

```bash
docker compose down -v
docker compose up --build
```

Warning: `down -v` deletes local Docker database data.

---

## Safe Seed Rules for GitHub

Before committing SQL seed files, check:

```text
No real usernames/emails/phone numbers
No real hashed passwords
No OTP values
No refresh/session tokens
No OAuth access/refresh tokens
No cloud DB hostnames if they reveal private infrastructure
No private API keys or secrets
```

For public GitHub, use placeholder values such as:

```text
demo@example.com
replace_me
local/demo
```

---

## Recommended Future Improvement

For a more production-style setup later, consider replacing large SQL dumps with:

```text
Flyway or Liquibase migrations
```

That would separate:

```text
Schema migration
Seed reference data
Private runtime secrets
```
