"""
Beginner demo: Token bucket rate limiter.
"""

from __future__ import annotations

import time
from dataclasses import dataclass


@dataclass
class TokenBucket:
    capacity: float
    refill_rate_per_sec: float
    tokens: float | None = None
    updated_at: float | None = None

    def __post_init__(self) -> None:
        self.tokens = float(self.capacity)
        self.updated_at = time.time()

    def _refill(self) -> None:
        now = time.time()
        elapsed = now - (self.updated_at or now)
        self.tokens = min(self.capacity, (self.tokens or 0) + elapsed * self.refill_rate_per_sec)
        self.updated_at = now

    def allow(self, cost: float = 1.0) -> bool:
        self._refill()
        if (self.tokens or 0) >= cost:
            self.tokens = (self.tokens or 0) - cost
            return True
        return False


def main() -> None:
    # 5 burst capacity, refill 2 tokens/sec
    limiter = TokenBucket(capacity=5, refill_rate_per_sec=2)

    print("Burst of 7 requests:")
    for i in range(1, 8):
        ok = limiter.allow()
        print(f"  req#{i}: {'ALLOW' if ok else 'REJECT 429'}")

    print("\nWait 1.2s for refill...")
    time.sleep(1.2)
    for i in range(8, 11):
        ok = limiter.allow()
        print(f"  req#{i}: {'ALLOW' if ok else 'REJECT 429'}")


if __name__ == "__main__":
    main()
