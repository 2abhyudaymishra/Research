/**
 * Beginner demo: CQRS with separate write store and read projection.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Demo {

    static class WriteModel {
        final Map<Integer, Map<String, Object>> orders = new HashMap<>();
        final List<Map<String, Object>> events = new ArrayList<>();

        void placeOrder(int orderId, String user, double total) {
            Map<String, Object> order = new LinkedHashMap<>();
            order.put("id", orderId);
            order.put("user", user);
            order.put("total", total);
            order.put("status", "PLACED");
            orders.put(orderId, order);

            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", "OrderPlaced");
            event.put("order", new LinkedHashMap<>(order));
            events.add(event);
            System.out.println("COMMAND PlaceOrder " + order);
        }
    }

    static class ReadModel {
        private final Map<String, List<Map<String, Object>>> byUser = new HashMap<>();

        void project(Map<String, Object> event) {
            if ("OrderPlaced".equals(event.get("type"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> order = (Map<String, Object>) event.get("order");
                String user = (String) order.get("user");
                Map<String, Object> view = new LinkedHashMap<>();
                view.put("id", order.get("id"));
                view.put("total", order.get("total"));
                byUser.computeIfAbsent(user, k -> new ArrayList<>()).add(view);
                System.out.println("PROJECTION updated read model for " + user);
            }
        }

        List<Map<String, Object>> listOrdersFor(String user) {
            return byUser.getOrDefault(user, List.of());
        }
    }

    public static void main(String[] args) {
        WriteModel writes = new WriteModel();
        ReadModel reads = new ReadModel();

        writes.placeOrder(1, "alice", 20);
        writes.placeOrder(2, "alice", 15);
        writes.placeOrder(3, "bob", 40);

        for (Map<String, Object> event : writes.events) {
            reads.project(event);
        }

        System.out.println("\nQUERY alice orders: " + reads.listOrdersFor("alice"));
        System.out.println("QUERY bob orders: " + reads.listOrdersFor("bob"));
    }
}
