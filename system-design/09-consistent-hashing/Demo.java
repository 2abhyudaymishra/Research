/**
 * Beginner demo: Consistent hashing ring with virtual nodes.
 *
 *   javac Demo.java && java Demo
 */
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Demo {

    static class ConsistentHash {
        private final int virtualNodes;
        private final TreeMap<Long, String> ring = new TreeMap<>();
        private final Set<String> nodes = new HashSet<>();

        ConsistentHash(int virtualNodes) {
            this.virtualNodes = virtualNodes;
        }

        private static long hash(String value) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
                long h = 0;
                for (int i = 0; i < 8; i++) {
                    h = (h << 8) | (digest[i] & 0xffL);
                }
                return h;
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        void addNode(String node) {
            if (!nodes.add(node)) {
                return;
            }
            for (int i = 0; i < virtualNodes; i++) {
                ring.put(hash(node + "#" + i), node);
            }
            System.out.println("ADD node " + node);
        }

        void removeNode(String node) {
            if (!nodes.remove(node)) {
                return;
            }
            for (int i = 0; i < virtualNodes; i++) {
                ring.remove(hash(node + "#" + i));
            }
            System.out.println("REMOVE node " + node);
        }

        String getNode(String key) {
            if (ring.isEmpty()) {
                return null;
            }
            Map.Entry<Long, String> entry = ring.ceilingEntry(hash(key));
            if (entry == null) {
                entry = ring.firstEntry();
            }
            return entry.getValue();
        }
    }

    static Map<String, Integer> distribution(ConsistentHash ring, List<String> keys) {
        Map<String, Integer> counts = new HashMap<>();
        for (String key : keys) {
            String node = ring.getNode(key);
            counts.merge(node, 1, Integer::sum);
        }
        return counts;
    }

    public static void main(String[] args) {
        ConsistentHash ring = new ConsistentHash(10);
        for (String n : List.of("A", "B", "C")) {
            ring.addNode(n);
        }

        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            keys.add("user:" + i);
        }

        Map<String, String> before = new HashMap<>();
        for (String k : keys) {
            before.put(k, ring.getNode(k));
        }
        System.out.println("Distribution: " + distribution(ring, keys));

        ring.addNode("D");
        int moved = 0;
        for (String k : keys) {
            if (!before.get(k).equals(ring.getNode(k))) {
                moved++;
            }
        }
        System.out.println("Distribution after adding D: " + distribution(ring, keys));
        System.out.println("Keys moved: " + moved + "/" + keys.size() + " (should be a minority)");
    }
}
