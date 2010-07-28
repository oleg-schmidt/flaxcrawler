package com.googlecode.flaxcrawler.concurrent;

/**
 * Task queue exception
 */
public class TaskQueueException extends Exception {

    private Task task;

    public TaskQueueException() {
    }

    public TaskQueueException(String message) {
        super(message);
    }

    public TaskQueueException(String message, Throwable t) {
        super(message, t);
    }

    public TaskQueueException(String message, Task task, Throwable t) {
        super(message, t);
        this.task = task;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
