/**
 * Beginner demo: Retry with exponential backoff + jitter.
 *
 *   javac Demo.java && java Demo
 */
import java.util.Random;
import java.util.function.IntFunction;

public class Demo {

    static String callWithRetry(IntFunction<String> fn, int maxAttempts, double baseDelaySec)
            throws InterruptedException {
        int attempt = 0;
        Random random = new Random();
        while (true) {
            attempt++;
            try {
                System.out.println("attempt " + attempt + "...");
                return fn.apply(attempt);
            } catch (RuntimeException exc) {
                if (attempt >= maxAttempts) {
                    throw exc;
                }
                double delay = baseDelaySec * Math.pow(2, attempt - 1);
                delay = delay * (0.5 + random.nextDouble()); // jitter 50%–150%
                System.out.printf("  failed: %s; sleeping %.3fs%n", exc.getMessage(), delay);
                Thread.sleep((long) (delay * 1000));
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String result = callWithRetry(attempt -> {
            if (attempt < 4) {
                throw new RuntimeException("temporary glitch");
            }
            return "success";
        }, 5, 0.05);

        System.out.println("result: " + result);
    }
}
