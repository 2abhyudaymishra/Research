# 04 — Builder

## Problem
Constructor with many optional params is messy (`new Foo(a,b,null,null,true,…)`).

## Idea
Build step-by-step with a fluent API; `build()` returns the immutable (or finished) object.

## When to use
- Many optional fields
- Want readable, safe construction
- Same construction process, different representations

## Tradeoffs
- Extra boilerplate (less with records/Lombok in real projects)

## Interview tip
Java examples: `StringBuilder`, `LokHttpClient.Builder`, Lombok `@Builder`. Distinct from Factory (Builder focuses on **piecing together** one complex object).

## Run
```bash
javac Demo.java && java Demo
```
