"""
Beginner demo: Hash-based database sharding.
"""

from __future__ import annotations


class Shard:
    def __init__(self, name: str) -> None:
        self.name = name
        self.data: dict[str, str] = {}

    def put(self, key: str, value: str) -> None:
        self.data[key] = value

    def get(self, key: str) -> str | None:
        return self.data.get(key)


class ShardedDB:
    def __init__(self, shard_count: int = 3) -> None:
        self.shards = [Shard(f"shard-{i}") for i in range(shard_count)]

    def _shard_for(self, key: str) -> Shard:
        idx = hash(key) % len(self.shards)
        # Note: Python's hash() is randomized per process — fine for demo.
        return self.shards[idx]

    def put(self, key: str, value: str) -> None:
        shard = self._shard_for(key)
        shard.put(key, value)
        print(f"PUT {key} -> {shard.name}")

    def get(self, key: str) -> str | None:
        shard = self._shard_for(key)
        value = shard.get(key)
        print(f"GET {key} from {shard.name} -> {value}")
        return value

    def stats(self) -> dict[str, int]:
        return {s.name: len(s.data) for s in self.shards}


def main() -> None:
    db = ShardedDB(3)
    for i in range(10):
        db.put(f"user:{i}", f"name-{i}")

    db.get("user:4")
    print("\nKeys per shard:", db.stats())


if __name__ == "__main__":
    main()
