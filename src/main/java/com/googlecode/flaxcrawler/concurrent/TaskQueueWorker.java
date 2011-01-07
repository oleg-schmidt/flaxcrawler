package com.googlecode.flaxcrawler.concurrent;

/**
 * Task queue worker interface
 */
public interface TaskQueueWorker {

    /**
     * Starts task queue worker
     */
    void start();

    /**
     * Stops task queue worker
     */
    void stop();

    /**
     * Joins worker thread
     */
    void join();

    /**
     * Joins worker thread (or waits for timeout to exceed)
     * @param timeout
     */
    void join(long timeout);

    /**
     * Sets this workers queue
     * @param taskQueue
     */
    void setTaskQueue(TaskQueue taskQueue);
}
