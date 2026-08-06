/**
 * Beginner demo: Tiny API Gateway with auth, rate limit, and routing.
 *
 *   javac Demo.java && java Demo
 */
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Demo {

    static class Response {
        final int status;
        final Map<String, Object> body;

        Response(int status, Map<String, Object> body) {
            this.status = status;
            this.body = body;
        }

        @Override
        public String toString() {
            return status + " " + body;
        }
    }

    interface Service {
        Response handle(String path);
    }

    static class UsersService implements Service {
        public Response handle(String path) {
            return new Response(200, Map.of("service", "users", "path", path));
        }
    }

    static class OrdersService implements Service {
        public Response handle(String path) {
            return new Response(200, Map.of("service", "orders", "path", path));
        }
    }

    static class ApiGateway {
        private final Map<String, Service> routes = new LinkedHashMap<>();
        private final Map<String, Integer> remaining = new HashMap<>();

        ApiGateway() {
            routes.put("/users", new UsersService());
            routes.put("/orders", new OrdersService());
            remaining.put("tok-alice", 3);
        }

        Response handle(String path, String token) {
            if (!remaining.containsKey(token) && !"tok-alice".equals(token)) {
                // invalid token (only tok-alice is valid in this demo)
            }
            if (!"tok-alice".equals(token)) {
                return new Response(401, Map.of("error", "unauthorized"));
            }
            if (remaining.getOrDefault(token, 0) <= 0) {
                return new Response(429, Map.of("error", "rate_limited"));
            }
            remaining.put(token, remaining.get(token) - 1);

            for (Map.Entry<String, Service> entry : routes.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    System.out.println("GATEWAY " + path + " -> " + entry.getKey() + " service");
                    return entry.getValue().handle(path);
                }
            }
            return new Response(404, Map.of("error", "not_found"));
        }
    }

    public static void main(String[] args) {
        ApiGateway gw = new ApiGateway();
        List<String[]> calls = List.of(
                new String[]{"/users/1", null},
                new String[]{"/users/1", "tok-alice"},
                new String[]{"/orders/9", "tok-alice"},
                new String[]{"/orders/9", "tok-alice"},
                new String[]{"/orders/9", "tok-alice"},
                new String[]{"/payments", "tok-alice"}
        );

        for (String[] call : calls) {
            Response resp = gw.handle(call[0], call[1]);
            System.out.println("  " + call[0] + " => " + resp);
        }
    }
}
