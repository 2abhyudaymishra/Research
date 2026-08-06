# Redis — Study Notes

Beginner-to-intermediate notes on **Redis** (and Redis-compatible engines like Valkey): what it is, data structures, persistence, clustering, patterns, and how it shows up in system design / AWS (ElastiCache).

---

## Table of Contents

1. [What is Redis?](#1-what-is-redis)
2. [When to use Redis](#2-when-to-use-redis)
3. [Core data types](#3-core-data-types)
4. [Essential commands](#4-essential-commands)
5. [Keys, TTL, and eviction](#5-keys-ttl-and-eviction)
6. [Persistence](#6-persistence)
7. [Replication & high availability](#7-replication--high-availability)
8. [Clustering & scaling](#8-clustering--scaling)
9. [Common application patterns](#9-common-application-patterns)
10. [Transactions, pipelines, Lua](#10-transactions-pipelines-lua)
11. [Pub/Sub & Streams](#11-pubsub--streams)
12. [Security & ops basics](#12-security--ops-basics)
13. [Redis vs Memcached vs DB](#13-redis-vs-memcached-vs-db)
14. [AWS: ElastiCache for Redis / Valkey](#14-aws-elasticache-for-redis--valkey)
15. [Quick cheatsheet](#15-quick-cheatsheet)

---

## 1. What is Redis?

**Redis** = **Re**mote **Di**ctionary **S**erver

- In-memory data store used as:
  - Cache
  - Session store
  - Message broker (Pub/Sub, Streams)
  - Fast primary store for selected workloads
- Single-threaded command execution model (very fast for in-memory ops; I/O can use threads in modern versions)
- Rich **data structures** (not just string key/value)
- Optional **durability** via RDB/AOF
- Typical latency: **sub-millisecond**

**Mental model:** a giant, networked, typed HashMap with TTLs, replication, and useful server-side operations.

> Note: **Valkey** is a Linux Foundation Redis-compatible fork. Concepts below apply to both for most beginner purposes. AWS ElastiCache supports Redis OSS and Valkey.

---

## 2. When to use Redis

| Great fit | Weak / wrong fit |
|---|---|
| Cache DB query results | Large blob storage (use S3) |
| Session / shopping cart | Complex relational queries/joins |
| Rate limiting counters | Source of truth you can’t afford to lose (unless carefully persisted + backed up) |
| Leaderboards (sorted sets) | Huge datasets that don’t fit memory (cost!) |
| Distributed locks (with care) | Analytics warehouse |
| Pub/Sub realtime fan-out | Guaranteed durable queue (prefer Kafka/SQS for many cases) |
| Idempotency keys / short-lived tokens | Primary store without HA/persistence plan |

**Rule:** if data must survive and is large/relational, use a real database; put Redis **in front** as cache/accelerator.

---

## 3. Core data types

| Type | What it holds | Typical use |
|---|---|---|
| **String** | Bytes / text / ints / serialized JSON | Cache values, counters (`INCR`) |
| **Hash** | Field → value map | User profile object (`user:1`) |
| **List** | Ordered list (linked) | Queues, recent activity |
| **Set** | Unique unordered members | Tags, unique visitors |
| **Sorted Set (ZSet)** | Unique members + score | Leaderboards, time indexes |
| **Stream** | Append-only log of entries | Event history, consumer groups |
| **Bitmap** | Bit ops on string | Daily active flags |
| **HyperLogLog** | Probabilistic cardinality | Approx unique counts |
| **Geo** | Geo coordinates on ZSet | Nearby drivers/stores |

### Choosing quickly
- One blob value → **String**
- Object with fields you update independently → **Hash**
- Rank by score/time → **Sorted Set**
- Unique collection → **Set**
- FIFO-ish work queue (simple) → **List** or **Stream**
- Fan-out notify → **Pub/Sub** (ephemeral) or **Stream** (durable-ish)

---

## 4. Essential commands

### Strings
```text
SET key value
GET key
SET key value EX 60          # expire in 60s
SET key value NX             # set only if not exists (lock patterns)
INCR pageviews:home
MGET k1 k2 k3
```

### Hashes
```text
HSET user:1 name Alice age 30
HGET user:1 name
HGETALL user:1
HINCRBY user:1 age 1
```

### Lists
```text
LPUSH queue job1          # push left
RPOP queue                # pop right  → simple queue
LRANGE queue 0 9          # peek
```

### Sets
```text
SADD tags:post1 redis cache
SISMEMBER tags:post1 redis
SINTER tags:post1 tags:post2
```

### Sorted sets
```text
ZADD leaderboard 100 alice 250 bob
ZINCRBY leaderboard 10 alice
ZREVRANGE leaderboard 0 9 WITHSCORES   # top 10
ZRANK leaderboard alice
```

### Keys / TTL
```text
EXISTS key
DEL key
EXPIRE key 120
TTL key
KEYS pattern          # dangerous in prod (blocks); prefer SCAN
SCAN cursor MATCH user:* COUNT 100
```

### Admin-ish
```text
PING
INFO
DBSIZE
FLUSHDB               # wipe current DB — careful
```

---

## 5. Keys, TTL, and eviction

### Key design
- Use clear namespaces: `app:env:entity:id` → `shop:prod:user:42:session`
- Keep keys short but readable
- Avoid huge key cardinality explosions without TTLs

### TTL (Time To Live)
- Absolute expiry on keys (`EXPIRE`, `SET EX`)
- Essential for caches, sessions, rate-limit windows, idempotency keys

### Eviction (when `maxmemory` hit)
Policy examples:

| Policy | Behavior |
|---|---|
| `noeviction` | Writes error when full |
| `allkeys-lru` | Evict least recently used (common for cache) |
| `volatile-lru` | LRU among keys that have TTL |
| `allkeys-lfu` | Evict least frequently used |
| `volatile-ttl` | Evict soonest-to-expire first |

**Cache tip:** for pure cache workloads, `allkeys-lru` or `allkeys-lfu` is common.

### Cache stampede
Many clients miss the same key at once → stampede to DB.  
Mitigations: lock/single-flight, probabilistic early expire, slightly staggered TTLs.

---

## 6. Persistence

Redis is in-memory first; persistence is optional/configurable.

| Mechanism | Idea | Tradeoff |
|---|---|---|
| **RDB** | Point-in-time snapshots | Compact; can lose data since last snapshot |
| **AOF** | Append every write to log | More durable; larger; rewrite compaction |
| **RDB + AOF** | Common production combo | Balance recovery speed + durability |

**Durability ≠ database ACID disk DB.** Even with AOF every second you can lose ~1s of data on crash (config-dependent).

**Ephemeral cache pattern:** disable heavy persistence if Redis is only a cache and DB is source of truth (faster, simpler recovery = warm from DB).

---

## 7. Replication & high availability

### Replica (read replica)
```text
Client writes → Primary
                  ↓ async replication
                Replica(s)  ← clients may read
```

- Replicas help **read scaling** and **failover**
- Async by default → possible lag / stale reads

### Redis Sentinel
- Monitors primary/replicas
- Handles **automatic failover** and config discovery
- Clients should be Sentinel-aware

### Managed HA
On AWS ElastiCache: Multi-AZ / replication groups with automatic failover (you don’t run Sentinel yourself).

---

## 8. Clustering & scaling

### Vertical scale
Bigger node = more memory/CPU. Simple, but limited.

### Redis Cluster (sharding)
- Data split across hash slots (**16384** slots)
- Each key maps to a slot → a node
- Horizontal scale for memory & throughput
- Multi-key ops require keys in the **same slot** (use hash tags `{user:1}:profile`)

### Scaling mental model
| Need | Approach |
|---|---|
| More reads | Add replicas |
| More memory / write throughput | Cluster / shard |
| Spiky traffic without ops | AWS ElastiCache Serverless |

---

## 9. Common application patterns

### 1) Cache-aside (lazy loading)
```text
read key in Redis
  hit  → return
  miss → read DB → SET in Redis with TTL → return
```
Most common app-level cache pattern.

### 2) Session store
```text
SET session:<id> <json> EX 1800
```
Sticky LB not required if all app nodes share Redis.

### 3) Rate limiting (counter + TTL)
```text
INCR ratelimit:user:42:minute
EXPIRE ratelimit:user:42:minute 60   # set expire on first hit
```
Or token-bucket / sliding window with Lua for atomicity.

### 4) Distributed lock (simplified)
```text
SET lock:order:99 <token> NX EX 30
... do work ...
# delete only if token matches (Lua) to avoid deleting someone else's lock
```
Prefer battle-tested libraries (Redlock debates exist — know risks).

### 5) Leaderboard
Sorted sets: `ZINCRBY` + `ZREVRANGE`.

### 6) Idempotency keys
```text
SET idem:<key> <response> NX EX 86400
```
If `NX` fails, return stored response.

### 7) Write-through / write-behind
- Write-through: write Redis + DB together
- Write-behind: write Redis, async flush DB (faster writes, more complexity/risk)

---

## 10. Transactions, pipelines, Lua

### MULTI / EXEC
Queues commands; executes sequentially.  
Not full relational rollback isolation — watch for races with `WATCH`.

### Pipelines
Send many commands without waiting for each reply → less RTT. Huge performance win.

### Lua scripts (`EVAL`)
Run custom atomic logic server-side (rate limit check+incr, compare-and-delete lock).

**When:** need atomic read-modify-write beyond one command.

---

## 11. Pub/Sub & Streams

### Pub/Sub
```text
SUBSCRIBE channel
PUBLISH channel message
```
- Fire-and-forget fan-out
- If subscriber is offline, message is **missed**
- Great for live notifications; not a durable queue

### Streams
- Append-only log with IDs
- **Consumer groups** (like a lightweight Kafka-ish pattern)
- Can acknowledge processing (`XACK`)
- Better when you need history / competing consumers

| Need | Prefer |
|---|---|
| Live push, loss OK | Pub/Sub |
| Durable-ish processing | Streams (or SQS/Kafka) |
| Massive multi-consumer log | Kafka often better |

---

## 12. Security & ops basics

### Security
- Never expose Redis to the public internet
- Require auth (ACL users in modern Redis)
- TLS in transit when supported
- Restrict by security groups / private subnets
- Disable dangerous commands in shared envs (`FLUSHALL`, `KEYS`, `CONFIG`)

### Memory ops
- Monitor `used_memory`, eviction rate, hit ratio, CPU, connections, replication lag
- Set `maxmemory` + eviction policy intentionally
- Beware big keys (huge lists/hashes) — hard to replicate/migrate; use `MEMORY USAGE`, scan carefully

### Hot keys
One key getting huge traffic can bottleneck a shard. Mitigate with local cache, key splitting, or read replicas.

---

## 13. Redis vs Memcached vs DB

| | Redis | Memcached | DB (Postgres/MySQL/DynamoDB) |
|---|---|---|---|
| Structures | Rich | Simple KV | Rich queries / durable |
| Persistence | Optional | No | Yes |
| Replication | Yes | Limited model | Yes |
| Use | Cache + many patterns | Simple cache | Source of truth |
| Latency | μs–ms | μs–ms | ms+ typically |

**DynamoDB DAX** = DynamoDB-specific cache (API-compatible).  
**Redis** = general-purpose cache/data structures for many backends.

---

## 14. AWS: ElastiCache for Redis / Valkey

Managed Redis-compatible caching on AWS.

### Options
| Option | Notes |
|---|---|
| **ElastiCache Serverless** | Least ops; scales; pay for storage + ECPU |
| **Node-based cluster** | More control; choose node types; Multi-AZ |
| **Valkey / Redis OSS engine** | Redis-compatible choices on ElastiCache |

### Features to know
- In-memory, microsecond latency
- Multi-AZ replication / failover
- Read replicas
- Backup/restore (snapshots to S3 in-region)
- Manual snapshots can be retained; automatic follow retention policy (up to ~35 days style retention windows — confirm current docs)
- CloudWatch metrics (CPU, memory, evictions, connections)
- Use as front-end cache for RDS/Aurora/DynamoDB/DocumentDB-style apps

### Billing (conceptual)
- Serverless: data stored + ECPU
- Node-based: node hours (+ Reserved Instances discount option)
- Cross-AZ / transfer nuances may apply

### App connectivity
- Place app in VPC; connect to cluster endpoint
- Don’t hardcode a single node if cluster mode expects topology discovery
- Use connection pooling; Redis connections are not unlimited

---

## 15. Quick cheatsheet

| Goal | Redis approach |
|---|---|
| Cache DB reads | String/Hash + TTL + cache-aside |
| Sessions | String/Hash + TTL; shared by all app nodes |
| Top-N leaderboard | Sorted Set |
| Unique tags | Set |
| Simple queue | List `LPUSH`/`RPOP` or Streams |
| Rate limit | `INCR` + `EXPIRE` or Lua token bucket |
| Idempotency | `SET key NX EX` |
| Fan-out notify | Pub/Sub |
| Durable event processing | Streams (+ consumer groups) or external queue |
| More reads | Replicas |
| More capacity | Cluster / larger nodes / Serverless |
| Avoid stampedes | Single-flight + TTL jitter |
| Prod safety | Private network, auth, TLS, maxmemory policy |

### Interview one-liners
- Redis is an **in-memory** data structure server  
- Use **TTL + eviction** for caches  
- **Sorted sets** power leaderboards  
- **Replicas** scale reads; **cluster** shards data  
- Pub/Sub is **ephemeral**; Streams keep a log  
- On AWS, run it managed with **ElastiCache**  

---

## Suggested practice order

1. Run Redis locally (Docker) and practice `SET/GET`, hashes, zsets  
2. Build a tiny cache-aside layer in front of a DB  
3. Add TTL + measure hit ratio  
4. Implement a rate limiter and a leaderboard  
5. Turn on a replica and observe lag  
6. Try ElastiCache (or Serverless) in a private subnet from an app  

*Commands and managed-service knobs evolve — focus on data structures, TTLs, HA/scaling choices, and when Redis is the wrong tool.*
