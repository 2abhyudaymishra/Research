/**
 * Composite — file/folder tree; total size computed uniformly.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.List;

public class Demo {

    interface Node {
        String name();
        int size();
    }

    static class FileNode implements Node {
        private final String name;
        private final int bytes;

        FileNode(String name, int bytes) {
            this.name = name;
            this.bytes = bytes;
        }

        public String name() { return name; }
        public int size() { return bytes; }
    }

    static class Folder implements Node {
        private final String name;
        private final List<Node> children = new ArrayList<>();

        Folder(String name) { this.name = name; }

        void add(Node node) { children.add(node); }

        public String name() { return name; }

        public int size() {
            int total = 0;
            for (Node child : children) {
                total += child.size();
            }
            return total;
        }
    }

    public static void main(String[] args) {
        Folder root = new Folder("root");
        Folder docs = new Folder("docs");
        docs.add(new FileNode("a.txt", 10));
        docs.add(new FileNode("b.txt", 20));
        root.add(docs);
        root.add(new FileNode("readme.md", 5));

        System.out.println(root.name() + " size=" + root.size());
    }
}
