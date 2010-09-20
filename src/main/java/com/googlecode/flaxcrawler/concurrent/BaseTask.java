package com.googlecode.flaxcrawler.concurrent;

import com.sleepycat.persist.model.Persistent;

/**
 *  abstract Task implementation. {@code getSequenceName} returns {@code null}.
 */
@Persistent
public abstract class BaseTask implements Task {

    public String getSequenceName() {
        return null;
    }
}
