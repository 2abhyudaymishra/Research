"""
Beginner demo: Primary–replica replication with lag.
"""

from __future__ import annotations

import time
from dataclasses import dataclass, field


@dataclass
class Node:
    name: str
    data: dict[str, str] = field(default_factory=dict)

    def write(self, key: str, value: str) -> None:
        self.data[key] = value

    def read(self, key: str) -> str | None:
        return self.data.get(key)


class ReplicatedDB:
    def __init__(self, lag_seconds: float = 0.2) -> None:
        self.primary = Node("primary")
        self.replica = Node("replica")
        self.lag_seconds = lag_seconds
        self._pending: list[tuple[float, str, str]] = []

    def write(self, key: str, value: str) -> None:
        self.primary.write(key, value)
        # Async replication scheduled
        self._pending.append((time.time() + self.lag_seconds, key, value))
        print(f"WRITE primary {key}={value} (replica will catch up)")

    def _apply_replication(self) -> None:
        now = time.time()
        still: list[tuple[float, str, str]] = []
        for when, key, value in self._pending:
            if when <= now:
                self.replica.write(key, value)
                print(f"REPLICATED -> replica {key}={value}")
            else:
                still.append((when, key, value))
        self._pending = still

    def read(self, key: str, from_replica: bool = True) -> str | None:
        self._apply_replication()
        node = self.replica if from_replica else self.primary
        value = node.read(key)
        print(f"READ {node.name} {key} -> {value}")
        return value


def main() -> None:
    db = ReplicatedDB(lag_seconds=0.2)
    db.write("user:1", "alice")

    print("\nImmediate replica read (may be stale):")
    db.read("user:1", from_replica=True)

    print("\nPrimary read (fresh):")
    db.read("user:1", from_replica=False)

    time.sleep(0.25)
    print("\nAfter lag window, replica read:")
    db.read("user:1", from_replica=True)


if __name__ == "__main__":
    main()
