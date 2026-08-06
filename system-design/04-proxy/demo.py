"""
Beginner demo: Reverse proxy routing + simple auth header injection.
"""

from __future__ import annotations


class AppServer:
    def __init__(self, name: str) -> None:
        self.name = name

    def handle(self, path: str, headers: dict[str, str]) -> dict:
        return {
            "server": self.name,
            "path": path,
            "user": headers.get("X-User", "anonymous"),
        }


class ReverseProxy:
    def __init__(self) -> None:
        self.routes: dict[str, AppServer] = {}

    def register(self, prefix: str, server: AppServer) -> None:
        self.routes[prefix] = server

    def forward(self, path: str, client_token: str | None = None) -> dict:
        headers: dict[str, str] = {}
        if client_token == "secret":
            headers["X-User"] = "alice"

        for prefix, server in self.routes.items():
            if path.startswith(prefix):
                print(f"PROXY route {path} -> {server.name}")
                return server.handle(path, headers)

        return {"error": "no_route", "status": 404}


def main() -> None:
    proxy = ReverseProxy()
    proxy.register("/api", AppServer("api-service"))
    proxy.register("/static", AppServer("static-service"))

    print(proxy.forward("/api/users", client_token="secret"))
    print(proxy.forward("/static/logo.png"))
    print(proxy.forward("/admin"))


if __name__ == "__main__":
    main()
