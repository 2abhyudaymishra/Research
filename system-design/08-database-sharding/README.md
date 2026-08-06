# 08 — Database Sharding

## Problem
One database server cannot hold or serve all data/traffic.

## Idea
**Shard** (partition) data across multiple databases by a shard key.

```text
userId % 3
  0 → Shard A
  1 → Shard B
  2 → Shard C
```

## Shard key tips
- High cardinality (many distinct values)
- Even distribution (avoid hot shards)
- Align with query patterns (don’t force cross-shard joins)

## Strategies
| Strategy | Notes |
|---|---|
| Range | A–M / N–Z — risk of hot ranges |
| Hash | `hash(key) % N` — even, but resharding hurts |
| Directory | Lookup table maps key → shard |
| Geo | By region |

## Tradeoffs
- Enables massive scale
- Cross-shard queries/transactions are hard
- Resharding is operationally painful (see consistent hashing)

## In the real world
DynamoDB partitions, MongoDB sharding, Vitess, Citus, manual app-level shards.

## Run
```bash
python3 demo.py
```
