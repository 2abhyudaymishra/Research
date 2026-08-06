/**
 * Beginner demo: Load Balancing (round-robin + least-connections).
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Demo {

    static class Backend {
        final String name;
        boolean healthy = true;
        int activeConnections;
        int hits;

        Backend(String name) {
            this.name = name;
        }

        String handle(int requestId) {
            activeConnections++;
            hits++;
            String result = name + " served request#" + requestId;
            activeConnections--;
            return result;
        }
    }

    static class LoadBalancer {
        final List<Backend> backends = new ArrayList<>();
        private int rrIndex;

        void add(Backend backend) {
            backends.add(backend);
        }

        private List<Backend> healthy() {
            return backends.stream().filter(b -> b.healthy).collect(Collectors.toList());
        }

        String roundRobin(int requestId) {
            List<Backend> healthy = healthy();
            if (healthy.isEmpty()) {
                return "no healthy backends";
            }
            Backend backend = healthy.get(rrIndex % healthy.size());
            rrIndex++;
            return backend.handle(requestId);
        }

        String leastConnections(int requestId) {
            List<Backend> healthy = healthy();
            if (healthy.isEmpty()) {
                return "no healthy backends";
            }
            for (int i = 0; i < healthy.size(); i++) {
                healthy.get(i).activeConnections = i; // simulate uneven load
            }
            Backend backend = healthy.stream()
                    .min(Comparator.comparingInt(b -> b.activeConnections))
                    .orElseThrow();
            return backend.handle(requestId);
        }
    }

    public static void main(String[] args) {
        LoadBalancer lb = new LoadBalancer();
        for (String name : List.of("A", "B", "C")) {
            lb.add(new Backend(name));
        }

        System.out.println("=== Round robin ===");
        for (int i = 1; i <= 6; i++) {
            System.out.println(lb.roundRobin(i));
        }

        System.out.println("\n=== Mark B unhealthy, continue RR ===");
        lb.backends.get(1).healthy = false;
        for (int i = 7; i <= 10; i++) {
            System.out.println(lb.roundRobin(i));
        }

        System.out.println("\n=== Least connections (B healthy again) ===");
        lb.backends.get(1).healthy = true;
        for (int i = 11; i <= 13; i++) {
            System.out.println(lb.leastConnections(i));
        }

        Map<String, Integer> hits = new LinkedHashMap<>();
        for (Backend b : lb.backends) {
            hits.put(b.name, b.hits);
        }
        System.out.println("\nHit counts: " + hits);
    }
}
