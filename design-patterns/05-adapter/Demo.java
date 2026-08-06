/**
 * Adapter — make a legacy XML payment API look like a modern PaymentProcessor.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface PaymentProcessor {
        void pay(String customerId, int amountCents);
    }

    /** Legacy / third-party API we cannot change. */
    static class LegacyXmlGateway {
        void submitXml(String xml) {
            System.out.println("LEGACY received: " + xml);
        }
    }

    static class LegacyGatewayAdapter implements PaymentProcessor {
        private final LegacyXmlGateway gateway;

        LegacyGatewayAdapter(LegacyXmlGateway gateway) {
            this.gateway = gateway;
        }

        public void pay(String customerId, int amountCents) {
            String xml = "<pay customer=\"" + customerId + "\" amount=\"" + amountCents + "\"/>";
            gateway.submitXml(xml);
        }
    }

    public static void main(String[] args) {
        PaymentProcessor processor = new LegacyGatewayAdapter(new LegacyXmlGateway());
        processor.pay("user-42", 1999);
    }
}
