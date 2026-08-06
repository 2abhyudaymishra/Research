"""
Beginner demo: CQRS with separate write store and read projection.
"""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class WriteModel:
    orders: dict[int, dict] = field(default_factory=dict)
    events: list[dict] = field(default_factory=list)

    def place_order(self, order_id: int, user: str, total: float) -> None:
        order = {"id": order_id, "user": user, "total": total, "status": "PLACED"}
        self.orders[order_id] = order
        self.events.append({"type": "OrderPlaced", "order": order.copy()})
        print(f"COMMAND PlaceOrder {order}")


@dataclass
class ReadModel:
    """Denormalized view optimized for queries."""

    by_user: dict[str, list[dict]] = field(default_factory=dict)

    def project(self, event: dict) -> None:
        if event["type"] == "OrderPlaced":
            order = event["order"]
            self.by_user.setdefault(order["user"], []).append(
                {"id": order["id"], "total": order["total"]}
            )
            print(f"PROJECTION updated read model for {order['user']}")

    def list_orders_for(self, user: str) -> list[dict]:
        return self.by_user.get(user, [])


def main() -> None:
    writes = WriteModel()
    reads = ReadModel()

    writes.place_order(1, "alice", 20)
    writes.place_order(2, "alice", 15)
    writes.place_order(3, "bob", 40)

    # Async projection (simulated)
    for event in writes.events:
        reads.project(event)

    print("\nQUERY alice orders:", reads.list_orders_for("alice"))
    print("QUERY bob orders:", reads.list_orders_for("bob"))


if __name__ == "__main__":
    main()
