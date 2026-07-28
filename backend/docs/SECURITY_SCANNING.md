# Dependency and Security Scanning

## Purpose

WanderMate separates security checks by the type of risk they detect:

| Check | Scope | Failure policy |
|---|---|---|
| npm audit | Frontend production dependencies only | Fails for critical advisories |
| OWASP Dependency-Check | Backend runtime/compile dependencies | Fails for CVSS 9.0 or higher |
| Gitleaks | Complete Git history | Fails when a non-allowlisted secret is detected |
| CodeQL | Java and JavaScript/TypeScript source | Publishes code-scanning alerts in GitHub |
| Dependabot | npm, Maven, GitHub Actions and Docker manifests | Opens reviewable pull requests; never auto-merges |

These controls report or block risky changes. They do not automatically force dependency upgrades.

## Workflow files

```text
.github/workflows/security-scanning.yml
.github/workflows/codeql.yml
.github/dependabot.yml
.gitleaks.toml
```

`security-scanning.yml` runs on pull requests and pushes to `main`, can be started manually, and also runs weekly. `codeql.yml` analyzes Java and JavaScript/TypeScript on the same branch events and on a weekly schedule.

## Frontend production audit

Local command:

```bash
cd frontend
npm run audit:prod
```

The script runs:

```bash
npm audit --omit=dev --audit-level=critical
```

`--omit=dev` keeps the blocking policy focused on dependencies shipped with the application. Development dependencies remain visible through Dependabot and CodeQL, but they do not make this production audit fail.

The workflow uploads `npm-audit-production.json`, including when the audit fails.

Do not use:

```bash
npm audit fix --force
```

A forced fix can install incompatible major versions, particularly in an Expo/React Native project. Review the advisory, update the direct dependency or Expo SDK coherently, run `npm ci`, and repeat all frontend verification.

## Backend dependency audit

Local command without an NVD API key:

```bash
cd backend
./mvnw -Psecurity-scan dependency-check:check
```

Optional faster NVD access:

```bash
./mvnw -Psecurity-scan dependency-check:check -DnvdApiKey=<nvd-api-key>
```

The `security-scan` Maven profile uses OWASP Dependency-Check 12.2.2 and generates HTML, JSON and SARIF reports under `backend/target`. Test-scope dependencies are excluded from the blocking policy, and the build fails at CVSS 9.0 or higher.

Add an `NVD_API_KEY` GitHub Actions secret if available. The scanner can run without it, but NVD updates can be slower and more likely to encounter public API rate limits.

A suppression must identify a reviewed false positive. Do not suppress an advisory merely to make CI green.

## Secret scanning

The workflow downloads the pinned Gitleaks 8.30.1 Linux binary and verifies its SHA-256 checksum before execution. It then scans the complete Git history with redacted output.

`.gitleaks.toml` extends the default rules. Its allowlists are deliberately narrow and require both:

- an approved example/test/CI path; and
- a known placeholder or test-only value.

A real credential must never be added to the allowlist. If a real secret is detected:

1. revoke or rotate it immediately;
2. remove it from the current files;
3. assess whether Git history must be rewritten;
4. review deployment and access logs;
5. rerun the scan.

Also enable GitHub repository secret scanning and push protection:

```text
Repository Settings
→ Security and analysis / Advanced Security
→ Secret scanning or Secret Protection
→ Enable push protection
```

The CI scanner provides a repository-controlled check. GitHub push protection adds prevention before supported secrets enter the repository.

## CodeQL

CodeQL analyzes:

```text
java-kotlin
javascript-typescript
```

The Java job performs a manual Maven package build after CodeQL initialization. JavaScript/TypeScript uses the no-build analysis mode. Both use the `security-extended` query suite.

Results appear under the repository Security tab. Review findings for exploitability and data flow rather than dismissing them based only on severity.

## Dependabot

Dependabot checks weekly:

- frontend npm dependencies;
- backend Maven dependencies;
- GitHub Actions versions;
- backend Docker images.

Expo and React Native packages are grouped to reduce incompatible one-package-at-a-time updates. Dependabot only opens pull requests. A dependency update is merged only after the normal backend/frontend CI and security workflows pass.

## Review process

For each advisory or update:

1. identify whether the dependency is direct or transitive;
2. confirm whether the vulnerable code path is used;
3. prefer the smallest compatible update;
4. read framework compatibility notes;
5. run the complete relevant test/build suite;
6. record any temporary suppression with a reason and review date.
