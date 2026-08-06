# 14 — CQRS (Command Query Responsibility Segregation)

## Problem
One model optimized for writes is often a poor fit for complex reads (dashboards, search, listings).

## Idea
Split:

- **Commands** — change state (writes)
- **Queries** — read state (often from a different store/model)

```text
Client --command--> Write Model (DB)
                         │ async update
                         ▼
Client <--query----- Read Model (cache / view DB)
```

## Why it matters
- Scale reads and writes independently
- Tailor read schemas for UI needs
- Pairs well with event-driven systems

## Tradeoffs
- Eventual consistency between write and read sides
- More moving parts
- Overkill for simple CRUD apps

## Related
Event sourcing often feeds CQRS read models, but they are separate ideas.

## In the real world
Orders service writes to SQL; search/read API served from Elasticsearch/Redis projections.

## Run
```bash
python3 demo.py
```
