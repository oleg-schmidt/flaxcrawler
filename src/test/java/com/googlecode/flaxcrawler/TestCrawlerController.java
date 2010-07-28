package com.googlecode.flaxcrawler;

import com.googlecode.flaxcrawler.CrawlerConfiguration;
import com.googlecode.flaxcrawler.DefaultCrawler;
import com.googlecode.flaxcrawler.CrawlerException;
import com.googlecode.flaxcrawler.CrawlerController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;

/**
 *
 * @author ameshkov
 */
public class TestCrawlerController {

    private int crawledCount = 0;
    private File output = new File("output.txt");
    private FileOutputStream out;

    @Test
    @Ignore
    public void testCrawlerController() throws MalformedURLException, CrawlerException, FileNotFoundException {

        out = new FileOutputStream(output, true);

        CrawlerConfiguration crawlerConfiguration = new CrawlerConfiguration();
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.addCrawler(new TestCrawler());
        crawlerConfiguration.setPolitenessPeriod(1000);
        crawlerConfiguration.setMaxParallelRequests(1);
        CrawlerController crawlerController = new CrawlerController(crawlerConfiguration);
        crawlerController.addSeed(new URL("http://lenta.ru/"));
        crawlerController.start();
        crawlerController.join();
    }

    private class TestCrawler extends DefaultCrawler {

        public TestCrawler() {
            setDownloaderController(new DefaultDownloaderController());
            setParserController(new DefaultParserController());
        }

        @Override
        protected void afterCrawl(CrawlerTask crawlerTask, Page page) {
            super.afterCrawl(crawlerTask, page);

            synchronized (TestCrawlerController.this) {
                try {
                    String message = ++crawledCount + ". " + DateFormat.getTimeInstance().format(new Date()) + " " + crawlerTask.getUrl() + ", response = " + page.getResponseCode() + ", links found " + (page.getLinks() == null ? 0 : page.getLinks().size()) + "\r\n";
                    IOUtils.write(message, out);
                } catch (Exception ex) {
                }
            }
        }
    }
}
