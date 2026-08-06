# 08 — Proxy

## Problem
You need a stand-in that controls access: lazy load, cache, auth, logging, remote call.

## Idea
Proxy implements the **same interface** as the real subject and delegates when appropriate.

## Types (interview)
| Type | Role |
|---|---|
| Virtual | Lazy creation/loading |
| Protection | Access control |
| Remote | Local stub for remote object |
| Cache / logging | Cross-cutting around the real call |

## When to use
- Expensive object created only when needed
- Enforce permissions before calling real object

## Tradeoffs
- Extra hop; latency if overused
- Easy to confuse with Decorator (Proxy ≠ adding features for the client’s sake; it’s about **access control / lifecycle**)

## Interview tip
Spring AOP / `@Transactional` proxies, Hibernate lazy-loading proxies.

## Run
```bash
javac Demo.java && java Demo
```
