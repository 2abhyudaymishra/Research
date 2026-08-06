"""
Beginner demo: Idempotent payment API using idempotency keys.
"""

from __future__ import annotations

from dataclasses import dataclass, field


@dataclass
class PaymentService:
    charges: list[dict] = field(default_factory=list)
    _results: dict[str, dict] = field(default_factory=dict)

    def charge(self, idempotency_key: str, user: str, amount: float) -> dict:
        if idempotency_key in self._results:
            print(f"IDEMPOTENT HIT key={idempotency_key} (no new charge)")
            return self._results[idempotency_key]

        charge = {"user": user, "amount": amount, "status": "captured"}
        self.charges.append(charge)
        result = {"charge_id": len(self.charges), **charge}
        self._results[idempotency_key] = result
        print(f"CHARGED key={idempotency_key} -> {result}")
        return result


def main() -> None:
    payments = PaymentService()

    # Client retries the same logical request
    r1 = payments.charge("key-123", "alice", 10)
    r2 = payments.charge("key-123", "alice", 10)  # retry
    r3 = payments.charge("key-999", "alice", 10)  # different key

    print("\nSame response for retries:", r1 == r2)
    print("Total charges stored:", len(payments.charges))
    print("Third request new charge id:", r3["charge_id"])


if __name__ == "__main__":
    main()
