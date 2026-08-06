/**
 * Beginner demo: Reverse proxy routing + simple auth header injection.
 *
 *   javac Demo.java && java Demo
 */
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Demo {

    static class AppServer {
        final String name;

        AppServer(String name) {
            this.name = name;
        }

        Map<String, String> handle(String path, Map<String, String> headers) {
            Map<String, String> response = new HashMap<>();
            response.put("server", name);
            response.put("path", path);
            response.put("user", headers.getOrDefault("X-User", "anonymous"));
            return response;
        }
    }

    static class ReverseProxy {
        private final Map<String, AppServer> routes = new LinkedHashMap<>();

        void register(String prefix, AppServer server) {
            routes.put(prefix, server);
        }

        Map<String, Object> forward(String path, String clientToken) {
            Map<String, String> headers = new HashMap<>();
            if ("secret".equals(clientToken)) {
                headers.put("X-User", "alice");
            }

            for (Map.Entry<String, AppServer> entry : routes.entrySet()) {
                if (path.startsWith(entry.getKey())) {
                    System.out.println("PROXY route " + path + " -> " + entry.getValue().name);
                    return new HashMap<>(entry.getValue().handle(path, headers));
                }
            }

            Map<String, Object> notFound = new HashMap<>();
            notFound.put("error", "no_route");
            notFound.put("status", 404);
            return notFound;
        }
    }

    public static void main(String[] args) {
        ReverseProxy proxy = new ReverseProxy();
        proxy.register("/api", new AppServer("api-service"));
        proxy.register("/static", new AppServer("static-service"));

        System.out.println(proxy.forward("/api/users", "secret"));
        System.out.println(proxy.forward("/static/logo.png", null));
        System.out.println(proxy.forward("/admin", null));
    }
}
