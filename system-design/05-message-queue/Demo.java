/**
 * Beginner demo: In-memory message queue with visibility timeout + DLQ.
 *
 *   javac Demo.java && java Demo
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Demo {

    static class Message {
        final String id = UUID.randomUUID().toString().substring(0, 8);
        final String body;
        int receiveCount;
        long visibleAtMs;

        Message(String body) {
            this.body = body;
        }
    }

    static class Queue {
        final List<Message> messages = new ArrayList<>();
        final List<Message> dlq = new ArrayList<>();
        private final long visibilityTimeoutMs;
        private final int maxReceives;

        Queue(long visibilityTimeoutMs, int maxReceives) {
            this.visibilityTimeoutMs = visibilityTimeoutMs;
            this.maxReceives = maxReceives;
        }

        String send(String body) {
            Message msg = new Message(body);
            messages.add(msg);
            System.out.println("ENQUEUE " + msg.id + ": " + body);
            return msg.id;
        }

        Message receive() {
            long now = System.currentTimeMillis();
            for (Message msg : messages) {
                if (msg.visibleAtMs <= now) {
                    msg.receiveCount++;
                    msg.visibleAtMs = now + visibilityTimeoutMs;
                    System.out.println("DEQUEUE " + msg.id + " (attempt " + msg.receiveCount + ")");
                    return msg;
                }
            }
            return null;
        }

        void delete(String msgId) {
            messages.removeIf(m -> m.id.equals(msgId));
            System.out.println("ACK/DELETE " + msgId);
        }

        void reclaimToDlq() {
            Iterator<Message> it = messages.iterator();
            while (it.hasNext()) {
                Message msg = it.next();
                if (msg.receiveCount >= maxReceives) {
                    dlq.add(msg);
                    System.out.println("MOVED TO DLQ " + msg.id + ": " + msg.body);
                    it.remove();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Queue q = new Queue(150, 3);
        q.send("resize-image:42");
        q.send("send-email:alice");

        Message msg = q.receive();
        q.delete(msg.id);

        Message poison = q.receive();
        for (int i = 0; i < 3; i++) {
            Thread.sleep(160);
            Message again = q.receive();
            System.out.println("  consumer failed processing " + (again == null ? null : again.id));
        }
        q.reclaimToDlq();

        System.out.println("\nQueue left=" + q.messages.stream().map(m -> m.body).collect(Collectors.toList()));
        System.out.println("DLQ=" + q.dlq.stream().map(m -> m.body).collect(Collectors.toList()));
    }
}
