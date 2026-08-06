# Design Patterns — Java Demos

Must-know **object-oriented design patterns** (interview set).  
Each folder has:

- `README.md` — what / why / when / tradeoffs
- `Demo.java` — small runnable Java example (JDK only)

```bash
cd design-patterns/01-singleton
javac Demo.java && java Demo
```

## Patterns

### Creational
| # | Folder | Pattern | One-line idea |
|---|---|---|---|
| 01 | [singleton](01-singleton) | Singleton | One shared instance |
| 02 | [factory-method](02-factory-method) | Factory Method | Subclass decides which product to create |
| 03 | [abstract-factory](03-abstract-factory) | Abstract Factory | Family of related objects |
| 04 | [builder](04-builder) | Builder | Step-by-step construction of complex objects |

### Structural
| # | Folder | Pattern | One-line idea |
|---|---|---|---|
| 05 | [adapter](05-adapter) | Adapter | Make incompatible interfaces work together |
| 06 | [decorator](06-decorator) | Decorator | Add behavior without changing the class |
| 07 | [facade](07-facade) | Facade | Simple front for a complex subsystem |
| 08 | [proxy](08-proxy) | Proxy | Placeholder controlling access to an object |
| 09 | [composite](09-composite) | Composite | Tree of objects treated uniformly |

### Behavioral
| # | Folder | Pattern | One-line idea |
|---|---|---|---|
| 10 | [strategy](10-strategy) | Strategy | Swap algorithms at runtime |
| 11 | [observer](11-observer) | Observer | Notify dependents on state change |
| 12 | [command](12-command) | Command | Encapsulate a request as an object |
| 13 | [template-method](13-template-method) | Template Method | Skeleton algorithm; subclasses fill steps |
| 14 | [state](14-state) | State | Behavior changes with internal state |
| 15 | [chain-of-responsibility](15-chain-of-responsibility) | Chain of Responsibility | Pass request along a handler chain |

## How to study

1. Read the README (problem → idea → when to use).
2. Run `javac Demo.java && java Demo`.
3. Explain the pattern in 30 seconds without looking.
4. Relate to Java/Spring examples (e.g. Strategy ≈ sorting Comparator, Observer ≈ events, Decorator ≈ `InputStream` wrappers).

## Requirements

- JDK 11+ (tested with JDK 21)
- No Maven/Gradle or third-party libraries
