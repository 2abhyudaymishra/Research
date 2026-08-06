/**
 * Beginner demo: Client–Server pattern (in-process simulation).
 *
 * Compile & run:
 *   javac Demo.java && java Demo
 */
import java.util.HashMap;
import java.util.Map;

public class Demo {

    static class Server {
        private final String name;
        private final Map<String, Map<String, String>> users = new HashMap<>();

        Server(String name) {
            this.name = name;
            users.put("alice", Map.of("plan", "pro"));
            users.put("bob", Map.of("plan", "free"));
        }

        Map<String, Object> handle(String path, String userId) {
            Map<String, Object> response = new HashMap<>();
            if ("/health".equals(path)) {
                response.put("ok", true);
                response.put("server", name);
                return response;
            }
            if ("/me".equals(path)) {
                Map<String, String> profile = users.get(userId);
                if (profile == null) {
                    response.put("error", "not_found");
                    response.put("status", 404);
                    return response;
                }
                response.put("user", userId);
                response.put("profile", profile);
                response.put("status", 200);
                return response;
            }
            response.put("error", "unknown_path");
            response.put("status", 404);
            return response;
        }
    }

    static class Client {
        private final Server server;

        Client(Server server) {
            this.server = server;
        }

        Map<String, Object> get(String path, String userId) {
            System.out.printf("CLIENT → GET %s (user=%s)%n", path, userId.isEmpty() ? "-" : userId);
            Map<String, Object> response = server.handle(path, userId);
            System.out.println("SERVER ← " + response);
            return response;
        }
    }

    public static void main(String[] args) {
        Server server = new Server("api-1");
        Client client = new Client(server);

        client.get("/health", "");
        client.get("/me", "alice");
        client.get("/me", "nobody");
    }
}
