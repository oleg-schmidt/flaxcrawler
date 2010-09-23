package com.googlecode.flaxcrawler.frontier;

import com.googlecode.flaxcrawler.frontier.DefaultStatisticsService;
import com.sleepycat.je.DatabaseException;
import org.junit.Ignore;
import org.junit.Test;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import static org.junit.Assert.*;

/**
 *
 * @author ameshkov
 */
public class TestDefaultStatisticsService {

    @Test
    public void testAfterScheduling() throws DatabaseException {
        DefaultStatisticsService statisticsService = null;

        try {
            statisticsService = new DefaultStatisticsService("stats");


            CrawlerTask task = new CrawlerTask("http://google.com/", 1);
            statisticsService.afterScheduling(task);

            assertNotNull(statisticsService.getDomainStatistics("google.com"));
            assertTrue(statisticsService.isCrawled("http://google.com/"));
            assertTrue(statisticsService.getDomainStatistics("google.com").getScheduled() > 0);
        } finally {
            if (statisticsService != null) {
                statisticsService.dispose();
            }
        }
    }
}
