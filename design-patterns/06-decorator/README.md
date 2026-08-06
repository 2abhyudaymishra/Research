# 06 — Decorator

## Problem
Add responsibilities (logging, compression, extras) **without** exploding subclasses.

## Idea
Wrap an object with another object of the **same interface**; delegate and add behavior.

```text
Client → DecoratorB → DecoratorA → Core
```

## When to use
- Optional features stacked dynamically
- Open/closed: extend without modifying original

## Tradeoffs
- Many small classes; debugging wrapper stacks can be hard

## Interview tip
Java I/O: `new BufferedInputStream(new FileInputStream(...))`. Differs from Inheritance (runtime composition) and from Proxy (Proxy controls access; Decorator **adds** behavior).

## Run
```bash
javac Demo.java && java Demo
```
