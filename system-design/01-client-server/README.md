# 01 — Client–Server

## Problem
How do many users talk to one application without each user running the full system?

## Idea
Split roles:

- **Client** — UI / app that sends requests
- **Server** — holds business logic and data; responds

```text
Client  ----request---->  Server
Client  <---response----  Server
```

## Why it matters
Almost every web and mobile app is client–server. Understanding request/response, latency, and server capacity starts here.

## Variants
| Style | Notes |
|---|---|
| Thin client | Browser / mobile app; logic on server |
| Thick client | More logic on device; still talks to APIs |
| Multi-tier | Client → App server → Database |

## Tradeoffs
- Simple to reason about
- Server can become a bottleneck → need load balancing, caching, scaling

## Run
```bash
python3 demo.py
```
