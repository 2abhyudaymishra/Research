/**
 * Builder — fluent construction of an HttpRequest-like object.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    static final class HttpRequest {
        final String method;
        final String url;
        final String body;

        private HttpRequest(Builder b) {
            this.method = b.method;
            this.url = b.url;
            this.body = b.body;
        }

        public String toString() {
            return method + " " + url + " body=" + body;
        }

        static class Builder {
            private String method = "GET";
            private String url;
            private String body = "";

            Builder method(String method) { this.method = method; return this; }
            Builder url(String url) { this.url = url; return this; }
            Builder body(String body) { this.body = body; return this; }

            HttpRequest build() {
                if (url == null || url.isBlank()) {
                    throw new IllegalStateException("url required");
                }
                return new HttpRequest(this);
            }
        }
    }

    public static void main(String[] args) {
        HttpRequest req = new HttpRequest.Builder()
                .method("POST")
                .url("https://api.example.com/orders")
                .body("{\"id\":1}")
                .build();
        System.out.println(req);
    }
}
