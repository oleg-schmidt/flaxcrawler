package com.googlecode.flaxcrawler.concurrent;

import java.util.LinkedList;

/**
 * Default queue implementation
 * @author ameshkov
 */
public class DefaultQueue extends LinkedList implements Queue {

    public void dispose() {
        // Doing nothing
    }
}
