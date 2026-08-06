# 10 — Strategy

## Problem
Many related algorithms (sort, pay, compress) — avoid giant `if/else` or subclass explosion.

## Idea
Put each algorithm in a **strategy** class with a shared interface; context delegates to the current strategy.

## When to use
- Interchangeable behaviors selected at runtime
- Open/closed for new algorithms

## Tradeoffs
- More classes; clients must know which strategy to pick (or use a factory)

## Interview tip
Java: `Comparator` for sort; Spring injecting different payment implementations. Often paired with Factory.

## Run
```bash
javac Demo.java && java Demo
```
