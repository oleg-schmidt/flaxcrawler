package com.googlecode.flaxcrawler.concurrent;

import java.util.LinkedList;

/**
 * Default queue implementation
 * @author ameshkov
 */
public class DefaultQueue implements Queue {

    private LinkedList innerList = new LinkedList();

    public void push(Object obj) {
        innerList.add(obj);
    }

    public Object poll() {
        return innerList.poll();
    }

    public void dispose() {
        // Do nothing
    }
}
