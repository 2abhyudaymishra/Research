# 16 — Bloom Filter

## Problem
You want a **fast, memory-cheap** check: “Have I seen this key before?” for huge sets (URLs, users, cache existence).

## Idea
A **Bloom filter** is a bit array + multiple hashes:

- **Maybe yes** (false positives possible)
- **Definitely no** (no false negatives, in the basic version)

```text
add("alice")  → set several bits
might_contain("alice") → True
might_contain("bob")   → False or rarely True (false positive)
```

## Why it matters
- Cheap negative checks before hitting DB/disk
- CDN / cache / DB existence probes
- Spell-check dictionaries, malicious URL lists

## Tradeoffs
| Pros | Cons |
|---|---|
| Tiny memory | False positives |
| O(k) fast | Hard to delete (unless counting Bloom) |
| Great “definitely not” | Not a source of truth |

## In the real world
Cassandra/Bigtable SSTable filters, browsers’ safe-browsing lists, Redis Bloom (module), CDN caches.

## Run
```bash
javac Demo.java && java Demo
```
