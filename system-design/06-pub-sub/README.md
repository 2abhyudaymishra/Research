# 06 — Pub/Sub (Publish–Subscribe)

## Problem
One event must notify many interested systems (email, analytics, search index) without the publisher knowing each consumer.

## Idea
Publisher sends to a **topic**. Subscribers each get a copy.

```text
Publisher → Topic ─┬→ Subscriber A
                   ├→ Subscriber B
                   └→ Subscriber C
```

## Queue vs Pub/Sub
| | Message Queue | Pub/Sub |
|---|---|---|
| Consumers | Competing (one wins) | Fan-out (all get it) |
| Goal | Work distribution | Event broadcast |
| Example | “process this order” | “order placed” |

Often combined: **SNS → multiple SQS queues** (fan-out + durable processing).

## Tradeoffs
- Loose coupling is great
- Harder to reason about end-to-end flow
- Delivery guarantees vary (at-least-once common)

## In the real world
SNS, Redis Pub/Sub, Kafka topics, Google Pub/Sub, EventBridge.

## Run
```bash
python3 demo.py
```
