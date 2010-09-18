package com.googlecode.flaxcrawler.frontier;

import com.googlecode.flaxcrawler.model.CrawlerTask;

/**
 * Schedules page crawl
 */
public interface Scheduler {

    /**
     * Schedulles crawling of page {@code url}
     * @param url
     */
    void schedule(CrawlerTask crawlerTask);
}
