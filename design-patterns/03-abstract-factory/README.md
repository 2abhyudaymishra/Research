# 03 — Abstract Factory

## Problem
You need to create a **family** of related objects (e.g. Dark UI button + checkbox) without mixing families.

## Idea
An abstract factory exposes methods like `createButton()`, `createCheckbox()`. Concrete factories produce a matching set.

## When to use
- Multiple product families that must stay consistent
- Cross-platform UI / theme / cloud-provider SDKs

## Tradeoffs
- More interfaces; harder to add a new product type (all factories change)

## Interview tip
Factory Method = one product. Abstract Factory = **group** of products.

## Run
```bash
javac Demo.java && java Demo
```
