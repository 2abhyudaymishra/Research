/**
 * Beginner demo: Simple Bloom filter.
 *
 *   javac Demo.java && java Demo
 */
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Demo {

    static class BloomFilter {
        private final int size;
        private final int hashCount;
        private final int[] bits;

        BloomFilter(int size, int hashCount) {
            this.size = size;
            this.hashCount = hashCount;
            this.bits = new int[size];
        }

        private int[] indexes(String item) {
            int[] indexes = new int[hashCount];
            try {
                for (int i = 0; i < hashCount; i++) {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] digest = md.digest((i + ":" + item).getBytes(StandardCharsets.UTF_8));
                    long h = 0;
                    for (int b = 0; b < 8; b++) {
                        h = (h << 8) | (digest[b] & 0xffL);
                    }
                    indexes[i] = (int) Math.floorMod(h, size);
                }
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            return indexes;
        }

        void add(String item) {
            for (int idx : indexes(item)) {
                bits[idx] = 1;
            }
            System.out.println("ADD " + item);
        }

        boolean mightContain(String item) {
            for (int idx : indexes(item)) {
                if (bits[idx] == 0) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void main(String[] args) {
        BloomFilter bf = new BloomFilter(32, 3);
        for (String name : List.of("alice", "bob", "carol")) {
            bf.add(name);
        }

        for (String p : List.of("alice", "bob", "dave", "erin", "carol")) {
            boolean result = bf.mightContain(p);
            System.out.println("contains(" + p + ")? " + (result ? "MAYBE" : "NO"));
        }

        System.out.println("\nNote: MAYBE can be a false positive; NO is reliable.");
    }
}
