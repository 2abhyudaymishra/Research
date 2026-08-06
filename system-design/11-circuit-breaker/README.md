# 11 — Circuit Breaker

## Problem
If a dependency is down, endless retries/timeouts waste threads and make *your* service fail too (cascading failure).

## Idea
Wrap calls in a **circuit breaker**:

```text
CLOSED  --many failures-->  OPEN (fail fast)
  ^                           |
  |                         half-open probe
  +------- success -----------+
```

| State | Behavior |
|---|---|
| Closed | Calls flow normally |
| Open | Immediately fail; don’t call dependency |
| Half-open | Allow a trial request |

## Why it matters
Fail fast, give the dependency time to recover, protect your resources.

## Tradeoffs
- Needs good thresholds (error %, time window)
- Half-open storms if many instances probe at once
- Fallback responses may be stale/degraded

## In the real world
Resilience4j, Hystrix (legacy), Envoy outliers, AWS Step Functions retries + catch, service meshes.

## Run
```bash
python3 demo.py
```
