/**
 * Factory Method — notify via email or SMS based on channel.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface Notifier {
        void send(String to, String msg);
    }

    static class EmailNotifier implements Notifier {
        public void send(String to, String msg) {
            System.out.println("EMAIL to " + to + ": " + msg);
        }
    }

    static class SmsNotifier implements Notifier {
        public void send(String to, String msg) {
            System.out.println("SMS to " + to + ": " + msg);
        }
    }

    static class NotifierFactory {
        static Notifier create(String channel) {
            return switch (channel) {
                case "email" -> new EmailNotifier();
                case "sms" -> new SmsNotifier();
                default -> throw new IllegalArgumentException("unknown: " + channel);
            };
        }
    }

    public static void main(String[] args) {
        Notifier n1 = NotifierFactory.create("email");
        Notifier n2 = NotifierFactory.create("sms");
        n1.send("a@x.com", "Welcome");
        n2.send("+91111", "OTP 1234");
    }
}
