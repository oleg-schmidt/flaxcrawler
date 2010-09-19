package com.googlecode.flaxcrawler.concurrent;

import java.util.LinkedList;

/**
 * Default queue implementation
 * @author ameshkov
 */
public class DefaultQueue implements Queue {

    private LinkedList innerList = new LinkedList();

    public void add(Object obj) {
        innerList.add(obj);
    }

    public void defer(Object obj) {
        add(obj);
    }

    public Object poll() {
        return innerList.poll();
    }

    public void dispose() {
        // Do nothing
    }

    public int size() {
        return innerList.size();
    }
}
