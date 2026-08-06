# 10 — Rate Limiting

## Problem
Clients (or attackers) can overwhelm your API. You need fair usage and protection.

## Idea
Allow only **N requests per time window** per key (user/IP/token).

```text
Request → Rate Limiter → allow / 429 Too Many Requests
```

## Algorithms
| Algorithm | Idea |
|---|---|
| Fixed window | Count per minute; resets on boundary |
| Sliding window | Smoother than fixed window |
| Token bucket | Tokens refill over time; allows short bursts |
| Leaky bucket | Steady outflow rate |

## Why it matters
- Protect databases and downstreams
- Enforce API plans (free vs paid)
- Mitigate abuse

## Tradeoffs
- False rejects under clock skew / multi-node without shared store
- Need distributed counters (Redis) at scale
- UX impact when limits are too strict

## In the real world
API Gateway usage plans, Nginx limit_req, Cloudflare, Redis + token bucket.

## Run
```bash
javac Demo.java && java Demo
```
