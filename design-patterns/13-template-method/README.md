# 13 — Template Method

## Problem
Several classes share the same algorithm skeleton but differ in a few steps.

## Idea
Put the skeleton in a base class method; mark varying steps as overridable (hooks/abstract methods).

```text
final process() {
  stepA();
  stepB();   // subclass
  stepC();
}
```

## When to use
- Shared workflow with customizable steps
- Frameworks that let users override hooks

## Tradeoffs
- Inheritance-based (less flexible than Strategy composition)
- Parent changes affect all children

## Interview tip
Compare with Strategy: Template Method uses **inheritance**; Strategy uses **composition**. Java: abstract classes in frameworks, JUnit test lifecycle conceptually similar.

## Run
```bash
javac Demo.java && java Demo
```
