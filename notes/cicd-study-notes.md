# CI/CD with GitHub Actions — Study Notes

Beginner-to-intermediate notes on **Continuous Integration / Continuous Delivery** centered on **GitHub Actions**: workflows, jobs, runners, secrets, environments, and real deploy patterns.

---

## Table of Contents

1. [What is CI/CD?](#1-what-is-cicd)
2. [Why GitHub Actions?](#2-why-github-actions)
3. [Core building blocks](#3-core-building-blocks)
4. [Workflow file anatomy](#4-workflow-file-anatomy)
5. [Triggers (`on`)](#5-triggers-on)
6. [Jobs, steps, and runners](#6-jobs-steps-and-runners)
7. [Actions (reusable steps)](#7-actions-reusable-steps)
8. [Environment variables, secrets, and OIDC](#8-environment-variables-secrets-and-oidc)
9. [Artifacts, caching, and matrices](#9-artifacts-caching-and-matrices)
10. [Environments & protection rules](#10-environments--protection-rules)
11. [Typical pipeline patterns](#11-typical-pipeline-patterns)
12. [Deploy strategies with Actions](#12-deploy-strategies-with-actions)
13. [Reusable workflows & organization patterns](#13-reusable-workflows--organization-patterns)
14. [Security best practices](#14-security-best-practices)
15. [Debugging & common failures](#15-debugging--common-failures)
16. [Quick cheatsheet](#16-quick-cheatsheet)
17. [Starter examples](#17-starter-examples)

---

## 1. What is CI/CD?

### Continuous Integration (CI)
Every push/PR runs automated **build + test** so broken code is caught early.

### Continuous Delivery
`main` stays releasable; production deploy may need a **manual approval**.

### Continuous Deployment
Every green change on the release branch is **automatically deployed** to production.

```text
Push / PR → GitHub Actions workflow
              → checkout → setup → build → test → (scan)
              → package artifact / image
              → deploy (dev → staging → prod)
```

---

## 2. Why GitHub Actions?

| Benefit | Meaning |
|---|---|
| Same place as code | Workflows live in the repo under `.github/workflows/` |
| Event-driven | Runs on push, PR, tags, schedule, manual click, etc. |
| Marketplace Actions | Reuse `actions/checkout`, `aws-actions/...`, etc. |
| Matrices | Test many OS/language versions in parallel |
| Environments | Approvals, secrets scoped per env (staging/prod) |
| OIDC | Short-lived cloud credentials (no long-lived keys) |

**Mental model:** GitHub is the orchestrator; Actions runners execute your YAML pipeline.

---

## 3. Core building blocks

| Term | Meaning |
|---|---|
| **Workflow** | A YAML file = one automation pipeline |
| **Event** | What starts the workflow (`push`, `pull_request`, …) |
| **Job** | A set of steps that runs on one runner (can depend on other jobs) |
| **Step** | A shell command or an Action |
| **Action** | Reusable packaged step (`uses: owner/name@version`) |
| **Runner** | VM/container that executes the job (GitHub-hosted or self-hosted) |
| **Artifact** | Files uploaded from a job for download / later jobs |
| **Environment** | Named target (e.g. `production`) with protection rules & secrets |

```text
Workflow
  └─ Job A (ubuntu)
       ├─ Step: checkout
       ├─ Step: setup-java
       └─ Step: mvn test
  └─ Job B (needs: A)
       └─ Step: deploy
```

---

## 4. Workflow file anatomy

Location:

```text
.github/workflows/ci.yml
.github/workflows/deploy.yml
```

Minimal shape:

```yaml
name: CI

on:
  pull_request:
  push:
    branches: [main]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"

      - name: Build & test
        run: mvn -B verify
```

**Rules of thumb**
- One concern per workflow file is fine (`ci.yml`, `release.yml`)
- Pin Actions to a major version tag (`@v4`) or full commit SHA (stricter security)
- Prefer `ubuntu-latest` unless you need Windows/macOS

---

## 5. Triggers (`on`)

| Trigger | Use |
|---|---|
| `push` | CI on branch updates; deploy from `main` |
| `pull_request` | Test PR before merge |
| `workflow_dispatch` | Manual “Run workflow” button |
| `schedule` | Cron (nightly builds, dependency audits) |
| `release` | When a GitHub Release is published |
| `workflow_call` | Called by a reusable workflow |
| `repository_dispatch` | External systems trigger via API |

### Filter examples

```yaml
on:
  push:
    branches: [main]
    paths:
      - "src/**"
      - "pom.xml"
      - ".github/workflows/**"
  pull_request:
    branches: [main]
  workflow_dispatch:
  schedule:
    - cron: "0 3 * * *"   # 03:00 UTC daily
```

### `concurrency` (cancel outdated PR runs)

```yaml
concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true
```

Saves minutes when people push quickly to the same PR.

---

## 6. Jobs, steps, and runners

### Jobs
- Run **in parallel** by default
- Use `needs:` for ordering (`deploy` needs `test`)
- Each job gets a **fresh** runner (no leftover files unless you pass artifacts)

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps: [...]

  deploy:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps: [...]
```

### Steps
- `uses:` → run an Action
- `run:` → shell script (`bash` on Linux)
- Steps in a job share the same workspace

### Runners

| Type | Notes |
|---|---|
| **GitHub-hosted** | `ubuntu-latest`, `windows-latest`, `macos-latest` — easy, billed by minutes |
| **Self-hosted** | Your VM/k8s runners — private network access, custom tools, you maintain them |
| **Larger/hosted** | Bigger CPUs for heavy builds (plan-dependent) |

Labels example: `runs-on: ubuntu-latest` or `runs-on: [self-hosted, linux, x64]`

### Job `if` conditions (common)

```yaml
if: github.event_name == 'pull_request'
if: github.ref == 'refs/heads/main'
if: startsWith(github.ref, 'refs/tags/v')
if: failure()          # cleanup job
if: always()           # run even if previous step failed
```

---

## 7. Actions (reusable steps)

Popular official/community Actions:

| Action | Purpose |
|---|---|
| `actions/checkout` | Clone the repo |
| `actions/setup-node` / `setup-java` / `setup-python` / `setup-go` | Install toolchains |
| `actions/cache` | Cache dependencies |
| `actions/upload-artifact` / `download-artifact` | Pass files between jobs |
| `docker/build-push-action` | Build & push images |
| `aws-actions/configure-aws-credentials` | Assume AWS role via OIDC/keys |
| `peaceiris/actions-gh-pages` | Deploy static site |

```yaml
- uses: actions/setup-node@v4
  with:
    node-version: "20"
    cache: npm

- run: npm ci
- run: npm test
```

**Create your own Action** when a step is reused across many repos (JS action, composite action, or Docker action).

---

## 8. Environment variables, secrets, and OIDC

### Env vars

```yaml
env:
  APP_ENV: staging

jobs:
  build:
    runs-on: ubuntu-latest
    env:
      NODE_ENV: test
    steps:
      - run: echo "env=$APP_ENV node=$NODE_ENV"
```

### Secrets
- Store in repo / org / environment secrets (Settings → Secrets and variables → Actions)
- Reference: `${{ secrets.MY_TOKEN }}`
- Masked in logs (still don’t `echo` them carelessly)
- **Never commit secrets** to YAML or source

```yaml
- name: Login to registry
  run: echo "${{ secrets.REGISTRY_TOKEN }}" | docker login -u user --password-stdin
```

### GitHub token
- `GITHUB_TOKEN` is auto-provided for API/package permissions
- Limit permissions with `permissions:` at workflow/job level (least privilege)

```yaml
permissions:
  contents: read
  id-token: write   # needed for OIDC
  packages: write
```

### OIDC to cloud (preferred over long-lived keys)

```text
GitHub Actions job
   → requests OIDC token from GitHub
   → AWS/GCP/Azure trusts GitHub repo
   → temporary credentials (minutes)
```

AWS sketch:

```yaml
permissions:
  id-token: write
  contents: read

steps:
  - uses: aws-actions/configure-aws-credentials@v4
    with:
      role-to-assume: arn:aws:iam::123456789012:role/gha-deploy
      aws-region: ap-south-1
```

**Prefer OIDC** over storing `AWS_ACCESS_KEY_ID` in secrets when possible.

### Variables vs secrets
| | Variables | Secrets |
|---|---|---|
| Purpose | Non-sensitive config | Sensitive tokens/keys |
| Example | `REGION=ap-south-1` | `DB_PASSWORD` |
| Access | `${{ vars.NAME }}` | `${{ secrets.NAME }}` |

---

## 9. Artifacts, caching, and matrices

### Artifacts
Share build outputs across jobs or keep for download.

```yaml
- uses: actions/upload-artifact@v4
  with:
    name: app-jar
    path: target/*.jar

# later job
- uses: actions/download-artifact@v4
  with:
    name: app-jar
```

**Pattern:** build once → upload artifact → deploy jobs reuse the same binary.

### Cache
Speed up `node_modules`, Maven `.m2`, Gradle, Go modules, etc.

```yaml
- uses: actions/setup-java@v4
  with:
    distribution: temurin
    java-version: "21"
    cache: maven
```

Or explicit `actions/cache` with a key based on lockfile hash.

### Matrix builds

```yaml
strategy:
  fail-fast: false
  matrix:
    java: ["17", "21"]
    os: [ubuntu-latest, windows-latest]
runs-on: ${{ matrix.os }}
steps:
  - uses: actions/setup-java@v4
    with:
      java-version: ${{ matrix.java }}
```

Runs combinations in parallel — great for library compatibility.

---

## 10. Environments & protection rules

Define environments: `development`, `staging`, `production`.

```yaml
jobs:
  deploy-prod:
    runs-on: ubuntu-latest
    environment:
      name: production
      url: https://app.example.com
    steps:
      - run: ./deploy.sh
```

**Environment features**
- Required reviewers (manual approval = Continuous Delivery gate)
- Wait timer
- Environment-specific secrets/variables
- Deployment branch restrictions (only `main` can deploy to `production`)

This is the GitHub-native way to add a **prod approval** step without a separate tool.

---

## 11. Typical pipeline patterns

### A) PR CI only
```text
pull_request → lint → unit test → (build)
```
No deploy.

### B) CI + deploy on main
```text
PR → test
main push → test → build artifact → deploy staging
                 → (approval) → deploy production
```

### C) Tag / release deploy
```text
push tag v1.2.3 → build → publish GitHub Release → deploy prod
```

### D) Monorepo path filters
Only run the service workflow when that service’s files change (`paths:` / `dorny/paths-filter`).

### E) Container image pipeline
```text
test → docker build → push GHCR/ECR → deploy to ECS/K8s/Beanstalk
```

---

## 12. Deploy strategies with Actions

GitHub Actions **orchestrates**; your platform does the rollout.

| Strategy | How with Actions |
|---|---|
| **All-at-once** | SSH/API replace service; simplest |
| **Rolling** | Update ASG/ECS/K8s gradually via CLI/action |
| **Blue/Green** | Deploy green env; shift LB / swap Beanstalk/ECS |
| **Canary** | Deploy canary; watch metrics; promote or rollback job |
| **Feature flags** | Deploy code dark; toggle in LaunchDarkly/AppConfig |

### Rollback patterns
- Re-run workflow with previous tag
- Deploy previous image digest
- Traffic shift back (blue/green)
- Environment protection + automatic fail job on smoke-test failure

### Smoke test after deploy

```yaml
- name: Smoke
  run: curl -fsS https://staging.example.com/health
```

Fail the job → don’t promote to prod (`needs:` + `if:`).

---

## 13. Reusable workflows & organization patterns

### Reusable workflow (`workflow_call`)

```yaml
# .github/workflows/reusable-java-ci.yml
on:
  workflow_call:
    inputs:
      java-version:
        type: string
        default: "21"

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ inputs.java-version }}
          distribution: temurin
          cache: maven
      - run: mvn -B verify
```

Caller:

```yaml
jobs:
  ci:
    uses: ./.github/workflows/reusable-java-ci.yml
    with:
      java-version: "21"
```

### Composite Actions
Package multiple steps into one local Action under `.github/actions/build/action.yml` for reuse inside the repo.

### Org rulesets / required checks
- Branch protection: require `CI / build-and-test` to be green
- CODEOWNERS for workflow file reviews
- Central reusable workflows in a shared `.github` repo (org)

---

## 14. Security best practices

| Practice | Why |
|---|---|
| Least-privilege `permissions:` | Default token can be too broad |
| Pin Actions (SHA for critical) | Avoid compromised tag moves |
| OIDC over static cloud keys | Short-lived creds |
| Environment approvals for prod | Human gate |
| Secret scanning + Dependabot | Catch leaks & CVEs |
| Don’t checkout untrusted PR code with write secrets | `pull_request` from forks has restricted secrets |
| Separate `pull_request` vs `push` deploy rights | Never deploy from untrusted forks |
| Review third-party Actions | They run in your job with your secrets |

### Fork PR caution
Secrets are **not** available to workflows from forked PRs by default (good). Use `pull_request_target` only when you truly understand the risks (easy to footgun).

### Dependency & image scanning in CI
```yaml
- run: npm audit --audit-level=high
# or SAST / Trivy / CodeQL
```

CodeQL example trigger: analyze on PR + push to main.

---

## 15. Debugging & common failures

| Symptom | Likely cause |
|---|---|
| Works locally, fails in CI | Missing env, wrong Java/Node version, unclean assumptions |
| Cache not restoring | Key mismatch (lockfile changed) |
| Secret empty | Wrong name / environment secret not attached to job `environment:` |
| OIDC assume-role fails | Trust policy repo/branch mismatch; missing `id-token: write` |
| Job skipped unexpectedly | `if:` condition false |
| Deployed wrong artifact | Rebuilt in deploy job instead of reusing artifact |
| Flaky tests | Shared state, timezones, missing retries for transient deps |

**Tips**
- Enable debug logging: secrets `ACTIONS_STEP_DEBUG=true`, `ACTIONS_RUNNER_DEBUG=true`
- Reproduce with [`act`](https://github.com/nektos/act) locally (optional)
- Keep scripts in `scripts/ci/*.sh` and call them from YAML for easier local runs

---

## 16. Quick cheatsheet

| Goal | GitHub Actions approach |
|---|---|
| Run tests on every PR | `on: pull_request` + job with `run: mvn test` / `npm test` |
| Deploy only from main | `if: github.ref == 'refs/heads/main'` |
| Manual prod deploy | `environment: production` (required reviewers) or `workflow_dispatch` |
| Share jar/image between jobs | `upload-artifact` / `download-artifact` |
| Speed up builds | toolchain `cache:` or `actions/cache` |
| Test Java 17 & 21 | `strategy.matrix` |
| AWS deploy without static keys | OIDC + `configure-aws-credentials` |
| Cancel old PR runs | `concurrency` + `cancel-in-progress: true` |
| Org-standard CI | `workflow_call` reusable workflows |
| Nightly job | `on.schedule` cron |

### One-liners
- Workflow YAML lives in **`.github/workflows/`**
- **Job** = one runner; **steps** share that workspace
- **Actions** = reusable steps (`uses:`)
- **Environments** = approvals + env-scoped secrets
- **OIDC** = temporary cloud login
- Build **once**, promote the **same artifact**

---

## 17. Starter examples

### Java CI (PR + main)

```yaml
name: Java CI

on:
  pull_request:
  push:
    branches: [main]

concurrency:
  group: java-ci-${{ github.ref }}
  cancel-in-progress: true

permissions:
  contents: read

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven

      - name: Test
        run: mvn -B verify
```

### CI + staging deploy + approved production

```yaml
name: Build and Deploy

on:
  push:
    branches: [main]
  workflow_dispatch:

permissions:
  contents: read
  id-token: write

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "21"
          cache: maven
      - run: mvn -B -DskipTests package
      - uses: actions/upload-artifact@v4
        with:
          name: app-jar
          path: target/*.jar

  deploy-staging:
    needs: build
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/download-artifact@v4
        with:
          name: app-jar
      - uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/gha-staging
          aws-region: ap-south-1
      - run: ./scripts/deploy.sh staging *.jar

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    environment: production   # required reviewers configured in GitHub UI
    steps:
      - uses: actions/download-artifact@v4
        with:
          name: app-jar
      - uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: arn:aws:iam::123456789012:role/gha-prod
          aws-region: ap-south-1
      - run: ./scripts/deploy.sh production *.jar
      - name: Smoke
        run: curl -fsS https://app.example.com/health
```

### Node CI matrix

```yaml
name: Node CI
on:
  pull_request:
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        node: ["18", "20", "22"]
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: ${{ matrix.node }}
          cache: npm
      - run: npm ci
      - run: npm test
```

---

## Suggested practice order

1. Add `.github/workflows/ci.yml` that runs tests on PR  
2. Require that check in branch protection  
3. Add caching + artifacts  
4. Deploy to staging automatically from `main`  
5. Add `production` environment with required reviewers  
6. Switch cloud auth to **OIDC**  
7. Extract a reusable workflow for other repos  

*Action versions and UI labels change — prioritize events → jobs → secrets/OIDC → environments → build-once/deploy-many.*
