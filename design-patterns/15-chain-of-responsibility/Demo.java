/**
 * Chain of Responsibility — support ticket escalation L1 → L2 → L3.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    enum Severity { LOW, MEDIUM, HIGH }

    static class Ticket {
        final String id;
        final Severity severity;
        Ticket(String id, Severity severity) {
            this.id = id;
            this.severity = severity;
        }
    }

    abstract static class Handler {
        private Handler next;

        Handler linkWith(Handler next) {
            this.next = next;
            return next;
        }

        void handle(Ticket ticket) {
            if (canHandle(ticket)) {
                process(ticket);
            } else if (next != null) {
                next.handle(ticket);
            } else {
                System.out.println("Unhandled " + ticket.id);
            }
        }

        protected abstract boolean canHandle(Ticket ticket);
        protected abstract void process(Ticket ticket);
    }

    static class L1 extends Handler {
        protected boolean canHandle(Ticket t) { return t.severity == Severity.LOW; }
        protected void process(Ticket t) { System.out.println("L1 handled " + t.id); }
    }

    static class L2 extends Handler {
        protected boolean canHandle(Ticket t) { return t.severity == Severity.MEDIUM; }
        protected void process(Ticket t) { System.out.println("L2 handled " + t.id); }
    }

    static class L3 extends Handler {
        protected boolean canHandle(Ticket t) { return t.severity == Severity.HIGH; }
        protected void process(Ticket t) { System.out.println("L3 handled " + t.id); }
    }

    public static void main(String[] args) {
        Handler chain = new L1();
        chain.linkWith(new L2()).linkWith(new L3());

        chain.handle(new Ticket("T-1", Severity.LOW));
        chain.handle(new Ticket("T-2", Severity.MEDIUM));
        chain.handle(new Ticket("T-3", Severity.HIGH));
    }
}
