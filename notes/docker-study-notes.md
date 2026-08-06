# Docker — Study Notes

Beginner-to-intermediate notes on **Docker**: images, containers, Dockerfiles, networking, volumes, Compose, multi-stage builds, registries, and how Docker fits CI/CD.

---

## Table of Contents

1. [What is Docker?](#1-what-is-docker)
2. [Core concepts](#2-core-concepts)
3. [Essential CLI](#3-essential-cli)
4. [Dockerfile basics](#4-dockerfile-basics)
5. [Build context & `.dockerignore`](#5-build-context--dockerignore)
6. [Layers, cache, and image size](#6-layers-cache-and-image-size)
7. [Multi-stage builds](#7-multi-stage-builds)
8. [Networking](#8-networking)
9. [Data: volumes & bind mounts](#9-data-volumes--bind-mounts)
10. [Docker Compose](#10-docker-compose)
11. [Registries & tagging](#11-registries--tagging)
12. [Environment config & secrets](#12-environment-config--secrets)
13. [Healthchecks, resources, logging](#13-healthchecks-resources-logging)
14. [Security basics](#14-security-basics)
15. [Docker in CI/CD (GitHub Actions)](#15-docker-in-cicd-github-actions)
16. [Containers vs VMs vs processes](#16-containers-vs-vms-vs-processes)
17. [Quick cheatsheet](#17-quick-cheatsheet)
18. [Starter examples](#18-starter-examples)

---

## 1. What is Docker?

**Docker** packages an app with its dependencies into a **container image** so it runs the same on a laptop, CI runner, or server.

```text
Code + runtime + libs + config  →  Image  →  Container (running instance)
```

**Why teams use it**
- “Works on my machine” → works in CI/prod
- Fast spin-up vs full VMs
- Clean dependency isolation
- Standard unit for deploy (ECS, Kubernetes, Cloud Run, App Runner, VMs)

**Docker Engine** = daemon (`dockerd`) + CLI (`docker`) that builds/runs containers.

---

## 2. Core concepts

| Term | Meaning |
|---|---|
| **Image** | Immutable template (layered filesystem + metadata) |
| **Container** | Running (or stopped) instance of an image |
| **Dockerfile** | Recipe to build an image |
| **Registry** | Store/share images (Docker Hub, GHCR, ECR) |
| **Tag** | Label on an image (`myapp:1.2.0`, `myapp:latest`) |
| **Volume** | Persistent data managed by Docker |
| **Network** | Virtual network so containers can talk |
| **Compose** | YAML to run multi-container apps |

```text
Dockerfile → docker build → Image → docker run → Container
                              ↓
                         docker push → Registry
```

### Image vs container
- Edit code → rebuild **image**
- `docker run` the same image many times → many **containers**
- Deleting a container does **not** delete the image

---

## 3. Essential CLI

### Images
```bash
docker build -t myapp:1.0 .
docker images
docker rmi myapp:1.0
docker pull nginx:1.27
docker push ghcr.io/org/myapp:1.0
```

### Containers
```bash
docker run --name web -p 8080:80 nginx:1.27
docker run -d --name web -p 8080:80 nginx:1.27   # detached
docker ps                                        # running
docker ps -a                                     # include stopped
docker logs -f web
docker exec -it web bash                         # or sh
docker stop web
docker rm web
docker rm -f web                                 # force remove
```

### System cleanup
```bash
docker system df
docker system prune          # unused data (careful)
docker volume prune
docker image prune -a
```

### Inspect / debug
```bash
docker inspect web
docker stats
docker port web
docker diff web              # filesystem changes vs image
```

---

## 4. Dockerfile basics

Common instructions:

| Instruction | Purpose |
|---|---|
| `FROM` | Base image |
| `WORKDIR` | Set working directory |
| `COPY` / `ADD` | Add files (`COPY` preferred; `ADD` has extra magic) |
| `RUN` | Execute build-time commands |
| `ENV` | Environment variables |
| `EXPOSE` | Document port (does not publish by itself) |
| `USER` | Drop privileges |
| `CMD` | Default start command |
| `ENTRYPOINT` | Fixed executable (args append/override carefully) |
| `ARG` | Build-time variables |
| `HEALTHCHECK` | Container health command |

### `CMD` vs `ENTRYPOINT`
| | Role |
|---|---|
| `ENTRYPOINT` | Main process (e.g. `java`) |
| `CMD` | Default args (e.g. `-jar app.jar`) |

```dockerfile
ENTRYPOINT ["java","-jar"]
CMD ["/app/app.jar"]
```

Prefer **exec form** (`["cmd","arg"]`) so PID 1 is your app (signals work for graceful stop).

### Simple Java example
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/app.jar /app/app.jar
EXPOSE 8080
USER nobody
CMD ["java","-jar","/app/app.jar"]
```

### Simple Node example
```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev
COPY . .
EXPOSE 3000
CMD ["node","server.js"]
```

---

## 5. Build context & `.dockerignore`

```bash
docker build -t myapp:1.0 .
```

The `.` is the **build context** — files sent to the daemon. Keep it small.

### `.dockerignore` (important)
```text
.git
.github
node_modules
target
*.md
.env
Dockerfile*
docker-compose*
```

Without this you may:
- Bloat images
- Bust cache constantly
- Leak secrets (`.env`) into build context

---

## 6. Layers, cache, and image size

Each Dockerfile instruction creates a **layer**. Docker reuses cached layers when inputs didn’t change.

### Cache-friendly order
```dockerfile
# 1) deps first (change less often)
COPY package*.json ./
RUN npm ci

# 2) app source last (changes often)
COPY . .
RUN npm run build
```

If you `COPY . .` before installing deps, **any** file change invalidates the dependency layer.

### Smaller images
| Technique | Why |
|---|---|
| Slim/alpine/distroless bases | Less attack surface & size |
| Multi-stage builds | Keep compilers out of final image |
| Delete package caches in same `RUN` | Avoid permanent layer bloat |
| Don’t copy secrets/build junk | `.dockerignore` |
| One app per container | Clearer scaling/deploy |

```dockerfile
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*
```

---

## 7. Multi-stage builds

Build in a heavy image; copy only the result into a tiny runtime image.

```dockerfile
# --- build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/app.jar /app/app.jar
USER nobody
CMD ["java","-jar","/app/app.jar"]
```

**Benefits**
- Final image has no Maven/source
- Smaller & safer
- Same pattern for Go, Node (build → nginx/node-slim), etc.

---

## 8. Networking

### Publish ports
```bash
docker run -p 8080:80 nginx
# host:container
```

`EXPOSE 80` only documents; `-p` actually publishes.

### Default bridge
Containers on the same custom network can resolve each other **by name**.

```bash
docker network create appnet
docker run -d --name db --network appnet postgres:16
docker run -d --name api --network appnet -p 8080:8080 myapi:1.0
# api connects to host "db"
```

### Network types (conceptual)
| Driver | Use |
|---|---|
| `bridge` | Default single-host container nets |
| `host` | Container shares host network (Linux) |
| `none` | No networking |
| Overlay | Multi-host (Swarm/k8s style networking elsewhere) |

**Compose** creates a project network automatically so services reach each other via service name.

---

## 9. Data: volumes & bind mounts

Containers are ephemeral — filesystem changes vanish when the container is removed (unless stored outside).

| Mechanism | Meaning | Typical use |
|---|---|---|
| **Named volume** | Docker-managed storage | DB data, persistent uploads |
| **Bind mount** | Map host folder → container | Live code reload in dev |
| **tmpfs** | Memory-only | Sensitive tmp files |

```bash
# named volume
docker volume create pgdata
docker run -v pgdata:/var/lib/postgresql/data postgres:16

# bind mount (dev)
docker run -v "$(pwd)":/app -w /app node:20 npm run dev
```

**Prod tip:** prefer named volumes for databases; don’t bind-mount source code in production.

---

## 10. Docker Compose

Define multi-container apps in `compose.yaml` / `docker-compose.yml`.

```yaml
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: postgres://postgres:postgres@db:5432/app
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: app
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  pgdata:
```

```bash
docker compose up --build
docker compose up -d
docker compose logs -f api
docker compose ps
docker compose down          # stop & remove containers/network
docker compose down -v       # also delete volumes (destructive)
```

**Good for:** local dev stacks (api + db + redis + mailhog).  
**Prod:** often Compose on a single VM, or translate services to ECS/Kubernetes.

---

## 11. Registries & tagging

### Tagging strategy
```bash
docker build -t ghcr.io/acme/api:1.4.2 .
docker tag  ghcr.io/acme/api:1.4.2 ghcr.io/acme/api:1.4
docker tag  ghcr.io/acme/api:1.4.2 ghcr.io/acme/api:sha-abc1234
docker push ghcr.io/acme/api:1.4.2
```

| Tag style | Notes |
|---|---|
| SemVer `1.4.2` | Releases |
| Git SHA | Traceable immutable deploys |
| `latest` | Convenient but **mutable** — avoid for prod pins |

### Registries
| Registry | Notes |
|---|---|
| Docker Hub | Public default |
| **GHCR** | `ghcr.io` — tight with GitHub Actions |
| **ECR** | AWS private registry |
| GCR/Artifact Registry | GCP |
| ACR | Azure |

**Deploy rule:** build once, push image, promote the **same digest** across environments.

```bash
docker images --digests
# deploy by digest: image@sha256:...
```

---

## 12. Environment config & secrets

### 12-factor style
- Image = code + runtime
- Config via env vars
- Secrets via env/files/secret manager — **not baked into image**

```bash
docker run --env-file .env.prod -e PORT=8080 myapp:1.0
```

```dockerfile
# OK: non-secret defaults
ENV PORT=8080
# BAD: never do this
ENV DB_PASSWORD=supersecret
```

### Build-time `ARG` vs runtime `ENV`
| | `ARG` | `ENV` |
|---|---|---|
| Available | Build time | Build + runtime |
| Persist in image | Not as env (unless copied to ENV) | Yes |

Don’t pass secrets as `ARG` — they can remain in image history/cache metadata.

---

## 13. Healthchecks, resources, logging

### Healthcheck
```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -fsS http://localhost:8080/health || exit 1
```

Orchestrators (Compose/ECS/K8s) use health to decide readiness.

### Resource limits
```bash
docker run --memory=512m --cpus=1.0 myapp:1.0
```

### Logs
- App should log to **stdout/stderr**
- `docker logs` / centralized drivers collect them
- Don’t log only to an internal file (unless shipped out)

### Graceful shutdown
- Use exec-form `CMD`/`ENTRYPOINT`
- Handle `SIGTERM` in the app
- `docker stop` sends SIGTERM then SIGKILL after timeout

---

## 14. Security basics

| Practice | Why |
|---|---|
| Non-root `USER` | Limit blast radius |
| Minimal base image | Fewer packages/CVEs |
| Multi-stage | No compilers/secrets in runtime |
| Pin base tags/digests | Reproducible & controlled upgrades |
| Scan images | Trivy/Grype/ECR scan in CI |
| Don’t mount Docker socket in prod apps | Root-equivalent escape risk |
| Read-only root FS when possible | Harden runtime |
| Drop Linux capabilities | Least privilege |
| Private registries + auth | Control supply chain |

```dockerfile
USER nobody
# or create explicit uid
RUN useradd -r -u 10001 appuser
USER appuser
```

Also: treat base images like dependencies — patch regularly.

---

## 15. Docker in CI/CD (GitHub Actions)

Common pipeline:

```text
PR  → build image (optional) → test
main → build → push GHCR/ECR → deploy same tag/digest
```

### Sketch
```yaml
name: Docker

on:
  push:
    branches: [main]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4

      - uses: docker/setup-buildx-action@v3

      - uses: docker/login-action@v3
        if: github.event_name != 'pull_request'
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - uses: docker/build-push-action@v6
        with:
          context: .
          push: ${{ github.event_name != 'pull_request' }}
          tags: |
            ghcr.io/${{ github.repository }}:sha-${{ github.sha }}
            ghcr.io/${{ github.repository }}:latest
          cache-from: type=gha
          cache-to: type=gha,mode=max
```

**Tips**
- Use Buildx + GitHub Actions cache for faster builds
- Push immutable tags (`sha-…`, semver), not only `latest`
- Scan image before deploy
- Deploy by digest for production

---

## 16. Containers vs VMs vs processes

| | Process | Container | VM |
|---|---|---|---|
| Isolation | Weak | OS-level (namespaces/cgroups) | Hardware-level |
| Startup | Instant | Seconds | Tens of seconds+ |
| Size | App only | App + deps (+ slim OS bits) | Full guest OS |
| Use | Local tools | App packaging & deploy | Strong isolation / different kernels |

Docker uses **images/containers**, not a replacement for all VM use cases (and not the same as Kubernetes — k8s **orchestrates** containers).

---

## 17. Quick cheatsheet

| Goal | Command / approach |
|---|---|
| Build image | `docker build -t name:tag .` |
| Run in background | `docker run -d -p host:container name:tag` |
| Shell into container | `docker exec -it name sh` |
| View logs | `docker logs -f name` |
| Stop/remove | `docker stop name && docker rm name` |
| Multi-service local | `docker compose up --build` |
| Persist DB files | named volume |
| Smaller prod image | multi-stage + slim base |
| Share image | tag + `docker push` |
| CI deploy unit | image digest |
| Config | runtime env / secret manager |
| Don’t copy into image | secrets, `.git`, `node_modules` |

### One-liners
- **Image** = template; **container** = running instance  
- **Dockerfile** layers are cached — order matters  
- **Compose** runs multi-container apps locally  
- **Volumes** keep data; containers are disposable  
- Build **once**, promote the **same image** across envs  

---

## 18. Starter examples

### Dev: API + Postgres + Redis (`compose.yaml`)
```yaml
services:
  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/app
      REDIS_URL: redis://redis:6379
    depends_on:
      - db
      - redis

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: app
      POSTGRES_USER: app
      POSTGRES_PASSWORD: app
    volumes:
      - pgdata:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

volumes:
  pgdata:
```

### Multi-stage Node → small runtime
```dockerfile
FROM node:20-alpine AS build
WORKDIR /src
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production
COPY package*.json ./
RUN npm ci --omit=dev
COPY --from=build /src/dist ./dist
USER node
CMD ["node","dist/index.js"]
```

### Useful local workflow
```bash
docker compose up --build
docker compose exec api sh
docker compose logs -f
docker compose down
```

---

## Suggested practice order

1. Run `nginx` with `-p 8080:80` and visit localhost  
2. Write a Dockerfile for a small app; use `.dockerignore`  
3. Optimize layer order; compare image sizes  
4. Convert to multi-stage build  
5. Add Compose with app + database volume  
6. Push an image to GHCR/Docker Hub  
7. Build/push from GitHub Actions and deploy that tag  

*CLI flags and Compose file versions evolve — focus on image vs container, layers/cache, volumes/networks, multi-stage, and immutable registry tags/digests.*
