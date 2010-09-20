package com.googlecode.flaxcrawler.concurrent;

/**
 * Task interface
 */
public interface Task {

    /**
     * You can limit number of parallel processing task with the same sequence name.
     * Limit is ignored if sequence name is {@code null}.
     * @return
     */
    String getSequenceName();
}
