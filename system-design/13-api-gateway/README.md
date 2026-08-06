# 13 — API Gateway

## Problem
Clients shouldn’t know every microservice URL, auth scheme, and version. Cross-cutting concerns repeat everywhere.

## Idea
A single **API Gateway** is the front door:

```text
Mobile/Web → API Gateway → Auth / Rate limit / Route
                              ├→ Users service
                              ├→ Orders service
                              └→ Payments service
```

## Typical responsibilities
- Routing & path rewriting
- Authentication / authorization
- Rate limiting & API keys
- Request validation
- TLS termination
- Response aggregation (sometimes)
- Canary / stage deployment

## Gateway vs Load Balancer vs Reverse Proxy
| | Focus |
|---|---|
| Reverse proxy | Generic HTTP middleman |
| Load balancer | Distribute to healthy instances |
| API Gateway | API product features (auth, quotas, transforms) |

## Tradeoffs
- Central place for policy (good) can become a bottleneck (bad)
- Extra hop / complexity
- Beware “god gateway” doing too much business logic

## In the real world
Amazon API Gateway, Kong, Apigee, Nginx + Lua, Envoy + filters.

## Run
```bash
python3 demo.py
```
