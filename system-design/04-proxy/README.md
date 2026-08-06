# 04 — Proxy / Reverse Proxy

## Problem
Clients should not always talk to backend servers directly (security, TLS, routing, logging).

## Idea
A **proxy** sits in the middle and forwards traffic.

| Type | Who it represents | Example |
|---|---|---|
| Forward proxy | Client | Corporate proxy, VPN-like egress |
| **Reverse proxy** | Server | Nginx in front of app servers |

```text
Client → Reverse Proxy → App Server(s)
           (TLS, routing,
            compression, auth)
```

## Why reverse proxies matter
- Terminate HTTPS
- Route `/api` vs `/static`
- Hide internal IPs
- Add caching / rate limits / WAF

## Tradeoffs
- Extra hop (usually tiny)
- Proxy misconfig can take everything down
- Must scale/HA the proxy tier too

## In the real world
Nginx, Envoy, HAProxy, AWS ALB, API Gateway (gateway ≈ smart reverse proxy).

## Run
```bash
javac Demo.java && java Demo
```
