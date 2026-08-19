package se.mickelus.mutil.scheduling;

import com.google.common.collect.Queues;
import net.minecraft.server.TickTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Iterator;
import java.util.Queue;

@ParametersAreNonnullByDefault
public class AbstractScheduler {
    private static final Logger logger = LogManager.getLogger();
    private final Queue<Task> queue = Queues.newConcurrentLinkedQueue();
    private int counter;

    public void schedule(int delay, Runnable task) {
        queue.add(new Task(counter + delay, task));
    }

    public void schedule(String id, int delay, Runnable task) {
        queue.removeIf(t -> id.equals(t.id));
        queue.add(new Task(id, counter + delay, task));
    }

    public void tick() {
        for (Iterator<Task> it = queue.iterator(); it.hasNext(); ) {
            Task task = it.next();
            if (task.getTick() < counter) {
                // removed first: a task that throws used to stay in the queue and throw again on
                // every tick after, so one bad task became an unbounded stream of them
                it.remove();
                try {
                    task.run();
                } catch (RuntimeException e) {
                    logger.error("Scheduled task '{}' threw and was dropped", task.id, e);
                }
            }
        }

        counter++;
    }

    static class Task extends TickTask {
        private String id;

        public Task(int timestamp, Runnable task) {
            super(timestamp, task);
        }

        public Task(String id, int timestamp, Runnable task) {
            this(timestamp, task);

            this.id = id;
        }
    }
}
