package com.googlecode.flaxcrawler;

/**
 *
 * @author ameshkov
 */
public class CrawlerException extends Exception {

    public CrawlerException() {
        super();
    }

    public CrawlerException(String message) {
        super(message);
    }

    public CrawlerException(Throwable cause) {
        super(cause);
    }

    public CrawlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
