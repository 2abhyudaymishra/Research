"""
Beginner demo: Simple Bloom filter.
"""

from __future__ import annotations

import hashlib


class BloomFilter:
    def __init__(self, size: int = 64, hash_count: int = 3) -> None:
        self.size = size
        self.hash_count = hash_count
        self.bits = [0] * size

    def _indexes(self, item: str) -> list[int]:
        indexes: list[int] = []
        for i in range(self.hash_count):
            digest = hashlib.md5(f"{i}:{item}".encode()).hexdigest()
            indexes.append(int(digest, 16) % self.size)
        return indexes

    def add(self, item: str) -> None:
        for idx in self._indexes(item):
            self.bits[idx] = 1
        print(f"ADD {item}")

    def might_contain(self, item: str) -> bool:
        return all(self.bits[idx] == 1 for idx in self._indexes(item))


def main() -> None:
    bf = BloomFilter(size=32, hash_count=3)
    for name in ("alice", "bob", "carol"):
        bf.add(name)

    probes = ["alice", "bob", "dave", "erin", "carol"]
    for p in probes:
        result = bf.might_contain(p)
        label = "MAYBE" if result else "NO"
        print(f"contains({p})? {label}")

    print("\nNote: MAYBE can be a false positive; NO is reliable.")


if __name__ == "__main__":
    main()
