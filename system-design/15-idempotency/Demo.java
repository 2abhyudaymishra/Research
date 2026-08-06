/**
 * Beginner demo: Idempotent payment API using idempotency keys.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Demo {

    static class PaymentService {
        private final List<Map<String, Object>> charges = new ArrayList<>();
        private final Map<String, Map<String, Object>> results = new HashMap<>();

        Map<String, Object> charge(String idempotencyKey, String user, double amount) {
            if (results.containsKey(idempotencyKey)) {
                System.out.println("IDEMPOTENT HIT key=" + idempotencyKey + " (no new charge)");
                return results.get(idempotencyKey);
            }

            Map<String, Object> charge = new LinkedHashMap<>();
            charge.put("user", user);
            charge.put("amount", amount);
            charge.put("status", "captured");
            charges.add(charge);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("charge_id", charges.size());
            result.putAll(charge);
            results.put(idempotencyKey, result);
            System.out.println("CHARGED key=" + idempotencyKey + " -> " + result);
            return result;
        }

        int chargeCount() {
            return charges.size();
        }
    }

    public static void main(String[] args) {
        PaymentService payments = new PaymentService();

        Map<String, Object> r1 = payments.charge("key-123", "alice", 10);
        Map<String, Object> r2 = payments.charge("key-123", "alice", 10); // retry
        Map<String, Object> r3 = payments.charge("key-999", "alice", 10); // different key

        System.out.println("\nSame response for retries: " + Objects.equals(r1, r2));
        System.out.println("Total charges stored: " + payments.chargeCount());
        System.out.println("Third request new charge id: " + r3.get("charge_id"));
    }
}
