package com.googlecode.flaxcrawler.concurrent;

/**
 * Represents simple Queue
 */
public interface Queue {

    /**
     * Adds object to the end of a queue
     * @param obj
     * @throws TaskQueueException
     */
    void add(Object obj);

    /**
     * Adds object (but defers it's enqueuing somehow - depends on Queue implementation).
     * In default implementation this method is the same as {@code add}
     * @param obj
     */
    void defer(Object obj);

    /**
     * Removes object from the head of the queue and returns it.
     * @return
     */
    Object poll();

    /**
     * Frees resources used by queue
     */
    void dispose();

    /**
     * Queue size
     * @return
     */
    int size();
}
