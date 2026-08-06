# 01 — Singleton

## Problem
You need **exactly one** shared instance (config, connection pool handle, logger facade).

## Idea
Make the constructor private; expose a static `getInstance()`.

## When to use
- Truly global, shared state that must be unique
- Expensive-to-create shared resource

## Tradeoffs
- Hidden global state → harder to test
- Overused; prefer DI when “one instance” is enough
- Thread safety matters (see enum / holder approaches)

## Interview tip
Prefer **enum singleton** or **initialization-on-demand holder** in Java; mention double-checked locking only if asked (and get `volatile` right).

## Run
```bash
javac Demo.java && java Demo
```
