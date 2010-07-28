package com.googlecode.flaxcrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * Represents crawler configuration.
 * @author ameshkov
 */
public class CrawlerConfiguration {

    private Logger log = Logger.getLogger(this.getClass());
    private int maxParallelRequests;
    private int maxLevel;
    private int politenessPeriod;
    private Map<Integer, Integer> maxHttpErrors = new HashMap<Integer, Integer>();
    private List<Crawler> crawlers = new ArrayList<Crawler>();
    private Map<String, DomainConstraints> domainConstraints = new HashMap<String, DomainConstraints>();

    public CrawlerConfiguration() {
    }

    /**
     * Returns maximum parallel requests to a single domain limit.  0 - unlimited. By default - 0.
     * @return
     */
    public int getMaxParallelRequests() {
        return maxParallelRequests;
    }

    /**
     * Sets maximum parallel requests to a single domain limit. 0 - unlimited. By default - 0.
     * @param maxParallelRequests
     */
    public void setMaxParallelRequests(int maxParallelRequests) {
        this.maxParallelRequests = maxParallelRequests;
    }

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

    /**
     * Returns http errors limits
     * @return
     */
    public Map<Integer, Integer> getMaxHttpErrors() {
        return maxHttpErrors;
    }

    /**
     * Sets http errors limits
     * @param maxHttpErrors
     */
    public void setMaxHttpErrors(Map<Integer, Integer> maxHttpErrors) {
        this.maxHttpErrors = maxHttpErrors;
    }

    /**
     * Sets maximum http errors for a single domain. If this limit is exceeded - domain will not be crawled any more.
     * @param maxHttpErrors
     */
    public void setMaxHttpErrors(int responseCode, int maxHttpErrors) {
        Integer count = this.maxHttpErrors.get(responseCode);

        if (count == null) {
            count = 0;
        }

        this.maxHttpErrors.put(responseCode, count + 1);
    }

    /**
     * Sets specific constraints for some domains
     * @param domainConstraints
     */
    public void setDomainConstraints(Map<String, DomainConstraints> domainConstraints) {
        this.domainConstraints = domainConstraints;
    }

    /**
     * Sets specific constraints for domain {@code domainName}
     * @param domainName
     * @param domainConstraints
     */
    public void setDomainConstraints(String domainName, DomainConstraints domainConstraints) {
        this.domainConstraints.put(domainName, domainConstraints);
    }

    /**
     * Returns domain constraints for {@code domainName}. If specific constraints for this domain
     * are not set - returns {@code null}.
     * @param domainName
     * @return
     */
    public DomainConstraints getDomainConstraints(String domainName) {
        return domainConstraints.get(domainName);
    }

    /**
     * Returns crawlers
     * @return
     */
    public List<Crawler> getCrawlers() {
        return crawlers;
    }

    /**
     * Sets crawlers
     * @param crawlers
     */
    public void setCrawlers(List<Crawler> crawlers) {
        this.crawlers = crawlers;
    }

    /**
     * Adds crawler
     * @param crawler
     */
    public void addCrawler(Crawler crawler) {
        crawlers.add(crawler);
    }
}
