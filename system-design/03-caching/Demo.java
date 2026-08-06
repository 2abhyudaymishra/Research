/**
 * Beginner demo: Cache-aside pattern with TTL.
 *
 *   javac Demo.java && java Demo
 */
import java.util.HashMap;
import java.util.Map;

public class Demo {

    static class CacheEntry {
        final String value;
        final long expiresAtMs;

        CacheEntry(String value, long expiresAtMs) {
            this.value = value;
            this.expiresAtMs = expiresAtMs;
        }
    }

    static class Database {
        private final Map<String, String> data = Map.of(
                "product:1", "Laptop",
                "product:2", "Phone"
        );
        int reads;

        String get(String key) {
            reads++;
            try {
                Thread.sleep(50); // pretend DB is slow
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return data.get(key);
        }
    }

    static class Cache {
        private final Map<String, CacheEntry> store = new HashMap<>();
        int hits;
        int misses;

        String get(String key) {
            CacheEntry entry = store.get(key);
            if (entry == null) {
                misses++;
                return null;
            }
            if (System.currentTimeMillis() > entry.expiresAtMs) {
                store.remove(key);
                misses++;
                return null;
            }
            hits++;
            return entry.value;
        }

        void set(String key, String value, long ttlMs) {
            store.put(key, new CacheEntry(value, System.currentTimeMillis() + ttlMs));
        }
    }

    static class App {
        final Database db;
        final Cache cache;
        private final long ttlMs;

        App(Database db, Cache cache, long ttlMs) {
            this.db = db;
            this.cache = cache;
            this.ttlMs = ttlMs;
        }

        String getProduct(String key) {
            String cached = cache.get(key);
            if (cached != null) {
                System.out.println("CACHE HIT  " + key + " -> " + cached);
                return cached;
            }
            System.out.println("CACHE MISS " + key + " -> reading DB");
            String value = db.get(key);
            if (value != null) {
                cache.set(key, value, ttlMs);
            }
            return value;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        App app = new App(new Database(), new Cache(), 200);

        app.getProduct("product:1");
        app.getProduct("product:1"); // hit
        Thread.sleep(250);           // TTL expired
        app.getProduct("product:1"); // miss again
        app.getProduct("product:2");

        System.out.printf("%nDB reads=%d, cache hits=%d, misses=%d%n",
                app.db.reads, app.cache.hits, app.cache.misses);
    }
}
