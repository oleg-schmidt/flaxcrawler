package com.googlecode.flaxcrawler.frontier;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import com.googlecode.flaxcrawler.model.CrawlerTask;
import com.googlecode.flaxcrawler.model.Page;
import java.util.HashMap;
import java.util.Map;

/**
 * Default statistics service implementation. BerkleyDB is used to store statistics.
 * @author ameshkov
 */
public class DefaultStatisticsService implements StatisticsService {

    private Logger log = Logger.getLogger(this.getClass());
    private Environment environment;
    private EntityStore statisticsStore;
    private PrimaryIndex<String, UrlElement> urlsIndex;
    private Map<String, DomainStatistics> statisticsMap = new HashMap<String, DomainStatistics>();
    private long scheduled = 0;
    private long downloaded = 0;
    private long parsed = 0;
    private long errors = 0;

    /**
     * Creates an instance of the {@code DefaultStatisticsService}
     * @param environmentFile
     */
    public DefaultStatisticsService(String environmentFile) throws DatabaseException {
        log.info("Initializing statistics storage...");

        EnvironmentConfig environmentConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        environmentConfig.setAllowCreate(true);
        environmentConfig.setTransactional(false);
        environmentConfig.setSharedCache(true);
        storeConfig.setAllowCreate(true);
        storeConfig.setTransactional(false);
        storeConfig.setTemporary(true);

        File envFile = new File(environmentFile);
        if (envFile.exists()) {
            log.info("Environment is already exists");
            try {
                FileUtils.cleanDirectory(envFile);
            } catch (Exception ex) {
                log.fatal("Error cleaning directory " + envFile, ex);
                throw new RuntimeException("Error cleaning directory " + envFile);
            }
        }
        envFile.mkdirs();

        log.info("Environment file is " + envFile.getAbsolutePath());

        environment = new Environment(envFile, environmentConfig);
        statisticsStore = new EntityStore(environment, "BerkleyQueueStore", storeConfig);
        urlsIndex = statisticsStore.getPrimaryIndex(String.class, UrlElement.class);
        log.info("Environment successfully initialized");
    }

    /**
     * Disposes statistics store
     */
    public void dispose() {
        synchronized (this) {
            try {
                statisticsStore.close();
                environment.close();

                log.info("Environment closed successfully");
            } catch (DatabaseException ex) {
                log.error("Error while closing environment", ex);
            }
        }
    }

    public boolean isCrawled(String url) {
        synchronized (this) {
            try {
                return urlsIndex.contains(url);
            } catch (DatabaseException ex) {
                log.warn("Error checking if url " + url + " is in berkley db index", ex);
                return false;
            }
        }
    }

    public void afterScheduling(CrawlerTask task) {
        synchronized (this) {
            try {
                urlsIndex.put(new UrlElement(task.getUrl()));
            } catch (DatabaseException ex) {
                log.warn("Error inserting " + task.getUrl() + " in the berkley db index", ex);
            }

            updateDomainStatistics(task.getDomain(), 1, 0, 0, 0, 0);
            scheduled++;
        }
    }

    public void afterDownloading(CrawlerTask task, Page page) {
        synchronized (this) {
            long errorsCount = page.getResponseCode() >= 400 ? 1 : 0;
            long downloadedCount = errorsCount > 0 ? 0 : 1;

            errors += errorsCount;
            downloaded += downloadedCount;

            updateDomainStatistics(task.getDomain(), 0, downloadedCount, 0, errorsCount, page.getResponseCode());
        }
    }

    public void afterParsing(CrawlerTask task, Page page) {
        synchronized (this) {
            parsed++;
            updateDomainStatistics(task.getDomain(), 0, 0, 1, 0, 0);
        }
    }

    public long getScheduled() {
        return scheduled;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public long getParsed() {
        return parsed;
    }

    public long getErrors() {
        return errors;
    }

    public DomainStatistics getDomainStatistics(String domainName) {
        synchronized (this) {
            DomainStatistics domainStatistics = statisticsMap.get(domainName);

            if (domainStatistics == null) {
                domainStatistics = new DomainStatistics(domainName);
                statisticsMap.put(domainName, domainStatistics);
            }

            return domainStatistics;
        }
    }

    /**
     * Updates domain statistics
     * @param domainName
     * @param schedulled
     * @param parsed
     * @param errors
     * @param responseCode
     */
    private void updateDomainStatistics(String domainName, long schedulled, long downloaded, long parsed, long errors, int responseCode) {
        synchronized (this) {
            DomainStatistics domainStatistics = getDomainStatistics(domainName);

            domainStatistics.setScheduled(domainStatistics.getScheduled() + schedulled);
            domainStatistics.setDownloaded(domainStatistics.getDownloaded() + downloaded);
            domainStatistics.setParsed(domainStatistics.getParsed() + parsed);
            domainStatistics.setErrors(domainStatistics.getErrors() + errors);
            domainStatistics.setLastTimeDownloaded(System.currentTimeMillis());

            if (errors > 0) {
                domainStatistics.addHttpError(responseCode);
            }
        }
    }
}
