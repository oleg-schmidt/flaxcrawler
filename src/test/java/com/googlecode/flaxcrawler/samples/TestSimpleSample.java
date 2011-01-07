package com.googlecode.flaxcrawler.samples;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.CrawlerController;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import org.junit.Test;

/**
 * Simple sample of flax crawler usage
 */
public class TestSimpleSample {

    @Test
    public void testStartSimpleSample() throws MalformedURLException, CrawlerException {
        main(null);
    }

    public static void main(String[] args) throws MalformedURLException, CrawlerException {
        // Setting up downloader controller
        DefaultDownloaderController downloaderController = new DefaultDownloaderController();
        // Setting up parser controller
        DefaultParserController parserController = new DefaultParserController();

        // Creating crawler configuration object
        CrawlerConfiguration configuration = new CrawlerConfiguration();

        // Creating five crawlers (to work with 5 threads)
        for (int i = 0; i < 5; i++) {
            // Creating crawler and setting downloader and parser controllers
            DefaultCrawler crawler = new ExampleCrawler();
            crawler.setDownloaderController(downloaderController);
            crawler.setParserController(parserController);
            // Adding crawler to the configuration object
            configuration.addCrawler(crawler);
        }

        // Setting maximum parallel requests to a single site limit
        configuration.setMaxParallelRequests(1);
        // Setting http errors limits. If this limit violated for any
        // site - crawler will stop this site processing
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_CLIENT_TIMEOUT, 10);
        configuration.setMaxHttpErrors(HttpURLConnection.HTTP_BAD_GATEWAY, 10);
        // Setting period between two requests to a single site (in milliseconds)
        configuration.setPolitenessPeriod(500);

        // Initializing crawler controller
        CrawlerController crawlerController = new CrawlerController(configuration);
        // Adding crawler seed
        crawlerController.addSeed(new URL("http://en.wikipedia.org/"));
        // Starting and joining our crawler
        crawlerController.start();
        System.out.println(new Date() + "Waiting for 60 seconds");
        crawlerController.join(60000);
        System.out.println(new Date() + "Stopping crawler");
        crawlerController.stop();
    }

    /**
     * Custom crawler. Extends {@link DefaultCrawler}.
     */
    private static class ExampleCrawler extends DefaultCrawler {

        /**
         * This method is called after each crawl attempt.
         * Warning - it does not matter if it was unsuccessfull attempt or response was redirected.
         * So you should check response code before handling it.
         * @param crawlerTask
         * @param page
         */
        @Override
        protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
            super.afterCrawl(crawlerTask, page);

            if (page == null) {
                System.out.println(crawlerTask.getUrl() + " violates crawler constraints (content-type or content-length or other)");
            } else if (page.getResponseCode() >= 300 && page.getResponseCode() < 400) {
                // If response is redirected - crawler schedulles new task with new url
                System.out.println("Response was redirected from " + crawlerTask.getUrl());
            } else if (page.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Printing url crawled
                System.out.println(crawlerTask.getUrl() + ". Found " + (page.getLinks() != null ? page.getLinks().size() : 0) + " links.");
            }
        }

        /**
         * You may check if you want to crawl next task
         * @param crawlerTask Task that is going to be crawled if you return {@code true}
         * @param parent parent.getUrl() page contain link to a crawlerTask.getUrl() or redirects to it
         * @return
         */
        @Override
        public boolean shouldCrawl(CrawlerTask crawlerTask, CrawlerTask parent) {
            // Default implementation returns true if crawlerTask.getDomainName() == parent.getDomainName()
            return super.shouldCrawl(crawlerTask, parent);
        }
    }
}
