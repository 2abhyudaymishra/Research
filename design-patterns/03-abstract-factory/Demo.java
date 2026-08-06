/**
 * Abstract Factory — light/dark UI component families.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface Button { void paint(); }
    interface Checkbox { void paint(); }

    interface UIFactory {
        Button createButton();
        Checkbox createCheckbox();
    }

    static class LightButton implements Button {
        public void paint() { System.out.println("Light Button"); }
    }
    static class LightCheckbox implements Checkbox {
        public void paint() { System.out.println("Light Checkbox"); }
    }
    static class DarkButton implements Button {
        public void paint() { System.out.println("Dark Button"); }
    }
    static class DarkCheckbox implements Checkbox {
        public void paint() { System.out.println("Dark Checkbox"); }
    }

    static class LightFactory implements UIFactory {
        public Button createButton() { return new LightButton(); }
        public Checkbox createCheckbox() { return new LightCheckbox(); }
    }

    static class DarkFactory implements UIFactory {
        public Button createButton() { return new DarkButton(); }
        public Checkbox createCheckbox() { return new DarkCheckbox(); }
    }

    static void render(UIFactory factory) {
        factory.createButton().paint();
        factory.createCheckbox().paint();
    }

    public static void main(String[] args) {
        System.out.println("-- light --");
        render(new LightFactory());
        System.out.println("-- dark --");
        render(new DarkFactory());
    }
}
