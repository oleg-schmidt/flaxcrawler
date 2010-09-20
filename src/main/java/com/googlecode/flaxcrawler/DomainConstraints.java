package com.googlecode.flaxcrawler;

/**
 * Represents crawler constraints for a single domain
 * @author ameshkov
 */
public class DomainConstraints {

    private int maxLevel;
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
