# 07 — Facade

## Problem
Client must call many classes in a complex subsystem in the right order.

## Idea
Provide one **simple facade** method that hides the subsystem choreography.

## When to use
- Simplify a messy API for most callers
- Define a clear entry point for a library/module

## Tradeoffs
- Facade can become a “god” object if it does too much
- Doesn’t replace understanding the subsystem when you need power features

## Interview tip
Facade **simplifies**; Adapter **converts** an interface. Example: a `CheckoutService` that coordinates inventory, payment, email.

## Run
```bash
javac Demo.java && java Demo
```
