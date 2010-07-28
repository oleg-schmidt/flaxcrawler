package com.googlecode.flaxcrawler.concurrent;

/**
 * Represents simple Queue
 */
public interface Queue {

    void push(Object obj) throws TaskQueueException;

    Object poll();

    void dispose();
}
