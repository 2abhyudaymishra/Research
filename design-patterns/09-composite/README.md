# 09 — Composite

## Problem
Tree structures (files/folders, UI widgets, org charts) — clients shouldn’t care if a node is leaf or group.

## Idea
Both leaf and composite implement the **same component interface**. Composite holds children and forwards operations.

## When to use
- Part–whole hierarchies
- Uniform treatment of individual and group

## Tradeoffs
- Design of the shared interface can be awkward (ops that only make sense on leaves/composites)

## Interview tip
File systems: `File` and `Directory` both are `Node` with `size()`. UI view trees.

## Run
```bash
javac Demo.java && java Demo
```
