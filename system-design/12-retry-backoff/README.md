# 12 — Retry with Exponential Backoff

## Problem
Transient failures (network blips, 503s, throttling) are common. Immediate endless retries make outages worse.

## Idea
Retry a few times with **increasing delays**, often with **jitter** (randomness) so clients don’t retry in lockstep.

```text
fail → wait 0.1s → fail → wait 0.2s → fail → wait 0.4s → give up
```

## Best practices
- Retry only **idempotent** or safely replayable operations
- Cap max attempts and max delay
- Add jitter
- Combine with circuit breaker / timeouts
- Honor `Retry-After` when present

## Tradeoffs
- Improves resilience for transient errors
- Can amplify load during outages if misconfigured
- Increases end-to-end latency

## In the real world
AWS SDKs, HTTP clients, Step Functions retry policies, gRPC retry.

## Run
```bash
javac Demo.java && java Demo
```
