# 05 — Message Queue

## Problem
If Service A calls Service B directly, spikes and failures in B hurt A. They are tightly coupled.

## Idea
A puts a **message** on a queue; B pulls and processes later.

```text
Producer → [ Queue ] → Consumer
```

## Why it matters
- Decouple services
- Buffer traffic spikes
- Retry failed work
- Scale consumers independently

## Key concepts
| Concept | Meaning |
|---|---|
| At-least-once | Message may be delivered more than once |
| Exactly-once | Harder; often “effectively once” with idempotency |
| Visibility timeout | Hide in-flight message while processing |
| DLQ | Dead-letter queue for poison messages |
| FIFO vs standard | Ordering + dedupe vs max throughput |

## Tradeoffs
- Eventual processing (not instant sync response)
- Need idempotent consumers
- Operational complexity (backlog monitoring)

## In the real world
SQS, RabbitMQ, Kafka (log), ActiveMQ, Redis streams.

## Run
```bash
python3 demo.py
```
