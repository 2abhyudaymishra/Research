"""
Beginner demo: Cache-aside pattern with TTL.
"""

from __future__ import annotations

import time
from dataclasses import dataclass


@dataclass
class CacheEntry:
    value: str
    expires_at: float


class Database:
    def __init__(self) -> None:
        self.data = {"product:1": "Laptop", "product:2": "Phone"}
        self.reads = 0

    def get(self, key: str) -> str | None:
        self.reads += 1
        time.sleep(0.05)  # pretend DB is slow
        return self.data.get(key)


class Cache:
    def __init__(self) -> None:
        self.store: dict[str, CacheEntry] = {}
        self.hits = 0
        self.misses = 0

    def get(self, key: str) -> str | None:
        entry = self.store.get(key)
        if not entry:
            self.misses += 1
            return None
        if time.time() > entry.expires_at:
            del self.store[key]
            self.misses += 1
            return None
        self.hits += 1
        return entry.value

    def set(self, key: str, value: str, ttl_seconds: float) -> None:
        self.store[key] = CacheEntry(value, time.time() + ttl_seconds)


class App:
    def __init__(self, db: Database, cache: Cache, ttl: float = 1.0) -> None:
        self.db = db
        self.cache = cache
        self.ttl = ttl

    def get_product(self, key: str) -> str | None:
        # Cache-aside
        cached = self.cache.get(key)
        if cached is not None:
            print(f"CACHE HIT  {key} -> {cached}")
            return cached

        print(f"CACHE MISS {key} -> reading DB")
        value = self.db.get(key)
        if value is not None:
            self.cache.set(key, value, self.ttl)
        return value


def main() -> None:
    app = App(Database(), Cache(), ttl=0.2)

    app.get_product("product:1")
    app.get_product("product:1")  # hit
    time.sleep(0.25)              # TTL expired
    app.get_product("product:1")  # miss again
    app.get_product("product:2")

    print(f"\nDB reads={app.db.reads}, cache hits={app.cache.hits}, misses={app.cache.misses}")


if __name__ == "__main__":
    main()
