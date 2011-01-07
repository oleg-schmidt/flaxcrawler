package com.googlecode.flaxcrawler.concurrent;

/**
 * Base task queue interface
 */
public interface TaskQueue {

    /**
     * Adds task queue worker. Warning - this method works when {@code TaskQueue} is stopped only.
     * @param worker
     * @throws TaskQueueException
     */
    void addWorker(TaskQueueWorker worker) throws TaskQueueException;

    /**
     * Starts task queue workers
     * @throws TaskQueueException
     */
    void start() throws TaskQueueException;

    /**
     * Stops all workers
     */
    void stop() throws TaskQueueException;

    /**
     * Disposes task queue
     * @throws TaskQueueException
     */
    void dispose() throws TaskQueueException;

    /**
     * Joins worker threads
     * @throws TaskQueueException
     */
    void join() throws TaskQueueException;

    /**
     * Joins worker threads or waits for timeout to exceed
     * @param timeout
     * @throws TaskQueueException
     */
    void join(long timeout) throws TaskQueueException;

    /**
     * Task queue size
     * @return
     */
    int size();

    /**
     * Returns {@code true} if {@code TaskQueue} is started. Otherwise returns {@code false}.
     * Warning - it can return {@code false} while stopping but not fully stopped.
     * @return
     */
    boolean isStarted();

    /**
     * Enqueues new task to the inner queue
     * @param task
     * @throws TaskQueueException
     */
    void enqueue(Task task) throws TaskQueueException;

    /**
     * Defers {@link Task} execution
     * @param task
     * @param timeout
     */
    void defer(Task task);

    /**
     * Signals to the queue that task was processed by a worker
     * @param task
     */
    void taskProcessed(Task task);

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     * @return Task
     */
    Task dequeue();
}
