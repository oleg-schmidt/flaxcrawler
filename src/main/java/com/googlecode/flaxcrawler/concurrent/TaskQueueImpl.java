package com.googlecode.flaxcrawler.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Task Queue implementation
 */
public class TaskQueueImpl implements TaskQueue {

    private Logger log = Logger.getLogger(this.getClass());
    private Queue queue = new DefaultQueue();
    private List<TaskQueueWorker> workers = new ArrayList<TaskQueueWorker>();
    private boolean started;
    private final Object syncRoot = new Object();
    private final Object queueSyncRoot = new Object();
    private int processingTasksCount;
    private int maxParallelProcessingSequences;
    private Map<String, Integer> processingSequences = new HashMap<String, Integer>();
    // Maximum loops count while searching for suitable task
    private final static int MAX_LOOPS = 1000;

    /**
     * Sets maximum number of parallel thread processing tasks with the same sequence name.
     * Default value - 0 (unlimited). Limit is ignored for tasks with sequence name equal to {@code null}.
     * @param maxParallelProcessingSequences
     */
    public void setMaxParallelProcessingSequences(int maxParallelProcessingSequences) {
        this.maxParallelProcessingSequences = maxParallelProcessingSequences;
    }

    /**
     * Sets inner queue ({@link DefaultQueue} is used by default. Also you can use {@link BerkleyQueue}.)
     * @param queue
     */
    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    @Override
    public void start() throws TaskQueueException {
        synchronized (syncRoot) {
            if (!isStarted()) {
                setStarted(true);
                for (TaskQueueWorker worker : workers) {
                    worker.start();
                }
            } else {
                log.error("TaskQueue is already started");
                throw new TaskQueueException("TaskQueue is already started");
            }
        }
    }

    @Override
    public void stop() throws TaskQueueException {
        synchronized (syncRoot) {
            setStarted(false);
            for (TaskQueueWorker worker : workers) {
                worker.stop();
            }
        }
    }

    @Override
    public void dispose() throws TaskQueueException {
        synchronized (syncRoot) {
            stop();
            queue.dispose();
        }
    }

    @Override
    public void join() throws TaskQueueException {
        for (TaskQueueWorker worker : workers) {
            worker.join();
        }
    }

    public int getProcessingTasksCount() {
        synchronized (queueSyncRoot) {
            return processingTasksCount;
        }
    }

    @Override
    public void enqueue(Task task) throws TaskQueueException {
        synchronized (queueSyncRoot) {
            try {
                queue.add(task);
            } catch (Exception ex) {
                String message = "Cannot enqueue specified task";
                log.error(message);
                throw new TaskQueueException(message, ex);
            }
        }
    }

    @Override
    public void defer(Task task) {
        synchronized (queueSyncRoot) {
            queue.defer(task);
        }
    }

    @Override
    public Task dequeue() {
        // Looping through the queue until suitable task is found
        for (int i = 0; i < MAX_LOOPS; i++) {
            synchronized (queueSyncRoot) {
                Task task = (Task) queue.poll();

                if (task != null) {
                    if (startProcessingTask(task)) {
                        // Task is ok, returning it
                        processingTasksCount++;
                        return task;
                    } else {
                        // Max parallel processing sequences limit was hit, adding task to the end of queue
                        queue.add(task);
                    }
                } else if (task == null) {
                    return task;
                }
            }
        }

        // Nothing found, returning null
        return null;
    }

    @Override
    public int size() {
        synchronized (queueSyncRoot) {
            return queue.size() + processingTasksCount;
        }
    }

    @Override
    public void addWorker(TaskQueueWorker worker) throws TaskQueueException {
        synchronized (syncRoot) {
            if (!isStarted()) {
                worker.setTaskQueue(this);
                workers.add(worker);
            } else {
                String message = "Error while adding task queue worker. Task queue is already started.";
                log.error(message);
                throw new TaskQueueException(message);
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    protected void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Method is called when task is dequeued. Method checks maxParallelProcessingSequences value
     * and returns {@code true} if limit is not hit and worker can process with this task, or {@code false}
     * if task execution should be deferred.
     * @param task
     * @return
     */
    protected boolean startProcessingTask(Task task) {
        if (maxParallelProcessingSequences == 0 || task.getSequenceName() == null) {
            return true;
        }

        synchronized (queueSyncRoot) {
            Integer count = processingSequences.get(task.getSequenceName());

            if (count == null) {
                count = 0;
            }

            if (count == maxParallelProcessingSequences) {
                // Limit already hit for this sequence, returning false
                return false;
            }

            processingSequences.put(task.getSequenceName(), count + 1);
        }

        return true;
    }

    /**
     * Method is called when task has been processed. Decrements processing sequences counter.
     * @param task
     */
    protected void stopProcessingTask(Task task) {
        if (task.getSequenceName() == null) {
            return;
        }

        synchronized (queueSyncRoot) {
            Integer count = processingSequences.get(task.getSequenceName());
            processingSequences.put(task.getSequenceName(), count - 1);
        }
    }

    /**
     * Method is called by a worker after task is processed
     * @param task
     */
    public void taskProcessed(Task task) {
        synchronized (queueSyncRoot) {
            processingTasksCount--;
            stopProcessingTask(task);
        }
    }
}
