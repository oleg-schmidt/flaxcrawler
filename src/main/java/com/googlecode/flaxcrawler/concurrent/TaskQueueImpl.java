package com.googlecode.flaxcrawler.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * Task Queue implementation
 */
public class TaskQueueImpl implements TaskQueue {

    private Logger log = Logger.getLogger(this.getClass());
    private Queue queue = new DefaultQueue();
    private List<TaskQueueWorker> workers = new ArrayList<TaskQueueWorker>();
    private boolean started;
    private SortedMap<Long, Task> deferredTasks = new TreeMap<Long, Task>();
    private final Object syncRoot = new Object();
    private final Object queueSyncRoot = new Object();
    private int processingTasksCount;

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

    public void taskProcessed(Task task) {
        synchronized (queueSyncRoot) {
            processingTasksCount--;
        }
    }

    @Override
    public void enqueue(Task task) throws TaskQueueException {
        synchronized (queueSyncRoot) {
            try {
                queue.push(task);
            } catch (Exception ex) {
                String message = "Cannot enqueue specified task";
                log.error(message);
                throw new TaskQueueException(message, ex);
            }
        }
    }

    @Override
    public void defer(Task task, long timeout) {
        synchronized (queueSyncRoot) {
            deferredTasks.put(timeout + System.currentTimeMillis(), task);
        }
    }

    @Override
    public Task dequeue() {
        synchronized (queueSyncRoot) {
            Task task = null;

            if (deferredTasks.size() > 0 && deferredTasks.firstKey() <= System.currentTimeMillis()) {
                // There's a deferred task ready for execution
                task = deferredTasks.remove(deferredTasks.firstKey());
            } else {
                // There's no deferred tasks ready for execution - dequeueing task from the task queue
                task = (Task) queue.poll();
            }

            if (task != null) {
                processingTasksCount++;
            }
            return task;
        }
    }

    @Override
    public int size() {
        synchronized (queueSyncRoot) {
            return queue.size() + deferredTasks.size() + processingTasksCount;
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
}
