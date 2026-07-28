# Project File Audit

Use this checklist before pushing, creating a source archive or sharing the project.

## Public source files

- Backend and frontend source code.
- Flyway migrations under `backend/src/main/resources/db/migration`.
- Root/backend/frontend documentation.
- `.env.example` templates with placeholders only.
- Reviewed screenshots that contain only demo data.

`backend/docker/init/init.sql` is legacy reference material, not the active schema source. It may remain in source only if clearly labelled and free of private data.

## Private or generated files

- Real `.env` files and environment exports.
- Tokens, OTPs, passwords, OAuth credentials and provider secrets.
- Database dumps/backups.
- `node_modules`, Maven `target`, Expo caches, `dist`, coverage and logs.
- IDE metadata when producing an external source archive.
- Native signing keys, certificates and provisioning profiles.

`.gitignore` reduces risk but does not invalidate a secret that was previously committed. Rotate exposed credentials and purge history where appropriate.

## Pre-push checks

```bash
git status --short
git diff --check
git ls-files | rg '(^|/)\.env$|(^|/)node_modules/|(^|/)target/|(^|/)dist/|(^|/)coverage/|\.sql\.dump$'
git grep -n -I -E 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|api[_-]?secret|client[_-]?secret|refresh[_-]?token|password[=:]'
```

Review every match; variable names and placeholders can be legitimate, but real values cannot.

Verify representative ignore rules:

```bash
git check-ignore -v backend/.env frontend/.env frontend/dist backend/target
```

## Clean source archive

Do not zip the working directory. Use committed files only:

```bash
git archive --format=zip --output=wandermate-source.zip HEAD
```

## Documentation checks

- Confirm all relative Markdown links exist with exact case.
- Confirm docs describe Flyway and `ddl-auto=validate`.
- Confirm auth docs distinguish operational email OTP from the demo-only simulated phone-OTP path.
- Confirm health links use `/Wandermate`.
- Confirm screenshots match the current UI and contain no private values.
