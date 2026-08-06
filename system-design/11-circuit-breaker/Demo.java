/**
 * Beginner demo: Circuit breaker states.
 *
 *   javac Demo.java && java Demo
 */
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {

    enum State { CLOSED, OPEN, HALF_OPEN }

    static class CircuitBreaker {
        private final int failureThreshold;
        private final long recoveryTimeMs;
        private int failures;
        private State state = State.CLOSED;
        private long openedAtMs;

        CircuitBreaker(int failureThreshold, long recoveryTimeMs) {
            this.failureThreshold = failureThreshold;
            this.recoveryTimeMs = recoveryTimeMs;
        }

        <T> T call(Callable<T> fn) throws Exception {
            long now = System.currentTimeMillis();
            if (state == State.OPEN) {
                if (now - openedAtMs >= recoveryTimeMs) {
                    state = State.HALF_OPEN;
                    System.out.println("STATE -> HALF_OPEN (trial)");
                } else {
                    throw new IllegalStateException("circuit open — fail fast");
                }
            }

            try {
                T result = fn.call();
                failures = 0;
                if (state == State.HALF_OPEN) {
                    state = State.CLOSED;
                    System.out.println("STATE -> CLOSED (recovered)");
                }
                return result;
            } catch (Exception e) {
                failures++;
                System.out.println("FAILURE count=" + failures);
                if (failures >= failureThreshold || state == State.HALF_OPEN) {
                    state = State.OPEN;
                    openedAtMs = now;
                    System.out.println("STATE -> OPEN");
                }
                throw e;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicBoolean unhealthy = new AtomicBoolean(true);

        Callable<String> paymentApi = () -> {
            if (unhealthy.get()) {
                throw new RuntimeException("payment timeout");
            }
            return "paid";
        };

        CircuitBreaker cb = new CircuitBreaker(3, 400);

        for (int i = 1; i <= 4; i++) {
            try {
                System.out.println("attempt " + i + ": " + cb.call(paymentApi));
            } catch (Exception exc) {
                System.out.println("attempt " + i + ": " + exc.getMessage());
            }
        }

        System.out.println("\nWaiting for recovery window...");
        Thread.sleep(450);
        unhealthy.set(false);
        System.out.println("Dependency is healthy again");
        try {
            System.out.println("trial: " + cb.call(paymentApi));
        } catch (Exception e) {
            System.out.println("trial failed: " + e.getMessage());
        }
    }
}
