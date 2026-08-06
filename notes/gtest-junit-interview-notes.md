# Google Test & JUnit — Interview Notes

Interview-oriented notes for **Google Test (C++)** and **JUnit (Java)**. Minimal examples — only what you need to explain and write in interviews.

---

## 1. Shared testing basics (say this first)

| Idea | Meaning |
|---|---|
| **Unit test** | Tests one unit (function/class) in isolation |
| **Arrange–Act–Assert** | Setup → call → check result |
| **Assertion** | Statement that must be true or test fails |
| **Fixture** | Shared setup/teardown for related tests |
| **Mock** | Fake dependency you control (verify interactions) |
| **Test pyramid** | Many fast unit tests; fewer integration/e2e |

**Good interview habits**
- Name tests by behavior: `shouldReturnZeroWhenListEmpty`
- One logical assert per behavior (multiple asserts OK if same behavior)
- Deterministic — no sleep/random/order dependence
- Don’t test private internals; test observable behavior

---

## 2. Side-by-side map

| Concept | Google Test (C++) | JUnit 5 (Java) |
|---|---|---|
| Framework | gtest (+ gmock) | JUnit 5 (Jupiter) |
| Test case | `TEST(Suite, Case)` | `@Test` method |
| Fixture | `TEST_F` + `::testing::Test` | `@BeforeEach` / `@AfterEach` |
| Assert equal | `EXPECT_EQ(a, b)` | `assertEquals(expected, actual)` |
| Assert true | `EXPECT_TRUE(x)` | `assertTrue(x)` |
| Fatal vs continue | `ASSERT_*` vs `EXPECT_*` | usually fail-fast per assert |
| Exceptions | `EXPECT_THROW` | `assertThrows` |
| Parameterized | `TEST_P` + `INSTANTIATE_TEST_SUITE_P` | `@ParameterizedTest` |
| Disable | `DISABLED_` prefix | `@Disabled` |
| Mocking | **gmock** | Mockito (common with JUnit) |

---

## 3. Google Test (C++)

### What interviewers expect
- `TEST` vs `TEST_F`
- `EXPECT_*` vs `ASSERT_*`
- Basic matchers; maybe death/throw tests
- Optional: gmock `EXPECT_CALL`

### Minimal test
```cpp
#include <gtest/gtest.h>

int Add(int a, int b) { return a + b; }

TEST(AddTest, HandlesPositive) {
  EXPECT_EQ(Add(2, 3), 5);      // continue on failure
  ASSERT_EQ(Add(1, 1), 2);      // abort test body on failure
}
```

### Fixture (`TEST_F`)
```cpp
class StackTest : public ::testing::Test {
 protected:
  void SetUp() override { s.push(1); }   // before each test
  void TearDown() override {}            // after each test
  std::stack<int> s;
};

TEST_F(StackTest, TopIsOne) {
  EXPECT_EQ(s.top(), 1);
}
```

### Exceptions
```cpp
EXPECT_THROW(std::stoi("x"), std::invalid_argument);
EXPECT_NO_THROW(std::stoi("42"));
```

### EXPECT vs ASSERT (common question)
| Macro | On failure |
|---|---|
| `EXPECT_*` | Mark fail, **continue** test |
| `ASSERT_*` | Mark fail, **stop** that test immediately |

Use `ASSERT_*` when later lines are unsafe (e.g. pointer must be non-null).

### gmock (one pattern)
```cpp
class Dep {
 public:
  virtual ~Dep() = default;
  virtual int Get() = 0;
};
class MockDep : public Dep {
 public:
  MOCK_METHOD(int, Get, (), (override));
};

TEST(ServiceTest, UsesDep) {
  MockDep mock;
  EXPECT_CALL(mock, Get()).WillOnce(::testing::Return(7));
  EXPECT_EQ(mock.Get(), 7);
}
```

**Interview line:** *gmock sets expectations on calls; unmet/unexpected calls fail the test.*

---

## 4. JUnit 5 (Java)

### What interviewers expect
- `@Test`, lifecycle annotations
- Assertions (`org.junit.jupiter.api.Assertions`)
- `assertThrows`
- `@ParameterizedTest` (bonus)
- Mockito: `when` / `verify` (almost always paired in industry interviews)

### Minimal test
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddTest {
  int add(int a, int b) { return a + b; }

  @Test
  void handlesPositive() {
    assertEquals(5, add(2, 3));
    assertTrue(add(1, 1) > 0);
  }
}
```

### Lifecycle / fixture
```java
import org.junit.jupiter.api.*;

class StackTest {
  Deque<Integer> stack;

  @BeforeEach
  void setUp() { stack = new ArrayDeque<>(); stack.push(1); }

  @AfterEach
  void tearDown() { stack = null; }

  @Test
  void topIsOne() { assertEquals(1, stack.peek()); }
}
```

| Annotation | When |
|---|---|
| `@BeforeEach` / `@AfterEach` | Each test |
| `@BeforeAll` / `@AfterAll` | Once per class (`static` methods) |
| `@Disabled` | Skip test |
| `@DisplayName("...")` | Readable name in report |
| `@Tag("slow")` | Filter groups |

### Exceptions
```java
@Test
void parseBadInput() {
  assertThrows(NumberFormatException.class, () -> Integer.parseInt("x"));
}
```

### Parameterized (enough for interview)
```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ParameterizedTest
@CsvSource({"2,3,5", "0,0,0", "-1,1,0"})
void add(int a, int b, int expected) {
  assertEquals(expected, a + b);
}
```

### Mockito (one pattern)
```java
import static org.mockito.Mockito.*;

interface Dep { int get(); }

@Test
void usesDep() {
  Dep dep = mock(Dep.class);
  when(dep.get()).thenReturn(7);

  assertEquals(7, dep.get());
  verify(dep, times(1)).get();
}
```

**Interview line:** *stub with `when(...).thenReturn(...)`; assert interactions with `verify`.*

---

## 5. Interview Q&A (both)

**Q: Unit vs integration test?**  
Unit = isolated (deps mocked). Integration = real modules/DB/network involved.

**Q: Why mocks?**  
Replace slow/flaky/external deps; force edge cases; verify the unit’s collaboration.

**Q: What makes a bad test?**  
Brittle (coupled to internals), non-deterministic, unclear name, tests many behaviors, no assertion.

**Q: AAA pattern?**  
Arrange data → Act (call SUT) → Assert outcome.

**Q: gtest EXPECT vs ASSERT?**  
EXPECT continues; ASSERT aborts that test.

**Q: JUnit 4 vs 5 (if asked)?**  
JUnit 5 = Jupiter engine, `org.junit.jupiter.api`, better parameterized/extensions; JUnit 4 uses `@Before`, `@Ignore`, different packages.

**Q: How do you run them?**  
- gtest: binary built with CMake/Bazel; `--gtest_filter=Suite.Case`  
- JUnit: Maven/Gradle (`mvn test` / `gradle test`) or IDE

**Q: Test isolation?**  
No shared mutable state across tests; fixtures reset each time (`SetUp` / `@BeforeEach`).

---

## 6. Tiny cheat sheet

```text
gtest:  TEST / TEST_F / EXPECT_EQ / ASSERT_EQ / EXPECT_THROW / EXPECT_CALL
JUnit:  @Test / @BeforeEach / assertEquals / assertThrows / @ParameterizedTest
Mocks:  gmock EXPECT_CALL  |  Mockito when + verify
```

**Write in interview:** one happy-path test + one edge/exception test; name them clearly; assert behavior, not implementation.
