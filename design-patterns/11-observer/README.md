# 11 — Observer

## Problem
When one object changes, many dependents must update — without tight coupling.

## Idea
**Subject** keeps a list of **observers** and notifies them on change (`update`).

```text
Subject ──notify──▶ Observer A
                 └─▶ Observer B
```

## When to use
- Event systems, UI bindings, pub/sub within a process
- One-to-many dependency

## Tradeoffs
- Notification order / cascade updates can surprise you
- Risk of memory leaks if observers aren’t removed
- Unexpected work in observers during notify

## Interview tip
Also called publish–subscribe (in-process). Java: `PropertyChangeListener`; reactive streams; GUI listeners. Differs from message-broker Pub/Sub (system design) but same idea.

## Run
```bash
javac Demo.java && java Demo
```
