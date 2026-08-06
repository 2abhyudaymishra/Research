"""
Beginner demo: Consistent hashing ring with virtual nodes.
"""

from __future__ import annotations

import bisect
import hashlib


def _hash(value: str) -> int:
    return int(hashlib.md5(value.encode()).hexdigest(), 16)


class ConsistentHash:
    def __init__(self, virtual_nodes: int = 8) -> None:
        self.virtual_nodes = virtual_nodes
        self._ring_keys: list[int] = []
        self._ring: dict[int, str] = {}
        self._nodes: set[str] = set()

    def add_node(self, node: str) -> None:
        if node in self._nodes:
            return
        self._nodes.add(node)
        for i in range(self.virtual_nodes):
            h = _hash(f"{node}#{i}")
            bisect.insort(self._ring_keys, h)
            self._ring[h] = node
        print(f"ADD node {node}")

    def remove_node(self, node: str) -> None:
        if node not in self._nodes:
            return
        self._nodes.remove(node)
        for i in range(self.virtual_nodes):
            h = _hash(f"{node}#{i}")
            idx = bisect.bisect_left(self._ring_keys, h)
            if idx < len(self._ring_keys) and self._ring_keys[idx] == h:
                self._ring_keys.pop(idx)
                self._ring.pop(h, None)
        print(f"REMOVE node {node}")

    def get_node(self, key: str) -> str | None:
        if not self._ring_keys:
            return None
        h = _hash(key)
        idx = bisect.bisect_left(self._ring_keys, h) % len(self._ring_keys)
        return self._ring[self._ring_keys[idx]]


def distribution(ring: ConsistentHash, keys: list[str]) -> dict[str, int]:
    counts: dict[str, int] = {}
    for key in keys:
        node = ring.get_node(key)
        assert node is not None
        counts[node] = counts.get(node, 0) + 1
    return counts


def main() -> None:
    ring = ConsistentHash(virtual_nodes=10)
    for n in ("A", "B", "C"):
        ring.add_node(n)

    keys = [f"user:{i}" for i in range(30)]
    before = {k: ring.get_node(k) for k in keys}
    print("Distribution:", distribution(ring, keys))

    ring.add_node("D")
    after = {k: ring.get_node(k) for k in keys}
    moved = sum(1 for k in keys if before[k] != after[k])
    print("Distribution after adding D:", distribution(ring, keys))
    print(f"Keys moved: {moved}/{len(keys)} (should be a minority)")


if __name__ == "__main__":
    main()
