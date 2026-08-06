"""
Beginner demo: Load Balancing (round-robin + least-connections).
"""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class Backend:
    name: str
    healthy: bool = True
    active_connections: int = 0
    hits: int = 0

    def handle(self, request_id: int) -> str:
        self.active_connections += 1
        self.hits += 1
        result = f"{self.name} served request#{request_id}"
        self.active_connections -= 1
        return result


@dataclass
class LoadBalancer:
    backends: list[Backend] = field(default_factory=list)
    _rr_index: int = 0

    def add(self, backend: Backend) -> None:
        self.backends.append(backend)

    def _healthy(self) -> list[Backend]:
        return [b for b in self.backends if b.healthy]

    def round_robin(self, request_id: int) -> str:
        healthy = self._healthy()
        if not healthy:
            return "no healthy backends"
        backend = healthy[self._rr_index % len(healthy)]
        self._rr_index += 1
        return backend.handle(request_id)

    def least_connections(self, request_id: int) -> str:
        healthy = self._healthy()
        if not healthy:
            return "no healthy backends"
        # Simulate uneven load before choosing
        for i, b in enumerate(healthy):
            b.active_connections = i  # A=0, B=1, C=2 ...
        backend = min(healthy, key=lambda b: b.active_connections)
        return backend.handle(request_id)


def main() -> None:
    lb = LoadBalancer()
    for name in ("A", "B", "C"):
        lb.add(Backend(name))

    print("=== Round robin ===")
    for i in range(1, 7):
        print(lb.round_robin(i))

    print("\n=== Mark B unhealthy, continue RR ===")
    lb.backends[1].healthy = False
    for i in range(7, 11):
        print(lb.round_robin(i))

    print("\n=== Least connections (B healthy again) ===")
    lb.backends[1].healthy = True
    for i in range(11, 14):
        print(lb.least_connections(i))

    print("\nHit counts:", {b.name: b.hits for b in lb.backends})


if __name__ == "__main__":
    main()
