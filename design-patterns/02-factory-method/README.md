# 02 — Factory Method

## Problem
Client code should not hardcode which concrete class to `new`.

## Idea
Define a creator method; subclasses (or a simple factory) return the right product.

## When to use
- Object type depends on input/config
- You want to isolate creation logic

## Tradeoffs
- Extra classes/interfaces
- Don’t invent factories for every `new`

## Interview tip
“Factory Method” = polymorphic creation. A simple static factory is related but not the full GoF pattern.

## Run
```bash
javac Demo.java && java Demo
```
