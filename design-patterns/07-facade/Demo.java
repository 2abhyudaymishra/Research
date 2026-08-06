/**
 * Facade — one checkout() call orchestrates inventory, payment, email.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    static class Inventory {
        void reserve(String sku) { System.out.println("reserve " + sku); }
    }
    static class Payment {
        void charge(int cents) { System.out.println("charge " + cents); }
    }
    static class Mailer {
        void send(String to) { System.out.println("email " + to); }
    }

    static class CheckoutFacade {
        private final Inventory inventory = new Inventory();
        private final Payment payment = new Payment();
        private final Mailer mailer = new Mailer();

        void checkout(String sku, int cents, String email) {
            inventory.reserve(sku);
            payment.charge(cents);
            mailer.send(email);
            System.out.println("checkout done");
        }
    }

    public static void main(String[] args) {
        new CheckoutFacade().checkout("SKU-1", 5000, "a@x.com");
    }
}
