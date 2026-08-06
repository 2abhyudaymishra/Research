/**
 * Decorator — stack milk/sugar on a coffee beverage.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface Coffee {
        String description();
        int costCents();
    }

    static class SimpleCoffee implements Coffee {
        public String description() { return "Coffee"; }
        public int costCents() { return 200; }
    }

    abstract static class CoffeeDecorator implements Coffee {
        protected final Coffee inner;
        CoffeeDecorator(Coffee inner) { this.inner = inner; }
    }

    static class Milk extends CoffeeDecorator {
        Milk(Coffee inner) { super(inner); }
        public String description() { return inner.description() + ", Milk"; }
        public int costCents() { return inner.costCents() + 50; }
    }

    static class Sugar extends CoffeeDecorator {
        Sugar(Coffee inner) { super(inner); }
        public String description() { return inner.description() + ", Sugar"; }
        public int costCents() { return inner.costCents() + 20; }
    }

    public static void main(String[] args) {
        Coffee order = new Sugar(new Milk(new SimpleCoffee()));
        System.out.println(order.description() + " = " + order.costCents() + " cents");
    }
}
