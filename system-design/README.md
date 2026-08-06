# System Design Patterns — Beginner Guide

Hands-on introduction to **must-know system design patterns**.  
Each folder has:

- `README.md` — what the pattern is, why it exists, when to use it
- `demo.py` — small runnable Python example (stdlib only)

```bash
cd system-design/02-load-balancing
python3 demo.py
```

## Patterns

| # | Folder | Pattern | One-line idea |
|---|---|---|---|
| 01 | [client-server](01-client-server) | Client–Server | Clients request; servers respond |
| 02 | [load-balancing](02-load-balancing) | Load Balancing | Spread traffic across many servers |
| 03 | [caching](03-caching) | Caching | Store hot data closer / in memory |
| 04 | [proxy](04-proxy) | Proxy / Reverse Proxy | Middleman that forwards requests |
| 05 | [message-queue](05-message-queue) | Message Queue | Decouple producers and consumers |
| 06 | [pub-sub](06-pub-sub) | Pub/Sub | One publish → many subscribers |
| 07 | [database-replication](07-database-replication) | Replication | Copies of data for HA & reads |
| 08 | [database-sharding](08-database-sharding) | Sharding | Split data across many DBs |
| 09 | [consistent-hashing](09-consistent-hashing) | Consistent Hashing | Stable key→node mapping when nodes change |
| 10 | [rate-limiting](10-rate-limiting) | Rate Limiting | Cap how fast clients can call you |
| 11 | [circuit-breaker](11-circuit-breaker) | Circuit Breaker | Stop calling a failing dependency |
| 12 | [retry-backoff](12-retry-backoff) | Retry + Backoff | Retry failures with growing delays |
| 13 | [api-gateway](13-api-gateway) | API Gateway | Single entry for many backend services |
| 14 | [cqrs](14-cqrs) | CQRS | Separate write model from read model |
| 15 | [idempotency](15-idempotency) | Idempotency | Same request twice → same effect once |
| 16 | [bloom-filter](16-bloom-filter) | Bloom Filter | Fast “probably exists?” set check |

## How to study

1. Read the README (problem → idea → tradeoffs).
2. Run `python3 demo.py`.
3. Change one parameter (servers, cache TTL, shard count) and re-run.
4. Ask: *Where would Netflix / Uber / AWS use this?*

## Mental model

```text
Client → API Gateway → Load Balancer → Services
                              ↓
                    Cache / Queue / DB (replicated + sharded)
```

Most real systems combine several of these patterns.

## Requirements

- Python 3.9+ recommended
- No third-party packages
