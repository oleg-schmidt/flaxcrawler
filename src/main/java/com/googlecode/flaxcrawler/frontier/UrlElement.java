package com.googlecode.flaxcrawler.frontier;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Helper class for storing crawling history in berkley db
 * @author ameshkov
 */
@Entity
public class UrlElement {

    public UrlElement() {
    }

    public UrlElement(String url) {
        this.url = url;
    }
    @PrimaryKey
    private String url;
}
