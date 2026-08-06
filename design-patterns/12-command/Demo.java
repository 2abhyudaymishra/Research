/**
 * Command — remote buttons execute/undo light commands.
 *
 *   javac Demo.java && java Demo
 */
public class Demo {

    interface Command {
        void execute();
        void undo();
    }

    static class Light {
        void on() { System.out.println("Light ON"); }
        void off() { System.out.println("Light OFF"); }
    }

    static class LightOnCommand implements Command {
        private final Light light;
        LightOnCommand(Light light) { this.light = light; }
        public void execute() { light.on(); }
        public void undo() { light.off(); }
    }

    static class LightOffCommand implements Command {
        private final Light light;
        LightOffCommand(Light light) { this.light = light; }
        public void execute() { light.off(); }
        public void undo() { light.on(); }
    }

    static class Remote {
        private Command last;

        void press(Command cmd) {
            cmd.execute();
            last = cmd;
        }

        void undo() {
            if (last != null) {
                last.undo();
            }
        }
    }

    public static void main(String[] args) {
        Light light = new Light();
        Remote remote = new Remote();
        remote.press(new LightOnCommand(light));
        remote.press(new LightOffCommand(light));
        remote.undo();
    }
}
