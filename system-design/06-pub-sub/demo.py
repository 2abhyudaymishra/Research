"""
Beginner demo: In-process pub/sub topic with fan-out.
"""

from __future__ import annotations

from typing import Callable


EventHandler = Callable[[dict], None]


class Topic:
    def __init__(self, name: str) -> None:
        self.name = name
        self._subs: dict[str, EventHandler] = {}

    def subscribe(self, subscriber_name: str, handler: EventHandler) -> None:
        self._subs[subscriber_name] = handler
        print(f"SUBSCRIBE {subscriber_name} -> {self.name}")

    def publish(self, event: dict) -> None:
        print(f"PUBLISH on {self.name}: {event}")
        for name, handler in self._subs.items():
            print(f"  fan-out -> {name}")
            handler(event)


def main() -> None:
    orders = Topic("orders")

    email_log: list[str] = []
    analytics_log: list[str] = []
    search_log: list[str] = []

    orders.subscribe("email-service", lambda e: email_log.append(f"email to {e['user']}"))
    orders.subscribe("analytics", lambda e: analytics_log.append(f"track {e['order_id']}"))
    orders.subscribe("search-indexer", lambda e: search_log.append(f"index order {e['order_id']}"))

    orders.publish({"order_id": 1001, "user": "alice", "total": 42})

    print("\nemail:", email_log)
    print("analytics:", analytics_log)
    print("search:", search_log)


if __name__ == "__main__":
    main()
