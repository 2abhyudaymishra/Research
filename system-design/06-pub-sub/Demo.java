/**
 * Beginner demo: In-process pub/sub topic with fan-out.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Demo {

    static class Topic {
        private final String name;
        private final Map<String, Consumer<Map<String, Object>>> subs = new LinkedHashMap<>();

        Topic(String name) {
            this.name = name;
        }

        void subscribe(String subscriberName, Consumer<Map<String, Object>> handler) {
            subs.put(subscriberName, handler);
            System.out.println("SUBSCRIBE " + subscriberName + " -> " + name);
        }

        void publish(Map<String, Object> event) {
            System.out.println("PUBLISH on " + name + ": " + event);
            for (Map.Entry<String, Consumer<Map<String, Object>>> entry : subs.entrySet()) {
                System.out.println("  fan-out -> " + entry.getKey());
                entry.getValue().accept(event);
            }
        }
    }

    public static void main(String[] args) {
        Topic orders = new Topic("orders");

        List<String> emailLog = new ArrayList<>();
        List<String> analyticsLog = new ArrayList<>();
        List<String> searchLog = new ArrayList<>();

        orders.subscribe("email-service", e -> emailLog.add("email to " + e.get("user")));
        orders.subscribe("analytics", e -> analyticsLog.add("track " + e.get("order_id")));
        orders.subscribe("search-indexer", e -> searchLog.add("index order " + e.get("order_id")));

        orders.publish(Map.of("order_id", 1001, "user", "alice", "total", 42));

        System.out.println("\nemail: " + emailLog);
        System.out.println("analytics: " + analyticsLog);
        System.out.println("search: " + searchLog);
    }
}
