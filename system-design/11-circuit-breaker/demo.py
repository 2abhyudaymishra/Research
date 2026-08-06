"""
Beginner demo: Circuit breaker states.
"""

from __future__ import annotations

import time
from enum import Enum


class State(Enum):
    CLOSED = "CLOSED"
    OPEN = "OPEN"
    HALF_OPEN = "HALF_OPEN"


class CircuitBreaker:
    def __init__(self, failure_threshold: int = 3, recovery_time: float = 0.5) -> None:
        self.failure_threshold = failure_threshold
        self.recovery_time = recovery_time
        self.failures = 0
        self.state = State.CLOSED
        self.opened_at = 0.0

    def call(self, fn):
        now = time.time()
        if self.state == State.OPEN:
            if now - self.opened_at >= self.recovery_time:
                self.state = State.HALF_OPEN
                print("STATE -> HALF_OPEN (trial)")
            else:
                raise RuntimeError("circuit open — fail fast")

        try:
            result = fn()
        except Exception:
            self.failures += 1
            print(f"FAILURE count={self.failures}")
            if self.failures >= self.failure_threshold or self.state == State.HALF_OPEN:
                self.state = State.OPEN
                self.opened_at = now
                print("STATE -> OPEN")
            raise

        # success
        self.failures = 0
        if self.state == State.HALF_OPEN:
            self.state = State.CLOSED
            print("STATE -> CLOSED (recovered)")
        return result


def main() -> None:
    unhealthy = {"down": True}

    def payment_api() -> str:
        if unhealthy["down"]:
            raise ConnectionError("payment timeout")
        return "paid"

    cb = CircuitBreaker(failure_threshold=3, recovery_time=0.4)

    for i in range(1, 5):
        try:
            print(f"attempt {i}:", cb.call(payment_api))
        except Exception as exc:
            print(f"attempt {i}: {exc}")

    print("\nWaiting for recovery window...")
    time.sleep(0.45)
    unhealthy["down"] = False
    print("Dependency is healthy again")
    print("trial:", cb.call(payment_api))


if __name__ == "__main__":
    main()
