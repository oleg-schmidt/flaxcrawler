package com.googlecode.flaxcrawler;

import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Represents crawler
 * @author ameshkov
 */
public interface Crawler {

    /**
     * Crawls url from the {@link CrawlerTask}. Returns parsed {@link Page}.
     * @param crawlerTask
     * @return
     * @throws Exception
     */
    Page crawl(CrawlerTask crawlerTask) throws Exception;

    /**
     * Checks if this crawler should process next task
     * @param crawlerTask
     * @param parent
     * @return
     */
    boolean shouldCrawl(CrawlerTask crawlerTask, CrawlerTask parent);
}
