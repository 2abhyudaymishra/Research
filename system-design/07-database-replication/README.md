# 07 — Database Replication

## Problem
One database is a single point of failure, and it may not handle all read traffic.

## Idea
Keep **replicas** (copies) of the data.

```text
          writes
Client ──────────→ Primary
                      │ replication
                      ▼
Client ←── reads ── Replica(s)
```

## Common topologies
| Mode | Behavior |
|---|---|
| Leader–follower (primary–replica) | Writes to primary; replicas copy |
| Multi-leader | Writes in multiple regions (conflict risk) |
| Leaderless | Quorum R/W (e.g. Dynamo-style) |

## Why it matters
- **High availability** (failover)
- **Read scaling** (read replicas)
- Cross-region latency / DR

## Tradeoffs
- **Replication lag** → stale reads
- Failover complexity
- More storage / cost

## In the real world
RDS Multi-AZ + Read Replicas, Aurora Replicas, MongoDB replica sets, MySQL binlog.

## Run
```bash
python3 demo.py
```
