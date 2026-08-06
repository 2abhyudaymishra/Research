/**
 * State — order moves New → Paid → Shipped with state-specific behavior.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface OrderState {
        void pay(Order ctx);
        void ship(Order ctx);
    }

    static class Order {
        private OrderState state = new NewState();
        void setState(OrderState state) { this.state = state; }
        void pay() { state.pay(this); }
        void ship() { state.ship(this); }
    }

    static class NewState implements OrderState {
        public void pay(Order ctx) {
            System.out.println("payment captured");
            ctx.setState(new PaidState());
        }
        public void ship(Order ctx) {
            System.out.println("cannot ship before pay");
        }
    }

    static class PaidState implements OrderState {
        public void pay(Order ctx) {
            System.out.println("already paid");
        }
        public void ship(Order ctx) {
            System.out.println("shipped");
            ctx.setState(new ShippedState());
        }
    }

    static class ShippedState implements OrderState {
        public void pay(Order ctx) { System.out.println("already shipped"); }
        public void ship(Order ctx) { System.out.println("already shipped"); }
    }

    public static void main(String[] args) {
        Order order = new Order();
        order.ship(); // invalid
        order.pay();
        order.ship();
        order.pay();  // no-op-ish
    }
}
