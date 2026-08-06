/**
 * Observer — stock price notifies display subscribers.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.List;

public class Demo {

    interface Observer {
        void update(String symbol, double price);
    }

    static class Stock {
        private final String symbol;
        private double price;
        private final List<Observer> observers = new ArrayList<>();

        Stock(String symbol, double price) {
            this.symbol = symbol;
            this.price = price;
        }

        void subscribe(Observer o) { observers.add(o); }

        void setPrice(double price) {
            this.price = price;
            for (Observer o : observers) {
                o.update(symbol, price);
            }
        }
    }

    static class ConsoleDisplay implements Observer {
        private final String name;
        ConsoleDisplay(String name) { this.name = name; }
        public void update(String symbol, double price) {
            System.out.println(name + " saw " + symbol + "=" + price);
        }
    }

    public static void main(String[] args) {
        Stock acme = new Stock("ACME", 10.0);
        acme.subscribe(new ConsoleDisplay("MobileApp"));
        acme.subscribe(new ConsoleDisplay("WebDashboard"));
        acme.setPrice(10.5);
        acme.setPrice(11.0);
    }
}
