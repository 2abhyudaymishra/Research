/**
 * Beginner demo: Primary–replica replication with lag.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Demo {

    static class Node {
        final String name;
        final Map<String, String> data = new HashMap<>();

        Node(String name) {
            this.name = name;
        }

        void write(String key, String value) {
            data.put(key, value);
        }

        String read(String key) {
            return data.get(key);
        }
    }

    static class PendingReplication {
        final long whenMs;
        final String key;
        final String value;

        PendingReplication(long whenMs, String key, String value) {
            this.whenMs = whenMs;
            this.key = key;
            this.value = value;
        }
    }

    static class ReplicatedDB {
        final Node primary = new Node("primary");
        final Node replica = new Node("replica");
        private final long lagMs;
        private final List<PendingReplication> pending = new ArrayList<>();

        ReplicatedDB(long lagMs) {
            this.lagMs = lagMs;
        }

        void write(String key, String value) {
            primary.write(key, value);
            pending.add(new PendingReplication(System.currentTimeMillis() + lagMs, key, value));
            System.out.println("WRITE primary " + key + "=" + value + " (replica will catch up)");
        }

        private void applyReplication() {
            long now = System.currentTimeMillis();
            Iterator<PendingReplication> it = pending.iterator();
            while (it.hasNext()) {
                PendingReplication p = it.next();
                if (p.whenMs <= now) {
                    replica.write(p.key, p.value);
                    System.out.println("REPLICATED -> replica " + p.key + "=" + p.value);
                    it.remove();
                }
            }
        }

        String read(String key, boolean fromReplica) {
            applyReplication();
            Node node = fromReplica ? replica : primary;
            String value = node.read(key);
            System.out.println("READ " + node.name + " " + key + " -> " + value);
            return value;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReplicatedDB db = new ReplicatedDB(200);
        db.write("user:1", "alice");

        System.out.println("\nImmediate replica read (may be stale):");
        db.read("user:1", true);

        System.out.println("\nPrimary read (fresh):");
        db.read("user:1", false);

        Thread.sleep(250);
        System.out.println("\nAfter lag window, replica read:");
        db.read("user:1", true);
    }
}
