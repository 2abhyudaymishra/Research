# 09 — Consistent Hashing

## Problem
With `hash(key) % N`, adding/removing a shard moves **almost all keys**. Painful rebalancing.

## Idea
Map both keys and nodes onto a ring. A key belongs to the first node clockwise.

When a node joins/leaves, **only nearby keys move**.

```text
        N1
      /    \
   Kx        N2
      \    /
        N3
```

## Why it matters
- Caches (Memcached clusters)
- Distributed databases / DHT
- Load balancing sticky routing

## Virtual nodes
Give each physical node many positions on the ring → smoother load distribution.

## Tradeoffs
- More complex than modulo hashing
- Still need replication for HA
- Hot keys can still overload one node

## In the real world
Dynamo/Cassandra-style rings, CDN/cache clusters, some load balancers.

## Run
```bash
python3 demo.py
```
