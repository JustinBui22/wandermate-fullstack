# Dependency and Security Scanning

## Workflows

### Frontend production dependencies

```bash
npm audit --omit=dev --audit-level=high
```

High and critical production advisories fail the security workflow. The workflow never runs `npm audit fix --force`.

### Backend dependencies

The Maven `security-scan` profile runs OWASP Dependency-Check and produces HTML, JSON and SARIF reports.

```bash
./mvnw -Psecurity-scan dependency-check:check
```

The current policy fails at CVSS 9.0 or higher and excludes test-scope dependencies from blocking.

An optional `NVD_API_KEY` GitHub secret can improve vulnerability-data download reliability.

### Secret scanning

The security workflow downloads a pinned Gitleaks release, verifies its checksum and scans complete Git history with redaction enabled.

The repository currently uses Gitleaks default rules. There is no committed `.gitleaks.toml`; documentation or workflows must not claim custom allowlists unless that file is deliberately added and reviewed.

GitHub secret scanning and push protection should also be enabled in repository settings.

### CodeQL

CodeQL analyzes:

- Java with a manual Maven build;
- JavaScript/TypeScript with no-build analysis.

The workflow uses the `security-extended` query suite and uploads findings to the GitHub Security tab.

### Dependabot

`.github/dependabot.yml` opens weekly reviewable update PRs for:

- frontend npm;
- backend Maven;
- GitHub Actions;
- Docker images.

Dependabot does not auto-merge or auto-deploy updates.

## Response process

1. Confirm the finding applies to a shipped/runtime dependency or reachable code path.
2. Review the advisory and fixed versions.
3. Prefer the smallest compatible upgrade.
4. Run backend/frontend tests and CI.
5. Avoid forced major upgrades to the Expo/React Native dependency graph.
6. Document accepted risk when no safe fix exists.

## Secret incident response

If a real secret appears in source, logs, screenshots or Git history:

1. revoke/rotate it immediately;
2. remove it from the current files;
3. purge it from history when appropriate;
4. review access/deployment logs;
5. replace public evidence with redacted demo data.

Removing a file from the latest commit does not invalidate an exposed secret.
