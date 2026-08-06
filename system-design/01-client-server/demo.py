"""
Beginner demo: Client–Server pattern (in-process simulation).
"""

from __future__ import annotations


class Server:
    def __init__(self, name: str) -> None:
        self.name = name
        self._users = {"alice": {"plan": "pro"}, "bob": {"plan": "free"}}

    def handle(self, path: str, user_id: str) -> dict:
        if path == "/health":
            return {"ok": True, "server": self.name}
        if path == "/me":
            profile = self._users.get(user_id)
            if not profile:
                return {"error": "not_found", "status": 404}
            return {"user": user_id, "profile": profile, "status": 200}
        return {"error": "unknown_path", "status": 404}


class Client:
    def __init__(self, server: Server) -> None:
        self.server = server

    def get(self, path: str, user_id: str = "") -> dict:
        print(f"CLIENT → GET {path} (user={user_id or '-'})")
        response = self.server.handle(path, user_id)
        print(f"SERVER ← {response}")
        return response


def main() -> None:
    server = Server("api-1")
    client = Client(server)

    client.get("/health")
    client.get("/me", "alice")
    client.get("/me", "nobody")


if __name__ == "__main__":
    main()
