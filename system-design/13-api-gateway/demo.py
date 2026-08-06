"""
Beginner demo: Tiny API Gateway with auth, rate limit, and routing.
"""

from __future__ import annotations

from dataclasses import dataclass


@dataclass
class Response:
    status: int
    body: dict


class UsersService:
    def handle(self, path: str) -> Response:
        return Response(200, {"service": "users", "path": path})


class OrdersService:
    def handle(self, path: str) -> Response:
        return Response(200, {"service": "orders", "path": path})


class ApiGateway:
    def __init__(self) -> None:
        self.routes = {
            "/users": UsersService(),
            "/orders": OrdersService(),
        }
        self.valid_tokens = {"tok-alice"}
        self.remaining = {"tok-alice": 3}

    def handle(self, path: str, token: str | None) -> Response:
        if token not in self.valid_tokens:
            return Response(401, {"error": "unauthorized"})

        if self.remaining.get(token, 0) <= 0:
            return Response(429, {"error": "rate_limited"})
        self.remaining[token] -= 1

        for prefix, service in self.routes.items():
            if path.startswith(prefix):
                print(f"GATEWAY {path} -> {prefix} service")
                return service.handle(path)

        return Response(404, {"error": "not_found"})


def main() -> None:
    gw = ApiGateway()
    calls = [
        ("/users/1", None),
        ("/users/1", "tok-alice"),
        ("/orders/9", "tok-alice"),
        ("/orders/9", "tok-alice"),
        ("/orders/9", "tok-alice"),  # rate limited
        ("/payments", "tok-alice"),
    ]
    for path, token in calls:
        resp = gw.handle(path, token)
        print(f"  {path} => {resp.status} {resp.body}")


if __name__ == "__main__":
    main()
