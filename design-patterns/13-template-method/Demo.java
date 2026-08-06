/**
 * Template Method — shared data-export pipeline with format-specific steps.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    abstract static class DataExporter {
        /** Template method — fixed skeleton. */
        final void export() {
            fetch();
            String formatted = format();
            send(formatted);
        }

        private void fetch() {
            System.out.println("fetch rows from DB");
        }

        protected abstract String format();

        private void send(String payload) {
            System.out.println("send: " + payload);
        }
    }

    static class CsvExporter extends DataExporter {
        protected String format() { return "id,name\n1,Ada"; }
    }

    static class JsonExporter extends DataExporter {
        protected String format() { return "[{\"id\":1,\"name\":\"Ada\"}]"; }
    }

    public static void main(String[] args) {
        new CsvExporter().export();
        System.out.println("---");
        new JsonExporter().export();
    }
}
