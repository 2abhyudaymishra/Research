"""
Beginner demo: Retry with exponential backoff + jitter.
"""

from __future__ import annotations

import random
import time


def call_with_retry(fn, max_attempts: int = 5, base_delay: float = 0.05) -> str:
    attempt = 0
    while True:
        attempt += 1
        try:
            print(f"attempt {attempt}...")
            return fn(attempt)
        except Exception as exc:
            if attempt >= max_attempts:
                raise
            delay = base_delay * (2 ** (attempt - 1))
            delay = delay * (0.5 + random.random())  # jitter 50%–150%
            print(f"  failed: {exc}; sleeping {delay:.3f}s")
            time.sleep(delay)


def main() -> None:
    def flaky(attempt: int) -> str:
        if attempt < 4:
            raise TimeoutError("temporary glitch")
        return "success"

    result = call_with_retry(flaky)
    print("result:", result)


if __name__ == "__main__":
    main()
