package com.googlecode.flaxcrawler.model;

import com.googlecode.flaxcrawler.concurrent.BaseTask;
import com.sleepycat.persist.model.Persistent;
import java.util.Date;
import com.googlecode.flaxcrawler.utils.UrlUtils;

/**
 * Represents crawler task
 * @author ameshkov
 */
@Persistent
public class CrawlerTask extends BaseTask {

    private String url;
    private String domain;
    private int level;
    private Date timeAdded;
    private Date timeDownloaded;
    private Date timeParsed;
    private Object customData;

    /**
     * Default constructor
     */
    public CrawlerTask() {
    }

    /**
     * Creates an instance of the CrawlerTask class
     * @param url
     * @param level
     */
    public CrawlerTask(String url, int level) {
        this.url = url;
        this.domain = UrlUtils.getDomainName(url);
        this.level = level;
        this.timeAdded = new Date();
    }

    /**
     * Task creation time
     * @return
     */
    public Date getTimeAdded() {
        return timeAdded;
    }

    /**
     * Returns task's url
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * Returns task's domain
     * @return
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Returns task's nested level
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * Gets time this task's url was downloaded
     * @return
     */
    public Date getTimeDownloaded() {
        return timeDownloaded;
    }

    /**
     * Sets time this task was downloaded
     * @param timeDownloaded
     */
    public void setTimeDownloaded(Date timeDownloaded) {
        this.timeDownloaded = timeDownloaded;
    }

    /**
     * Gets time this task was parsed
     * @return
     */
    public Date getTimeParsed() {
        return timeParsed;
    }

    /**
     * Sets time this task was parsed
     * @param timeParsed
     */
    public void setTimeParsed(Date timeParsed) {
        this.timeParsed = timeParsed;
    }

    /**
     * Crawlers can set additional custom data
     * @return
     */
    public Object getCustomData() {
        return customData;
    }

    /**
     * Sets custom data object
     * @param customData
     */
    public void setCustomData(Object customData) {
        this.customData = customData;
    }
}
