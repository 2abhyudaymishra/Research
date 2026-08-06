# 14 — State

## Problem
An object behaves differently based on its **state** (and transitions between states) — big `switch` on status is messy.

## Idea
Extract each state into a class; the context delegates behavior to the current state object; transitions replace the state.

## When to use
- Order/lifecycle workflows (NEW → PAID → SHIPPED)
- Connections, media players, TCP-like protocols

## Tradeoffs
- More classes
- Related to Strategy (both delegate) but State is about **transitions** and identity of “what phase am I in?”

## Interview tip
If interviewer asks Strategy vs State: Strategy is chosen by client; State often changes itself via transitions.

## Run
```bash
javac Demo.java && java Demo
```
