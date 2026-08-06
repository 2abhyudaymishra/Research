"""
Beginner demo: In-memory message queue with visibility timeout + DLQ.
"""

from __future__ import annotations

import time
import uuid
from dataclasses import dataclass, field


@dataclass
class Message:
    body: str
    id: str = field(default_factory=lambda: str(uuid.uuid4())[:8])
    receive_count: int = 0
    visible_at: float = 0.0


class Queue:
    def __init__(self, visibility_timeout: float = 0.2, max_receives: int = 3) -> None:
        self.messages: list[Message] = []
        self.dlq: list[Message] = []
        self.visibility_timeout = visibility_timeout
        self.max_receives = max_receives

    def send(self, body: str) -> str:
        msg = Message(body=body)
        self.messages.append(msg)
        print(f"ENQUEUE {msg.id}: {body}")
        return msg.id

    def receive(self) -> Message | None:
        now = time.time()
        for msg in self.messages:
            if msg.visible_at <= now:
                msg.receive_count += 1
                msg.visible_at = now + self.visibility_timeout
                print(f"DEQUEUE {msg.id} (attempt {msg.receive_count})")
                return msg
        return None

    def delete(self, msg_id: str) -> None:
        self.messages = [m for m in self.messages if m.id != msg_id]
        print(f"ACK/DELETE {msg_id}")

    def reclaim_to_dlq(self) -> None:
        kept: list[Message] = []
        for msg in self.messages:
            if msg.receive_count >= self.max_receives:
                self.dlq.append(msg)
                print(f"MOVED TO DLQ {msg.id}: {msg.body}")
            else:
                kept.append(msg)
        self.messages = kept


def main() -> None:
    q = Queue(visibility_timeout=0.15, max_receives=3)
    q.send("resize-image:42")
    q.send("send-email:alice")

    # Happy path
    msg = q.receive()
    assert msg is not None
    q.delete(msg.id)

    # Poison message: fail until DLQ
    poison = q.receive()
    assert poison is not None
    for _ in range(3):
        time.sleep(0.16)  # visibility expires → can receive again
        again = q.receive()
        print(f"  consumer failed processing {again.id if again else None}")
    q.reclaim_to_dlq()

    print(f"\nQueue left={[m.body for m in q.messages]}")
    print(f"DLQ={[m.body for m in q.dlq]}")


if __name__ == "__main__":
    main()
