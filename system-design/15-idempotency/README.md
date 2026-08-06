# 15 — Idempotency

## Problem
Networks retry. Clients double-click. Queues deliver at-least-once. Without care, you charge a card **twice**.

## Idea
An operation is **idempotent** if doing it N times has the same effect as doing it once.

```text
Request + Idempotency-Key: abc
  → first time: charge $10, store result under abc
  → retry abc: return stored result (no second charge)
```

## Techniques
| Technique | Example |
|---|---|
| Idempotency keys | Payments, form posts |
| Natural unique keys | `INSERT ... ON CONFLICT` / upsert by order_id |
| Deduplicate consumers | Track processed message IDs |
| PUT vs POST | PUT `/resource/id` often idempotent by design |

## Why it matters
Required for reliable distributed systems with retries and queues.

## Tradeoffs
- Need a store for keys + responses (TTL)
- Key design matters (per-user uniqueness)
- Not every operation can be made perfectly idempotent without care

## In the real world
Stripe Idempotency-Key, SQS + dedupe, DynamoDB conditional writes.

## Run
```bash
javac Demo.java && java Demo
```
