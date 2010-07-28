package com.googlecode.flaxcrawler;

/**
 * Represents crawler constraints for a single domain
 * @author ameshkov
 */
public class DomainConstraints {

    private int maxLevel;
    private int maxParallelRequests;
    private int politenessPeriod;

    /**
     * Returns maximum crawling depth limit. 0 - unlimited. By default - 0.
     * @return
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Sets maximum crawling depth limit. 0 - unlimited. By default - 0.
     * @param maxLevel
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * Returns maximum parallel requests to a single domain limit.  0 - unlimited. By default - 0.
     * @return
     */
    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    /**
     * Sets maximum parallel requests to a single domain limit. 0 - unlimited. By default - 0.
     * @param maxParallelRequests
     */
    public void setMaxParallelRequests(int maxParallelRequests) {
        this.maxParallelRequests = maxParallelRequests;
    }

    /**
     * Returns a delay between two requests to a single domain (ms).  0 - unlimited. By default - 0.
     * @return
     */
    public int getPolitenessPeriod() {
        return politenessPeriod;
    }

    /**
     * Sets a delay between two requests to a single domain (ms).  0 - unlimited. By default - 0.
     * @param politenessPeriod
     */
    public void setPolitenessPeriod(int politenessPeriod) {
        this.politenessPeriod = politenessPeriod;
    }
}
