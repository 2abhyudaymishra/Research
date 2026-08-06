# 05 — Adapter

## Problem
Existing class has a useful API, but your code expects a different interface.

## Idea
Wrap the adaptee in an **adapter** that implements the target interface and delegates.

```text
Client → Target interface ← Adapter → Adaptee (legacy/3rd party)
```

## When to use
- Integrate legacy / third-party APIs
- Reuse class without changing its source

## Tradeoffs
- Extra layer of indirection
- Don’t adapt everything — sometimes rewrite is clearer

## Interview tip
Java: `Arrays.asList` / `InputStreamReader` (byte stream → character stream) are adapter-like. Differs from Facade (Facade simplifies; Adapter **translates** interfaces).

## Run
```bash
javac Demo.java && java Demo
```
