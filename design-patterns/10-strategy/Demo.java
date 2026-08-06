/**
 * Strategy — swap payment algorithms at runtime.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface PaymentStrategy {
        void pay(int amountCents);
    }

    static class CardPayment implements PaymentStrategy {
        public void pay(int amountCents) {
            System.out.println("Paid " + amountCents + " by CARD");
        }
    }

    static class UpiPayment implements PaymentStrategy {
        public void pay(int amountCents) {
            System.out.println("Paid " + amountCents + " by UPI");
        }
    }

    static class Checkout {
        private PaymentStrategy strategy;

        void setStrategy(PaymentStrategy strategy) { this.strategy = strategy; }

        void checkout(int amountCents) {
            strategy.pay(amountCents);
        }
    }

    public static void main(String[] args) {
        Checkout cart = new Checkout();
        cart.setStrategy(new CardPayment());
        cart.checkout(1000);
        cart.setStrategy(new UpiPayment());
        cart.checkout(500);
    }
}
