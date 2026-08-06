# 03 — Caching

## Problem
Databases are slower and more expensive than memory. Repeated reads of the same data waste time.

## Idea
Keep a fast copy of hot data in a **cache** (memory, CDN edge, local process).

```text
Client → App → Cache  (hit)  → return
              ↘ Cache miss → DB → fill cache → return
```

## Common strategies
| Strategy | Meaning |
|---|---|
| Cache-aside | App reads cache; on miss, load DB and populate cache |
| Read-through | Cache library loads DB on miss |
| Write-through | Write to cache and DB together |
| Write-behind | Write cache first; async flush to DB |
| TTL | Expire entries after time-to-live |

## Why it matters
Huge latency and cost wins for read-heavy workloads (feeds, product pages, sessions).

## Tradeoffs
- **Stale data** if invalidation is wrong
- Cache stampede when many clients miss at once
- Extra complexity (eviction, TTLs, consistency)

## In the real world
Redis, Memcached, ElastiCache, CDN caches, HTTP Cache-Control.

## Run
```bash
python3 demo.py
```
