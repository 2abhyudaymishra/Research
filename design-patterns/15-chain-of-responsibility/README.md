# 15 — Chain of Responsibility

## Problem
Multiple potential handlers; you don’t want the sender to know which one will process the request.

## Idea
Link handlers in a **chain**. Each handler either processes the request or passes it to the next.

```text
Request → Auth → RateLimit → Logger → BusinessHandler
```

## When to use
- Middleware / filters / validation pipelines
- Support ticket escalation levels
- Event processing with optional handlers

## Tradeoffs
- Request may fall off the end unhandled (must design carefully)
- Debugging the chain can be harder
- Performance if chains are long

## Interview tip
Servlet filters, Spring Security filter chain, logger levels, exception handling hierarchies (conceptually). Differs from Decorator (chain of handlers for a **request**, not stacking interfaces for one object’s API).

## Run
```bash
javac Demo.java && java Demo
```
