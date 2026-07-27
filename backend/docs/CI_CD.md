# CI/CD Verification

## Workflow overview

WanderMate uses four GitHub Actions workflows:

```text
.github/workflows/backend-ci-cd.yml
.github/workflows/frontend-ci.yml
.github/workflows/security-scanning.yml
.github/workflows/codeql.yml
```

The workflows run for matching pull requests and pushes to `main`, and they can also be started manually with `workflow_dispatch`.

## Backend workflow

The backend workflow performs these jobs in order:

1. `./mvnw -B clean verify`
2. JaCoCo report generation during Maven `verify`
3. backend JAR and verification-report artifact upload
4. startup of the packaged JAR against an empty MariaDB 11.4 service database
5. automatic Flyway V1-V6 migration verification
6. Hibernate schema validation and health-endpoint verification
7. tracked Render deployment for successful pushes to `main`
8. production health verification after Render reports the new deploy as `live`

The fresh-database job uses temporary CI-only credentials. It does not connect to the production database.

Backend artifacts:

```text
backend-verification-reports
backend-application
fresh-database-startup-log   # uploaded only when the fresh-database job fails
```

`backend-verification-reports` contains Surefire output and the JaCoCo HTML/XML/CSV report under `target/site/jacoco`.

No coverage threshold is enforced yet. The report is evidence and a baseline for later quality decisions.

## Frontend workflow

The frontend workflow performs:

```bash
npm ci
npm run typecheck
npm test
npm run test:components -- --coverage --coverageDirectory=coverage/components
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```

`expo config --type public --json` validates the resolved public Expo configuration. The static web export validates Metro bundling, Expo Router resolution and referenced assets.

Frontend artifacts:

```text
frontend-verification-reports
frontend-web-export
```

The coverage artifact currently covers the Jest component-test suite. Vitest still runs as part of `npm test`, but it does not yet have a coverage provider dependency.

## Security workflows

`security-scanning.yml` runs three independent jobs:

- `npm audit --omit=dev --audit-level=high` for production frontend dependencies;
- OWASP Dependency-Check for backend dependencies, failing at CVSS 9.0 or higher;
- a checksum-verified Gitleaks scan across the complete Git history.

`codeql.yml` analyzes Java and JavaScript/TypeScript with the `security-extended` query suite. CodeQL results are published under the repository Security tab.

Dependabot is configured separately in `.github/dependabot.yml` for npm, Maven, GitHub Actions and Docker updates. It opens reviewable pull requests and does not auto-merge them.

See [Dependency and security scanning](SECURITY_SCANNING.md) for thresholds, reports, local commands and remediation rules.

## Required GitHub configuration

Create these under:

```text
Repository Settings
→ Secrets and variables
→ Actions
```

### Repository secrets

```text
RENDER_DEPLOY_HOOK_URL
RENDER_API_KEY
NVD_API_KEY                  # optional, improves OWASP Dependency-Check feed performance
```

`RENDER_DEPLOY_HOOK_URL` is the backend service deploy hook.

`RENDER_API_KEY` is used only to retrieve the status of the specific deploy started by the workflow. It prevents CI from reporting success merely because Render accepted the hook request.

`NVD_API_KEY` is optional. OWASP Dependency-Check can run without it, but an API key improves National Vulnerability Database update speed and reliability.

### Repository variables

```text
RENDER_SERVICE_ID
RENDER_HEALTH_URL
```

Example health URL:

```text
https://wandermate-fullstack.onrender.com/Wandermate/api/v1/health
```

The service ID has the Render form:

```text
srv-...
```

## Render configuration

In the Render backend service settings:

1. set the HTTP health-check path to:

   ```text
   /Wandermate/api/v1/health
   ```

2. keep the required backend environment variables configured;
3. activate the `prod` Spring profile;
4. avoid a separate `On Commit` auto-deploy when this GitHub workflow owns deployment, otherwise the same commit can deploy twice.

The workflow deploys the exact GitHub commit SHA, retrieves that deploy by ID, fails on Render build/start failure statuses, and then checks the public health endpoint.

A `202 Accepted` deploy-hook response is treated as a CI failure because Render does not return a deploy ID for a queued deployment. This prevents an untracked queued deployment from being reported as successful.

## Local equivalents

Backend:

```bash
cd backend
./mvnw clean verify
```

Frontend:

```bash
cd frontend
npm ci
npm run typecheck
npm test
npx expo config --type public --json
npx expo export --platform web --output-dir dist
```

## Failure investigation

Backend verification failure:

- download `backend-verification-reports`;
- review Surefire test reports;
- review `target/site/jacoco/index.html` for coverage evidence.

Fresh-database startup failure:

- download `fresh-database-startup-log`;
- inspect Flyway validation, schema validation and application startup errors.

Render failure:

- open the tracked deploy in the Render dashboard;
- inspect build and runtime logs;
- confirm the Render API key and service ID refer to the same service;
- confirm the health-check path and production health URL are correct.

Frontend failure:

- review the TypeScript/test output;
- download `frontend-verification-reports`;
- run the failing Expo config or export command locally.

Security-scan failure:

- download the corresponding npm, OWASP or Gitleaks artifact;
- verify whether the finding affects shipped code;
- update dependencies without forced major upgrades;
- rotate any real secret before removing it from source control;
- use suppressions only for documented, reviewed false positives.
