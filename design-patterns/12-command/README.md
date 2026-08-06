# 12 — Command

## Problem
You want to queue, log, undo, or schedule operations — so the request must be an object.

## Idea
Encapsulate an action as a **command** with `execute()` (and optionally `undo()`). Invoker runs commands without knowing details.

## When to use
- Undo/redo
- Job queues / task scheduling
- Macro operations
- Decouple UI buttons from business logic

## Tradeoffs
- Many small command classes
- Undo requires storing enough state

## Interview tip
Runnable/Callable are command-like. GUI Action objects; transactional unit-of-work queues.

## Run
```bash
javac Demo.java && java Demo
```
