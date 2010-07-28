package com.googlecode.flaxcrawler;

import com.googlecode.flaxcrawler.DefaultCrawler;
import org.junit.Test;
import com.googlecode.flaxcrawler.download.DefaultDownloaderController;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import com.googlecode.flaxcrawler.parse.DefaultParserController;
import static org.junit.Assert.*;

/**
 *
 * @author ameshkov
 */
public class TestDefaultCrawler {

    @Test
    public void testDefaultCrawler() throws Exception {
        DefaultCrawler crawler = new DefaultCrawler();
        crawler.setDownloaderController(new DefaultDownloaderController());
        crawler.setParserController(new DefaultParserController());

        CrawlerTask crawlerTask = new CrawlerTask("http://lenta.ru/", 0);
        Page page = crawler.crawl(crawlerTask);

        assertNotNull(page);
        assertTrue(page.getLinks().size() > 0);
    }
}
