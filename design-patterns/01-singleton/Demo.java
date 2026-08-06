/**
 * Singleton — enum style (simple, thread-safe, serialization-safe).
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    enum AppConfig {
        INSTANCE;

        private String env = "dev";

        String env() { return env; }

        void setEnv(String env) { this.env = env; }
    }

    public static void main(String[] args) {
        AppConfig a = AppConfig.INSTANCE;
        AppConfig b = AppConfig.INSTANCE;
        a.setEnv("prod");

        System.out.println("same instance? " + (a == b));
        System.out.println("env via b: " + b.env());
    }
}
