/**
 * Proxy — virtual proxy lazy-loads a heavy image.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface Image {
        void display();
    }

    static class RealImage implements Image {
        private final String file;

        RealImage(String file) {
            this.file = file;
            System.out.println("LOADING from disk: " + file);
        }

        public void display() {
            System.out.println("DISPLAY " + file);
        }
    }

    static class ImageProxy implements Image {
        private final String file;
        private RealImage real;

        ImageProxy(String file) { this.file = file; }

        public void display() {
            if (real == null) {
                real = new RealImage(file); // lazy
            }
            real.display();
        }
    }

    public static void main(String[] args) {
        Image img = new ImageProxy("photo.png");
        System.out.println("proxy created (not loaded yet)");
        img.display();
        img.display(); // second time: no reload
    }
}
