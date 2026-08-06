/**
 * Beginner demo: Hash-based database sharding.
 *
 *   javac Demo.java && java Demo
 */
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Demo {

    static class Shard {
        final String name;
        final Map<String, String> data = new HashMap<>();

        Shard(String name) {
            this.name = name;
        }

        void put(String key, String value) {
            data.put(key, value);
        }

        String get(String key) {
            return data.get(key);
        }
    }

    static class ShardedDB {
        private final Shard[] shards;

        ShardedDB(int shardCount) {
            shards = new Shard[shardCount];
            for (int i = 0; i < shardCount; i++) {
                shards[i] = new Shard("shard-" + i);
            }
        }

        private Shard shardFor(String key) {
            int idx = Math.floorMod(key.hashCode(), shards.length);
            return shards[idx];
        }

        void put(String key, String value) {
            Shard shard = shardFor(key);
            shard.put(key, value);
            System.out.println("PUT " + key + " -> " + shard.name);
        }

        String get(String key) {
            Shard shard = shardFor(key);
            String value = shard.get(key);
            System.out.println("GET " + key + " from " + shard.name + " -> " + value);
            return value;
        }

        Map<String, Integer> stats() {
            Map<String, Integer> result = new LinkedHashMap<>();
            for (Shard s : shards) {
                result.put(s.name, s.data.size());
            }
            return result;
        }
    }

    public static void main(String[] args) {
        ShardedDB db = new ShardedDB(3);
        for (int i = 0; i < 10; i++) {
            db.put("user:" + i, "name-" + i);
        }

        db.get("user:4");
        System.out.println("\nKeys per shard: " + db.stats());
    }
}
