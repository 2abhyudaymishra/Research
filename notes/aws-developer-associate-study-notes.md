# AWS Developer Associate (DVA-C02) — Study Notes

Refined and expanded notes for core AWS services commonly tested on the Developer Associate exam.

---

## Table of Contents

1. [Databases](#1-databases)
2. [Caching](#2-caching)
3. [Storage](#3-storage)
4. [Messaging & Eventing](#4-messaging--eventing)
5. [Compute](#5-compute)
6. [API & Application Integration](#6-api--application-integration)
7. [Identity, Security & Encryption](#7-identity-security--encryption)
8. [Monitoring, Logging & Tracing](#8-monitoring-logging--tracing)
9. [CI/CD & Developer Tools](#9-cicd--developer-tools)
10. [Networking & Content Delivery (developer-relevant)](#10-networking--content-delivery-developer-relevant)
11. [Quick Decision Cheatsheet](#11-quick-decision-cheatsheet)

---

## 1. Databases

### DynamoDB

**What it is**  
Serverless, fully managed, distributed NoSQL key-value / document database. Single-digit millisecond performance at any scale. Designed for 99.999% availability (global tables) and high resilience.

**Core behavior**
- Automatically scales throughput to meet demand; partitions/repartitions data as the table grows.
- Max item size: **400 KB**.
- Data remains indefinitely unless **TTL** is configured (TTL deletes items after an expiry attribute).
- Supports **List** and **Map** types (handles serialization/deserialization). Prefer these over stuffing JSON strings when you need nested structure.
- Prefer storing large binaries/images in **S3** and saving the object URL/key in DynamoDB (cheaper and avoids the 400 KB limit).
- IAM controls access; data encrypted at rest; API activity auditable via **CloudTrail**.

**Table classes**
| Class | When to use | Cost profile |
|---|---|---|
| **Standard** | Max performance; unpredictable / frequently accessed workloads | Higher storage, lower R/W relative cost |
| **Standard-IA (SIA)** | Infrequently accessed data; storage-dominant cost | Lower storage, higher R/W cost |

- You can switch table class **once every 30 days**, with **no downtime**.

**Capacity modes**
- **On-Demand**: pay per request; good for unpredictable traffic.
- **Provisioned**: set WCU/RCU (optionally with Auto Scaling); good for steady/predictable traffic.
- **WCU**: 1 write/sec for item ≤ 1 KB (round up).
- **RCU (eventually consistent)**: 1 RCU = 2 eventually consistent reads/sec for ≤ 4 KB.
- **RCU (strongly consistent)**: 1 RCU = 1 strongly consistent read/sec for ≤ 4 KB.
- **Transactional** reads/writes consume **2×** the capacity of standard operations.

**Keys & indexes**
- Primary key: **Partition key** only, or **Partition + Sort** key.
- **LSI** (Local Secondary Index): same partition key, different sort key; must be created with the table; shares table’s capacity.
- **GSI** (Global Secondary Index): different partition (and optional sort) key; has its own capacity; can be added later.
- Query vs Scan: **Query** is efficient (uses key); **Scan** reads the whole table/index — avoid on large tables.

**Consistency**
- Default: **eventually consistent** reads.
- Optional: **strongly consistent** reads (not available on all GSI scenarios; costs more RCU).

**Global tables**
- Multi-region, multi-active replication (one replica table per region, **same name**).
- Used for low-latency local R/W and multi-region resilience.
- Requires streams enabled under the hood.

**Streams vs Kinesis for DynamoDB**
| Need | Choose |
|---|---|
| Track DynamoDB item-level changes (insert/update/delete) for Lambda triggers, CDC within AWS | **DynamoDB Streams** (ordered per shard; 24h retention) |
| Broader streaming, higher throughput, longer retention, fan-out to many consumers | **Kinesis Data Streams for DynamoDB** |

- Kinesis Data Streams for DynamoDB does **not** guarantee record ordering or deduplication; application must handle that if required.

**DAX (DynamoDB Accelerator)**
- In-memory, DynamoDB-compatible cache.
- Microsecond reads for eventually consistent access patterns; reduces hot-key latency and RCU usage.
- Not a substitute when you need strongly consistent reads from the source of truth.

**Backup / recovery**
- **Point-in-time recovery (PITR)** can be enabled (continuous backups, restore to any second within retention window — typically up to 35 days).
- On-demand backups also available.

**Billing (high level)**
- Always Free (subject to AWS Free Tier terms): **25 GB** storage, **25** provisioned WCU, **25** provisioned RCU (enough for a modest learning/dev workload; often cited as ~200M requests/month under light provisioned use — treat as approximate).
- Paid: storage + RCU/WCU (or on-demand request units) + optional features (streams, backups, DAX, global tables, etc.).

**Exam tips**
- Images/files → S3 + key/URL in DynamoDB.
- Unpredictable traffic → On-Demand; steady → Provisioned + Auto Scaling.
- Need secondary access patterns → GSI (or LSI if same partition key and known at create time).
- Cross-region active-active → Global Tables.
- Cache DynamoDB reads → DAX.

---

### RDS (Relational Database Service)

**What it is**  
Managed relational databases (MySQL, PostgreSQL, MariaDB, Oracle, SQL Server, etc.). Automates provisioning, patching, backups, and much of operational management — you still choose engine, instance class, storage, and networking.

**vs running DB on EC2**
- EC2 + AMI: you manage OS, patching, backups, HA yourself.
- RDS: AWS manages undifferentiated DB ops; you focus on schema/app.

**Key features**
- Scale compute/storage (often with brief disruption depending on change).
- **Multi-AZ**: synchronous standby in another AZ for HA/failover (not primarily for read scaling).
- **Read Replicas**: async copies for read scaling (and optional cross-region DR).
- Automated backups + manual snapshots; restore to a point in time (within retention).
- Encryption at rest (KMS), IAM DB auth (supported engines), Security Groups for network access.
- Parameter groups / option groups for engine config.

**Pricing (conceptual)**
- Charged for: instance hours + storage (GB/month) + provisioned IOPS (if used) + backup storage beyond free allocation + data transfer.
- Free Tier typically includes limited hours of a small instance class + limited storage (check current Free Tier; promotional credits vary by program).

**Exam tips**
- Need SQL + joins + transactions → RDS (or Aurora).
- HA for RDS → Multi-AZ.
- Read-heavy → Read Replicas.
- Fully serverless relational option → consider **Aurora Serverless**.

---

### Aurora

**What it is**  
AWS-designed MySQL- and PostgreSQL-compatible relational engine. Storage auto-grows; typically higher throughput than stock RDS engines for many workloads.

**Highlights**
- Shared storage volume replicated across **3 AZs** (6 copies conceptually).
- Faster failover than classic RDS Multi-AZ in many cases.
- Up to 15 Aurora Replicas (same region).
- **Aurora Global Database** for cross-region low-latency reads / DR.
- **Aurora Serverless v2**: scales capacity automatically (good for variable/spiky traffic).
- Backtrack (MySQL-compatible) for “rewind” without restoring a snapshot (feature-limited).

**When to choose Aurora over RDS**
- Need higher performance/availability, auto-scaling storage, or Serverless capacity model.

---

### Other DB options (know the “when”)

| Service | Use when |
|---|---|
| **DocumentDB** | MongoDB-compatible document workloads |
| **Neptune** | Graph relationships |
| **Timestream** | Time-series |
| **Keyspaces** | Cassandra-compatible |
| **QLDB** | Immutable ledger / verifiable history |
| **Redshift** | Data warehouse / analytics (OLAP), not OLTP app DB |

---

## 2. Caching

### ElastiCache

**What it is**  
Managed in-memory caching for **Valkey**, **Redis OSS**, or **Memcached**. Microsecond latency; up to **99.99%** availability SLA (depending on configuration). Improves latency/throughput for read-heavy workloads and reduces load on RDS/Aurora/DocumentDB/DynamoDB.

**Deployment styles**
- **ElastiCache Serverless**: least ops — no infrastructure management, scaling, pay-for-use (data stored + ECPU).
- Node-based clusters: more control; scale node count/type; Multi-AZ for HA.

**Redis/Valkey vs Memcached (exam contrast)**
| | Redis / Valkey | Memcached |
|---|---|---|
| Data structures | Rich (strings, lists, sets, sorted sets, hashes, streams…) | Simple key-value |
| Persistence / snapshots | Yes (engine-dependent) | No |
| Replication / Multi-AZ | Yes | No (distributed but not replicated HA the same way) |
| Use | Sessions, leaderboards, pub/sub, caching with richer needs | Simple, horizontal cache shards |

**HA & ops**
- **Multi-AZ**: replicates across AZs for high availability.
- Node count can often be changed without downtime (engine/config dependent).
- CloudWatch metrics for CPU, memory, evictions, connections — set alarms for overloaded caches.

**Backup & restore (Redis/Valkey-oriented)**
- Snapshots stored in S3 in the same Region.
- Manual snapshots can be retained (including options when deleting a cache).
- Automatic snapshots are managed on a schedule and deleted per retention policy.
- Manual or recurring daily backups; retention typically **up to 35 days**.

**Read replicas**
- Read-only copies synchronized from primary; offload read-heavy traffic (Redis/Valkey replication model).

**Billing**
- Serverless: GB stored + ECPU.
- Node-based: instance hours + optional Reserved Instances (1- or 3-year discount in a Region).

**Exam tips**
- Front-end cache for RDS/Aurora/DynamoDB → ElastiCache.
- DynamoDB-specific cache → DAX (API-compatible with DynamoDB).
- Session store / sorted sets → Redis/Valkey, not Memcached.

---

## 3. Storage

### S3 (Simple Storage Service)

**What it is**  
Object storage — virtually unlimited, highly durable (**11 9s** durability design target), accessible via HTTP APIs. Fundamental for static assets, backups, data lakes, and app object storage.

**Core concepts**
- **Bucket** (globally unique name) → **Objects** (key + data + metadata).
- Max object size: **5 TB** (multipart upload required above 5 GB).
- Strong read-after-write consistency for PUT/overwrite/DELETE.

**Storage classes (know tradeoffs)**
| Class | Pattern |
|---|---|
| **S3 Standard** | Frequent access |
| **S3 Standard-IA** | Infrequent, rapid retrieval |
| **S3 One Zone-IA** | Infrequent, single AZ (cheaper, less resilient) |
| **S3 Glacier Instant Retrieval** | Archive, milliseconds retrieval |
| **S3 Glacier Flexible Retrieval** | Archive, minutes–hours |
| **S3 Glacier Deep Archive** | Cheapest archive, hours |
| **S3 Intelligent-Tiering** | Auto-moves between tiers based on access |

**Security & access**
- Block Public Access (default safe posture).
- Bucket policies, IAM, ACLs (prefer policies/IAM over ACLs).
- Encryption: SSE-S3, SSE-KMS, SSE-C; also client-side.
- Pre-signed URLs for temporary access without making bucket public.
- Versioning + MFA Delete for protection against overwrite/delete.
- Object Lock (WORM) for compliance.

**Features developers use often**
- Event notifications → Lambda / SQS / SNS / EventBridge.
- Static website hosting (or better: CloudFront + S3).
- Lifecycle rules (transition/expire).
- Replication (CRR/SRR).
- Transfer Acceleration, Multipart Upload, Select, Inventory.

**Billing**
- Storage + requests + data transfer out + optional features.
- Same-region transfers to many AWS services often free or reduced — check current pricing.

**Exam tips**
- Large media → S3, not DynamoDB/EBS for sharing.
- Temporary upload/download for users → pre-signed URL.
- Trigger processing on upload → S3 event → Lambda/SQS.

---

### EBS (Elastic Block Store)

**What it is**  
Block storage attached to an **EC2** instance (like a network hard drive). Create filesystems, run databases, or use as raw block devices. Attached to a **single instance** in the **same AZ** (except Multi-Attach volumes).

**Volume types**
**SSD**
| Type | Notes |
|---|---|
| **io2 Block Express** | Highest performance SSD; **sub-millisecond** latency (not “&lt;500 ms”) |
| **io1 / io2** | Provisioned IOPS; I/O-intensive DBs; io1 up to ~1,000 MB/s throughput per volume (check current limits) |
| **gp3** | General purpose; baseline **3,000 IOPS** and **125 MiB/s** independent of size; tunable IOPS/throughput |
| **gp2** | General purpose; performance scales with size (burst); up to 16,000 IOPS / ~250 MB/s |

**HDD**
| Type | Notes |
|---|---|
| **st1** | Throughput HDD (~40 MB/s per TB baseline pattern) — big sequential workloads |
| **sc1** | Cold HDD (~12 MB/s per TB pattern) — infrequent access |

**Ops & cost**
- Snapshots are incremental and stored in **S3** (managed).
- Can create volume clones / restore from snapshot (often same AZ considerations for attach).
- Billed for provisioned size (and provisioned IOPS where applicable) **even if detached**.
- Detached unused volumes: snapshot then delete to save cost.
- **Multi-Attach**: supported on certain Provisioned IOPS volumes (io1/io2) for multi-instance attach in one AZ — still billed for GB-Mo and IOPS-Mo.

**Exam tips**
- Persist EC2 disk beyond instance stop/terminate carefully (root volume delete-on-termination setting).
- Shareable file storage across instances/AZs → **EFS**, not EBS.
- Object storage / static files → **S3**.

---

### EFS & Instance Store (quick)

| Service | Pattern |
|---|---|
| **EFS** | NFS shared file system; multi-AZ; scale automatically; mount on many EC2/Lambda (with config) |
| **Instance Store** | Ephemeral local disk on some EC2 types; lost on stop/terminate; extreme I/O temp data |

---

## 4. Messaging & Eventing

### SQS (Simple Queue Service)

**What it is**  
Fully managed message **queue**. Decouples producers and consumers. Massive scale, high durability, minimal admin.

**Standard vs FIFO**
| | Standard | FIFO |
|---|---|---|
| Ordering | Best-effort | Strict order per message group |
| Delivery | At-least-once | Exactly-once processing (with deduplication) |
| Throughput | Nearly unlimited | Higher limits with batching / high-throughput mode |
| Use | Most decoupling | Orders, payments, anything needing order + dedupe |

**Important mechanics**
- Consumers **poll** (short or long poll). Long poll recommended; **do not set WaitTimeSeconds &gt; 20**.
- **Visibility timeout**: after receive, message hidden so others don’t process it; if not deleted in time, it reappears.
- Delete requires the **receipt handle** (not just message ID). Message IDs exist for tracking.
- Up to **10 message attributes** (name-type-value).
- Body size up to **256 KB** (use S3 for larger payloads + pointer pattern / Extended Client Library).
- **Dead Letter Queue (DLQ)**: after maxReceiveCount failures, move to DLQ for inspection.
- Latency typically tens of ms (order of ~10–100 ms depending on pattern).

**SQS vs SNS vs MQ**
- **SNS**: push/fan-out publish-subscribe.
- **SQS**: pull/queue between components.
- **Amazon MQ**: managed ActiveMQ/RabbitMQ — best when migrating **existing** broker-based apps.
- Greenfield AWS-native apps → prefer **SQS + SNS** (and EventBridge).

**Billing**
- Free Tier: **1 million requests/month** (not accumulated across months); payload-related charges may still apply.
- Requests billed in **64 KB chunks** (each 64 KB of payload = 1 request unit).
- No data transfer charge when interacting with services in the **same Region** (typical pattern); S3/KMS usage billed separately if used.

**Exam tips**
- Decouple microservices / absorb spikes → SQS.
- Need pub/sub fan-out to many queues → SNS → multiple SQS subscriptions.
- Poison messages → DLQ + redrive.

---

### SNS (Simple Notification Service)

**What it is**  
Pub/sub messaging: publish once, deliver to many subscribers. Scalable, flexible, cost-effective fan-out.

**Topics**
- **Standard topics**: best-effort ordering, at-least-once, high throughput.
- **FIFO topics**: ordering + deduplication; often paired with SQS FIFO.

**Scale (order-of-magnitude limits to remember)**
- On the order of **100,000** Standard topics per account; a topic can support on the order of **millions** of subscriptions (AWS documents ~12.5M — verify current quotas).

**Subscribers / destinations**
- SQS, Lambda, HTTP/S, email, SMS, mobile push, EventBridge (patterns evolve — know SQS/Lambda/HTTP/SMS/push as classics).

**Features**
- Message filtering (subscription filter policies) — subscribers only get matching messages.
- Message encryption, DLQ support for failed deliveries (where applicable).
- Fan-out: one event → many processing pipelines.
- Integrates with **CloudTrail** for API auditing.

**Exam tips**
- One event, many consumers → SNS fan-out (often SNS → SQS per consumer).
- Filter who gets what → subscription filter policies.
- Mobile push / SMS alerts → SNS.

---

### EventBridge (CloudWatch Events successor)

**What it is**  
Serverless event bus for application events, AWS service events, and SaaS partners.

**Why it matters for DVA**
- Route events with **rules** to Lambda, SQS, SNS, Step Functions, API destinations, etc.
- Schema registry, archive/replay, scheduled (cron/rate) rules.
- Prefer EventBridge over raw CloudWatch Events naming in modern designs.
- Decouple producers from consumers with content-based routing (better than SNS when you need rich event patterns across accounts/buses).

---

### Kinesis (streaming)

| Service | Role |
|---|---|
| **Kinesis Data Streams** | Real-time streaming ingest; shards; multiple consumers; retention hours–days (extendable) |
| **Kinesis Data Firehose** | Load streams into S3/Redshift/OpenSearch/Splunk with less ops |
| **Kinesis Data Analytics** | SQL/Apache Flink on streams |
| **Kinesis Video Streams** | Video ingest |

**vs SQS**
- SQS: messaging/decoupling, individual message ack/delete.
- Kinesis: ordered streaming, multiple consumers on same data, analytics/replay within retention.

---

### Step Functions

**What it is**  
Orchestrate workflows (state machines) across Lambda and other AWS services. Handles retries, branching, parallel steps, human approval patterns, long-running processes.

**Exam tips**
- Replace complex Lambda-to-Lambda chaining with Step Functions.
- Standard vs Express workflows: durable long-running vs high-volume short event-driven.

---

## 5. Compute

### Lambda

**What it is**  
Serverless functions — upload code, AWS runs it on events. Pay for duration × memory (GB-seconds) + requests.

**Limits & behavior to know**
- Max timeout: **15 minutes**.
- Memory: 128 MB–10,240 MB (CPU scales with memory).
- Deployment package limits (zip vs container image — know that large deps may need layers or container images).
- **Environment variables**; secrets via Secrets Manager / SSM (prefer not hardcoding).
- **Execution role** (IAM) grants permissions to AWS APIs.
- Triggers: API Gateway, S3, DynamoDB Streams, SQS, SNS, EventBridge, ALB, etc.
- **Concurrency**: reserved vs provisioned concurrency (cold starts).
- VPC-attached Lambda needs ENIs / Hyperplane — watch cold start and subnet IP capacity.
- Versions + aliases for safe deployments; Lambda Function URLs for simple HTTPS endpoints.

**Exam tips**
- Short event-driven compute → Lambda.
- Need &gt;15 min or specialized runtime/host → Fargate/EC2/Batch.
- SQS + Lambda: partial batch failure reporting; DLQ/on-failure destinations.

---

### EC2 (developer view)

- VMs you manage (AMI, instance type, security groups, user data).
- Use when you need full OS control, long-running processes, or custom runtimes.
- Pair with **ASG** + **ALB** for scalable web tiers.
- Roles via **instance profiles** (never embed long-lived keys on instances).

---

### Elastic Beanstalk

**What it is**  
PaaS-style deploy: upload app; Beanstalk provisions EC2/ASG/ALB/RDS (optional) etc.

**Developer value**
- Platforms for Node, Java, Python, .NET, Go, Docker, etc.
- Rolling / immutable / blue-green style deployments.
- `.ebextensions` / `.platform` hooks for config.
- Good exam answer when: “deploy web app quickly without managing infra, but still on EC2 under the hood.”

---

### Containers: ECS, EKS, Fargate

| Option | Notes |
|---|---|
| **ECS** | AWS-native container orchestration |
| **EKS** | Managed Kubernetes |
| **Fargate** | Serverless containers (no EC2 to manage) with ECS or EKS |
| **ECR** | Container image registry |

**Exam tips**
- Don’t want to manage servers for containers → Fargate.
- Need Kubernetes APIs/portability → EKS.
- Simple AWS-integrated containers → ECS (+ Fargate).

---

## 6. API & Application Integration

### API Gateway

**What it is**  
Managed front door for REST, HTTP, and WebSocket APIs.

**Know for the exam**
- Integration types: Lambda proxy, HTTP, AWS services, mock.
- Auth: IAM, Cognito authorizers, Lambda authorizers, API keys + usage plans.
- Stages, stage variables, canary releases.
- Throttling, caching, request/response mapping (REST).
- CORS configuration (common troubleshooting topic).
- REST API vs HTTP API: HTTP API cheaper/faster for most JWT/IAM/Lambda use cases; REST has more legacy features (API keys usage plans richer historically, WAF, request transformation, etc.).

---

### AppSync

- Managed GraphQL API with real-time subscriptions.
- Resolvers to DynamoDB, Lambda, HTTP, Aurora, etc.
- Choose when clients need GraphQL flexible queries / realtime.

---

## 7. Identity, Security & Encryption

### IAM (Identity and Access Management)

**Core**
- Users, Groups, Roles, Policies (JSON).
- **Least privilege**; prefer roles over long-lived access keys.
- Trust policy (who can assume) vs permissions policy (what they can do).
- Policy evaluation: explicit Deny wins.
- Service-linked roles; permission boundaries; SCPs (Org level) as outer guardrails.

**Developer patterns**
- Lambda execution role, EC2 instance profile, ECS task role.
- Temporary credentials via STS `AssumeRole`.
- Cross-account access via roles.

---

### Cognito

| Piece | Role |
|---|---|
| **User Pools** | Sign-up/sign-in, JWT tokens (Id/Access/Refresh) for app users |
| **Identity Pools** | Federate to AWS credentials (access S3/DynamoDB directly with temporary creds) |

- Social/SAML/OIDC federation.
- Common pattern: User Pool authenticates → Identity Pool issues AWS creds → access AWS resources.

---

### KMS & Secrets

| Service | Use |
|---|---|
| **KMS** | Create/control encryption keys; integrate with S3, EBS, DynamoDB, SQS, etc. |
| **Secrets Manager** | Rotate/store secrets (DB creds); automatic rotation Lambda |
| **SSM Parameter Store** | Config and secrets (Standard/Advanced); cheaper; rotation less feature-rich than Secrets Manager |
| **Certificate Manager (ACM)** | TLS certs for CloudFront/ALB |

**Exam tips**
- App needs rotating DB password → Secrets Manager.
- Simple config flag → Parameter Store.
- Encrypt SQS/SNS/S3 with customer-managed key → KMS CMK + key policy.

---

## 8. Monitoring, Logging & Tracing

### CloudWatch

- **Metrics**, **Alarms**, **Dashboards**, **Logs**, **Logs Insights**.
- Custom metrics from apps; embedded metric format.
- Alarms → SNS / Auto Scaling / EC2 actions.
- Log retention settings matter for cost.
- Synthetics, RUM (know exist for observability).

### CloudTrail

- Records **API calls** / account activity for auditing and compliance.
- Distinct from CloudWatch Logs (app/system logs) — CloudTrail is “who called which AWS API.”

### X-Ray

- Distributed tracing across Lambda, API Gateway, ECS, etc.
- Service map, latency segments, annotations/metadata for debugging microservices.
- Enable X-Ray SDK / Active Tracing on Lambda for exam scenarios about latency root-cause across services.

### CloudWatch vs X-Ray vs CloudTrail

| Need | Tool |
|---|---|
| Metrics/alarms/logs of runtime | CloudWatch |
| Audit who changed AWS resources / API history | CloudTrail |
| Trace a request across microservices | X-Ray |

---

## 9. CI/CD & Developer Tools

### Code* suite

| Service | Role |
|---|---|
| **CodeCommit** | Git repos (managed) |
| **CodeBuild** | Build/test in managed build containers; `buildspec.yml` |
| **CodeDeploy** | Deploy to EC2/on-prem/ECS/Lambda; appspec; deployment strategies (in-place, blue/green, canary/linear for Lambda) |
| **CodePipeline** | Orchestrate CI/CD stages (Source → Build → Deploy) |
| **CodeArtifact** | Artifact/npm/maven package repo |
| **Cloud9** | Cloud IDE (less emphasized now) |
| **CloudShell** | Browser shell with AWS CLI |

### SAM & CDK & CloudFormation

| Tool | Role |
|---|---|
| **CloudFormation** | IaC templates (JSON/YAML) |
| **SAM** | Serverless IaC + CLI (`sam build/deploy`); transforms to CloudFormation; great for Lambda apps |
| **CDK** | IaC in TypeScript/Python/etc.; synthesizes CloudFormation |
| **AppConfig** | Feature flags / dynamic config with validators and deploy strategies |

**CodeDeploy for Lambda**
- Traffic shifting: Canary, Linear, All-at-once.
- Hooks: pre-traffic / post-traffic for validation; automatic rollback on CloudWatch alarms.

---

## 10. Networking & Content Delivery (developer-relevant)

### VPC essentials for developers
- Public vs private subnets; Security Groups (stateful) vs NACLs (stateless).
- NAT Gateway for private subnet egress.
- VPC endpoints (Gateway for S3/DynamoDB; Interface for others) to keep traffic private.
- Lambda in VPC to reach private RDS — common pattern/trap (cold starts, ENI, subnet sizing).

### ELB
- **ALB**: HTTP/S Layer 7, path/host routing, Lambda targets.
- **NLB**: Layer 4 ultra-high performance / static IPs.
- **CLB**: legacy.

### CloudFront
- CDN in front of S3/ALB/custom origins.
- Edge caching, HTTPS (ACM), OAC/OAI for S3, signed URLs/cookies.
- Lower latency and offload origin.

### Route 53
- DNS + health checks + routing policies (simple, failover, weighted, latency, geolocation).

---

## 11. Quick Decision Cheatsheet

| Scenario | Prefer |
|---|---|
| Serverless key-value, huge scale | DynamoDB |
| Cache DynamoDB specifically | DAX |
| Cache for RDS / general sessions | ElastiCache (Redis/Valkey) |
| Relational SQL | RDS / Aurora |
| Variable relational load | Aurora Serverless |
| Objects, images, static files | S3 |
| Disk for one EC2 | EBS |
| Shared files across instances | EFS |
| Queue / decouple / buffer | SQS |
| Fan-out pub/sub | SNS |
| Event routing / schedule | EventBridge |
| Real-time streaming analytics | Kinesis |
| Migrate existing ActiveMQ/RabbitMQ | Amazon MQ |
| Short event compute | Lambda |
| Orchestrate many steps | Step Functions |
| HTTP API front door | API Gateway |
| User sign-in JWTs | Cognito User Pools |
| Temporary AWS creds for users | Cognito Identity Pools |
| Trace microservices | X-Ray |
| Audit API calls | CloudTrail |
| Rotate secrets | Secrets Manager |
| Serverless IaC for Lambda | SAM |
| Deploy Lambda with canary | CodeDeploy + Alias traffic shifting |

---

## Corrections applied to original notes

1. **DynamoDB table class switch**: can switch **once every 30 days** (not “twice”), with no downtime.
2. **io2 Block Express latency**: **sub-millisecond**, not &lt;500 ms.
3. **SQS FIFO**: designed for **exactly-once processing** (with deduplication), not merely “at-least-once without duplicates” wording.
4. **SNS notes**: completed (topics, fan-out, filtering, DLQ, CloudTrail, mobile/SMS).
5. **S3**: expanded from a stub into a full exam-oriented section.
6. **RDS Free Tier / credits**: Free Tier is primarily limited instance hours + storage; “$100 credits” are program-specific (e.g. promotions) — don’t memorize as core RDS pricing.
7. **Kinesis for DynamoDB**: ordering/dedup not guaranteed — app must handle (kept; clarified).
8. **DAX vs ElastiCache**: DAX is DynamoDB-specific; ElastiCache is general-purpose cache in front of many data stores.

---

## Suggested study order for DVA-C02

1. IAM + Lambda + API Gateway + DynamoDB  
2. S3 + SQS + SNS + EventBridge  
3. CloudWatch + X-Ray + CloudTrail  
4. Cognito + KMS + Secrets Manager  
5. CI/CD (CodePipeline/Build/Deploy) + SAM  
6. RDS/Aurora + ElastiCache/DAX  
7. Containers (ECS/Fargate) + Beanstalk  
8. Review decision cheatsheet and practice exams  

*Quotas, Free Tier amounts, and exact throughput numbers change — for the exam, prioritize patterns, tradeoffs, and integration behavior over memorizing every numeric limit.*
