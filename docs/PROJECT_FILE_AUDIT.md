# Project File Audit

Use this checklist before pushing the repository, creating a source archive or sharing the project.

## Files intended for public sharing

- Backend and frontend source code.
- Root, backend, frontend and feature documentation.
- `.env.example` files containing variable names and non-secret placeholders only.
- The sanitized Docker seed at `backend/docker/init/init.sql`.
- Screenshots that have passed the privacy review in
  [SCREENSHOT_CHECKLIST.md](SCREENSHOT_CHECKLIST.md).

## Files that must remain private or generated locally

- Real `.env` files and production environment exports.
- Access tokens, refresh tokens, session tokens, OTP values and OAuth credentials.
- Database passwords, Cloudinary secrets and email-provider credentials.
- `backend/docker/init/full-init.sql`, raw database dumps and backups.
- `node_modules`, Maven `target`, Expo caches, logs and IDE metadata.
- Native signing keys, certificates and provisioning profiles.

The root `.gitignore` contains rules for these categories. Ignore rules reduce risk but do not
remove a secret that was committed previously; rotate exposed credentials and purge them from
Git history when necessary.

## Pre-push verification

Run these commands from the repository root:

```bash
git status --short
git diff --check
git ls-files | rg '(^|/)\.env$|(^|/)node_modules/|(^|/)target/|full-init\.sql$'
git grep -n -I -E 'BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY|api[_-]?secret|client[_-]?secret|refresh[_-]?token|password[=:]'
```

Review every match from the final command; documentation and placeholder variable names can be
legitimate, but real values must not be committed. The `git ls-files` command should produce no
output for the listed private or generated paths.

Verify representative ignore rules:

```bash
git check-ignore -v backend/.env frontend/.env backend/docker/init/full-init.sql
```

## Source archive

Do not zip the working directory, because it may contain ignored secrets and generated files.
Create a clean archive from committed files instead:

```bash
git archive --format=zip --output=wandermate-source.zip HEAD
```

Inspect the archive before sharing it, especially if the repository has ever contained real
credentials or database exports.

## Documentation verification

- Confirm local Markdown targets exist with exact case-sensitive paths.
- Push the documentation changes before checking links on GitHub.
- Open the root README, backend documentation index and frontend README on the target branch.
- Follow every documentation link and inspect every rendered image.
- Verify external endpoints separately; a valid URL can still be temporarily unavailable.
