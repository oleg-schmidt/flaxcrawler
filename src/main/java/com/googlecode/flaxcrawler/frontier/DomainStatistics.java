package com.googlecode.flaxcrawler.frontier;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents domain statistics
 * @author ameshkov
 */
public class DomainStatistics {

    private String domainName;
    private long scheduled;
    private long downloaded;
    private long parsed;
    private long errors;
    private long lastTimeDownloaded;
    private Map<Integer, Long> httpErrors = new HashMap<Integer, Long>();

    public DomainStatistics() {
    }

    public DomainStatistics(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Returns domain name
     * @return
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * Sets domain name
     * @param domainName
     */
    void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * Returns scheduled tasks count
     * @return
     */
    public long getScheduled() {
        return scheduled;
    }

    /**
     * Sets scheduled tasks count
     * @param scheduled
     */
    void setScheduled(long scheduled) {
        this.scheduled = scheduled;
    }

    /**
     * Returns downloaded pages count
     * @return
     */
    public long getDownloaded() {
        return downloaded;
    }

    /**
     * Sets downloaded pages count
     * @param downloaded
     */
    void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    /**
     * Returns parsed pages count
     * @return
     */
    public long getParsed() {
        return parsed;
    }

    /**
     * Sets parsed pages count
     * @param parsed
     */
    void setParsed(long parsed) {
        this.parsed = parsed;
    }

    /**
     * Returns last time url from this domain was downloaded
     * @return
     */
    public long getLastTimeDownloaded() {
        return lastTimeDownloaded;
    }

    /**
     * Sets last time url from this domain was downloaded
     * @param lastTimeDownloaded
     */
    void setLastTimeDownloaded(long lastTimeDownloaded) {
        this.lastTimeDownloaded = lastTimeDownloaded;
    }

    /**
     * Returns errors count
     * @return
     */
    public long getErrors() {
        return errors;
    }

    /**
     * Sets errors count
     * @param errors
     */
    void setErrors(long errors) {
        this.errors = errors;
    }

    /**
     * Adds http error
     * @param code
     */
    synchronized void addHttpError(int code) {
        Long count = httpErrors.get(code);

        if (count == null) {
            httpErrors.put(code, new Long(1));
        } else {
            httpErrors.put(code, count + 1);
        }
    }

    /**
     * Returns http errors count
     * @param code
     * @return
     */
    public long getHttpErrors(int code) {
        Long count = httpErrors.get(code);

        return count == null ? 0 : count.longValue();
    }
}
