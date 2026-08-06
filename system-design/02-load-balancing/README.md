# 02 — Load Balancing

## Problem
One server cannot handle all traffic. If it dies, the whole app is down.

## Idea
Put a **load balancer** in front of many identical servers and distribute requests.

```text
                ┌→ Server A
Client → LB ────┼→ Server B
                └→ Server C
```

## Common algorithms
| Algorithm | Behavior |
|---|---|
| Round robin | A → B → C → A … |
| Weighted round robin | Stronger servers get more traffic |
| Least connections | Prefer the least busy server |
| IP hash | Same client IP → same server (sticky-ish) |

## Why it matters
- Horizontal scaling (add servers)
- High availability (remove unhealthy servers)

## Tradeoffs
- LB itself must be highly available
- Sticky sessions complicate scaling (prefer stateless servers + shared session store)

## In the real world
AWS ALB/NLB, Nginx, HAProxy, Cloudflare, Envoy.

## Run
```bash
javac Demo.java && java Demo
```
