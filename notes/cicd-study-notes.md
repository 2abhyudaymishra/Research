# CI/CD — Study Notes

Beginner-to-intermediate notes on **Continuous Integration** and **Continuous Delivery/Deployment**: concepts, pipeline stages, tools (especially AWS), strategies, and interview/exam patterns.

---

## Table of Contents

1. [What is CI/CD?](#1-what-is-cicd)
2. [Pipeline stages](#2-pipeline-stages)
3. [Source control](#3-source-control)
4. [Build & test](#4-build--test)
5. [Artifacts & registries](#5-artifacts--registries)
6. [Deploy strategies](#6-deploy-strategies)
7. [Infrastructure as Code](#7-infrastructure-as-code)
8. [AWS CI/CD tools](#8-aws-cicd-tools)
9. [Other popular tools](#9-other-popular-tools)
10. [Security in the pipeline (DevSecOps)](#10-security-in-the-pipeline-devsecops)
11. [Quality gates & rollback](#11-quality-gates--rollback)
12. [Common patterns & anti-patterns](#12-common-patterns--anti-patterns)
13. [Quick cheatsheet](#13-quick-cheatsheet)

---

## 1. What is CI/CD?

### Continuous Integration (CI)
Developers merge code **frequently** into a shared branch. Each merge triggers an automated **build + test** so bugs are found early.

**Goals**
- Catch integration problems quickly
- Keep `main`/`master` always buildable
- Fast feedback on every commit/PR

### Continuous Delivery
Every change that passes tests can be **released to production at any time** with a manual approval button (or light gate).

### Continuous Deployment
Every change that passes automated checks is **automatically released** to production (no manual click).

```text
CI  = automate build + test on every change
CD (Delivery) = always releasable; human may approve prod
CD (Deployment) = auto-release to prod when green
```

### Why it matters
- Smaller, safer releases
- Faster recovery (easy to redeploy last good version)
- Less “works on my machine”
- Repeatable environments

---

## 2. Pipeline stages

Typical flow:

```text
Source → Build → Test → Package/Artifact → Deploy (Dev → Staging → Prod)
                              ↓
                     Security scans / quality gates
```

| Stage | What happens |
|---|---|
| **Source** | Clone repo; trigger on push/PR/tag/schedule |
| **Build** | Compile, resolve deps, create binary/image |
| **Unit test** | Fast tests of functions/classes |
| **Static analysis** | Linters, type checks, SAST |
| **Package** | JAR/WAR, Docker image, zip, npm pack |
| **Integration / e2e** | Tests against real/test dependencies |
| **Deploy** | Push to environment (Beanstalk, ECS, Lambda, K8s, EC2…) |
| **Verify** | Smoke tests, health checks, canary metrics |
| **Rollback** | Revert traffic / redeploy previous artifact |

**Triggers**
- Push to branch
- Pull request opened/updated
- Tag / release
- Cron / scheduled
- Manual / API

---

## 3. Source control

### Branching strategies (know the tradeoffs)

| Strategy | Idea | Fit |
|---|---|---|
| **Trunk-based** | Short-lived branches; merge to trunk often | Strong CI culture, feature flags |
| **GitHub Flow** | `main` + feature branches + PR | Most product teams |
| **Git Flow** | `develop`, `release`, `hotfix`, `main` | Versioned releases; heavier |

### Good CI habits
- Protect `main` (required reviews + green checks)
- Small PRs
- Prefer merge/rebase policies that keep history understandable
- Tag releases that map to deployed artifacts

### Hosting
GitHub, GitLab, Bitbucket, AWS **CodeCommit**, Azure Repos, self-hosted Git.

---

## 4. Build & test

### Build
- Language toolchains: Maven/Gradle (Java), npm/yarn (Node), pip/poetry (Python), go build, etc.
- **Reproducible builds**: lockfiles, pinned base images, deterministic flags
- Cache dependencies to speed pipelines

### Test pyramid
```text
        /\
       /E2E\        ← few, slow, expensive
      /------\
     / Integr \     ← medium
    /----------\
   / Unit tests \   ← many, fast
  /--------------\
```

| Type | Scope |
|---|---|
| Unit | Isolated logic |
| Integration | DB, queue, API boundaries |
| Contract | Provider/consumer API agreements |
| E2E / UI | Full user flows |
| Smoke | Tiny post-deploy “is it alive?” |

### Fail fast
Run cheapest checks first (lint/unit) before heavy e2e.

---

## 5. Artifacts & registries

An **artifact** is the immutable output of a build: jar, zip, container image, static site bundle.

**Rules of thumb**
- Deploy **the same artifact** through Dev → Staging → Prod (don’t rebuild per env)
- Version artifacts (`1.4.2`, git SHA, build number)
- Store in a registry/repo

| Artifact type | Registry examples |
|---|---|
| Container images | ECR, Docker Hub, GHCR, GCR |
| Language packages | CodeArtifact, Nexus, Artifactory, npm |
| Generic files | S3, GitHub Releases |
| IaC templates | S3 + CloudFormation, Terraform modules registry |

---

## 6. Deploy strategies

| Strategy | How it works | Risk / notes |
|---|---|---|
| **Recreate / all-at-once** | Stop old, start new | Downtime; simplest |
| **Rolling** | Replace instances gradually | Low downtime; mixed versions briefly |
| **Blue/Green** | Two environments; switch traffic | Fast rollback; needs 2× capacity |
| **Canary** | Send small % traffic to new version | Great safety; needs metrics |
| **Linear** | Increase traffic in steps (10%…100%) | Similar to canary, scheduled steps |
| **Shadow** | Mirror traffic to new version (no user impact) | Validation; extra cost |
| **Feature flags** | Deploy dark; enable for users later | Decouples deploy from release |

### Lambda-specific (AWS CodeDeploy)
- All-at-once
- Canary (e.g. 10% for 5 minutes, then 100%)
- Linear (e.g. 10% every 10 minutes)
- Alias traffic shifting + CloudWatch alarms → auto rollback

### Health & readiness
- Liveness: process up
- Readiness: can take traffic (warmup DB connections, caches)
- Never send traffic to non-ready instances

---

## 7. Infrastructure as Code

IaC = define infra in files, review/version it like app code, apply via pipeline.

| Tool | Notes |
|---|---|
| **CloudFormation** | AWS-native JSON/YAML |
| **SAM** | Serverless apps on top of CloudFormation |
| **CDK** | IaC in TypeScript/Python/Java/etc. → CloudFormation |
| **Terraform** | Multi-cloud HCL |
| **Pulumi** | Multi-cloud with general-purpose languages |
| **Ansible** | Config management / procedural automation |
| **Helm** | Kubernetes package charts |

**Pipeline tip:** plan/diff in PR; apply on merge to main (with approvals for prod).

---

## 8. AWS CI/CD tools

### CodeCommit
- Managed private Git repos
- IAM auth; integrates with CodePipeline

### CodeBuild
- Managed build service (pay per build-minute)
- Spec file: `buildspec.yml`
- Phases: install → pre_build → build → post_build
- Can build Docker images and push to **ECR**
- Runs in isolated environment; attach IAM role for AWS API access

```yaml
# buildspec.yml (conceptual)
version: 0.2
phases:
  install:
    runtime-versions:
      java: corretto21
  build:
    commands:
      - mvn -B package
artifacts:
  files:
    - target/*.jar
```

### CodeDeploy
- Deploys to **EC2/on-prem**, **ECS**, **Lambda**
- AppSpec file (`appspec.yml` / `appspec.yaml`)
- Hooks: BeforeInstall, AfterInstall, ApplicationStart, ValidateService, etc.
- Deployment configs: all-at-once, rolling, blue/green, canary/linear (Lambda/ECS)

### CodePipeline
- Orchestrates stages: Source → Build → Deploy (and more)
- Connects CodeCommit/GitHub/S3 → CodeBuild → CodeDeploy/CloudFormation/ECS/Elastic Beanstalk…
- Manual approval actions between stages
- Artifact store usually **S3**

### CodeArtifact
- Managed artifact repository (npm, Maven, PyPI, etc.)
- Reduces reliance on public registries; private packages

### Related AWS deploy targets
| Target | Typical path |
|---|---|
| Elastic Beanstalk | Upload bundle / pipeline deploy |
| ECS/Fargate | Build image → ECR → new task definition → service update |
| Lambda | Build zip/image → update function → (CodeDeploy alias shift) |
| CloudFormation/SAM/CDK | Pipeline deploys stack updates |
| S3 + CloudFront | Sync static assets; invalidate CDN |

### Minimal AWS pipeline mental model
```text
GitHub/CodeCommit
    → CodePipeline
        → CodeBuild (test + package)
        → CodeDeploy / CloudFormation / ECS
        → CloudWatch alarms gate rollback
```

---

## 9. Other popular tools

| Area | Tools |
|---|---|
| All-in-one | GitHub Actions, GitLab CI, Bitbucket Pipelines, Azure DevOps |
| Classic CI servers | Jenkins, TeamCity |
| CD / GitOps | Argo CD, Flux, Spinnaker |
| Build | Maven, Gradle, Bazel, Make |
| Containers | Docker, BuildKit, Kaniko |
| Quality | SonarQube, ESLint, Checkstyle, JaCoCo |

### GitHub Actions (conceptual)
- Workflow YAML under `.github/workflows/`
- Events: `push`, `pull_request`, `workflow_dispatch`
- Jobs/steps on runners (GitHub-hosted or self-hosted)

### Jenkins (conceptual)
- Freestyle or **Jenkinsfile** (Pipeline as Code)
- Plugins-heavy; you manage the controller/agents
- Still common in enterprises

---

## 10. Security in the pipeline (DevSecOps)

Shift security **left** (earlier), automate it.

| Check | What |
|---|---|
| **SAST** | Static analysis of source |
| **SCA** | Dependency / CVE scanning |
| **Secret scanning** | Block committed keys/tokens |
| **Container scan** | Image vulnerabilities |
| **IaC scan** | Misconfigured security groups, public buckets |
| **DAST** | Runtime/black-box tests in staging |
| **Sign & verify** | Sign images/artifacts; verify before deploy |

**Secrets in CI**
- Prefer OIDC/role assumption over long-lived keys (e.g. GitHub Actions → AWS IAM)
- Store secrets in Secrets Manager / SSM / vault — not in repo or plain env dumps
- Least-privilege job roles

---

## 11. Quality gates & rollback

### Gates (examples)
- Unit tests must pass
- Coverage threshold
- No critical CVEs
- Staging smoke tests green
- Manual approval for production
- CloudWatch error rate / latency within SLO during canary

### Rollback approaches
| Approach | When |
|---|---|
| Redeploy previous artifact | Immutable releases |
| Traffic shift back (blue/green, canary) | LB / Lambda alias / mesh |
| Feature flag off | Code already deployed |
| DB migrate care | Prefer backward-compatible migrations (expand/contract) |

**Database note:** app rollback ≠ DB rollback. Design migrations to be compatible with both old and new app versions during rolling deploys.

---

## 12. Common patterns & anti-patterns

### Patterns
- Build once, deploy many
- Immutable artifacts + version tags
- Environment parity (dev ≈ staging ≈ prod)
- Automated smoke tests after deploy
- Observability first (metrics/logs/traces before release)
- Progressive delivery (canary + auto rollback)

### Anti-patterns
- “Works on my machine” snowflake servers
- Manual FTP/SSH production changes
- Rebuilding separately for prod with different flags secretly
- Storing secrets in Git
- Huge long-lived feature branches
- No rollback plan
- Testing only in production

---

## 13. Quick cheatsheet

| Question | Prefer |
|---|---|
| Automate build/test on every PR | CI (Actions, CodeBuild, Jenkins…) |
| Always able to release safely | Continuous Delivery |
| Auto-release every green commit | Continuous Deployment |
| Orchestrate AWS stages | CodePipeline |
| Compile/test on AWS | CodeBuild + `buildspec.yml` |
| Deploy to EC2/ECS/Lambda | CodeDeploy |
| Private Maven/npm | CodeArtifact |
| Container images on AWS | ECR |
| Safer prod rollout | Canary / blue-green + alarms |
| Rollback Lambda traffic | Alias + previous version |
| Infra changes with review | IaC in the same pipeline |
| Avoid secret sprawl | IAM roles / OIDC + Secrets Manager |

### One-line definitions to memorize
- **CI** — integrate and verify continuously  
- **Artifact** — immutable build output you promote  
- **Canary** — small % of users on new version first  
- **Blue/Green** — two full environments; flip traffic  
- **GitOps** — desired state in Git; controller reconciles cluster  

---

## Suggested practice order

1. Write a tiny app + unit tests  
2. Add a pipeline that builds and tests on PR  
3. Publish an artifact (jar or Docker image)  
4. Deploy to a non-prod environment automatically  
5. Add a manual approval + canary/blue-green to prod  
6. Add dependency scanning and a forced rollback alarm  

*Tool UIs and free tiers change — prioritize concepts, stage order, and deploy/rollback strategies.*
