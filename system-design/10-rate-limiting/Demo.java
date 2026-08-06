/**
 * Beginner demo: Token bucket rate limiter.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    static class TokenBucket {
        private final double capacity;
        private final double refillRatePerSec;
        private double tokens;
        private long updatedAtNs;

        TokenBucket(double capacity, double refillRatePerSec) {
            this.capacity = capacity;
            this.refillRatePerSec = refillRatePerSec;
            this.tokens = capacity;
            this.updatedAtNs = System.nanoTime();
        }

        private void refill() {
            long now = System.nanoTime();
            double elapsedSec = (now - updatedAtNs) / 1_000_000_000.0;
            tokens = Math.min(capacity, tokens + elapsedSec * refillRatePerSec);
            updatedAtNs = now;
        }

        synchronized boolean allow(double cost) {
            refill();
            if (tokens >= cost) {
                tokens -= cost;
                return true;
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucket limiter = new TokenBucket(5, 2);

        System.out.println("Burst of 7 requests:");
        for (int i = 1; i <= 7; i++) {
            boolean ok = limiter.allow(1);
            System.out.println("  req#" + i + ": " + (ok ? "ALLOW" : "REJECT 429"));
        }

        System.out.println("\nWait 1.2s for refill...");
        Thread.sleep(1200);
        for (int i = 8; i <= 10; i++) {
            boolean ok = limiter.allow(1);
            System.out.println("  req#" + i + ": " + (ok ? "ALLOW" : "REJECT 429"));
        }
    }
}
