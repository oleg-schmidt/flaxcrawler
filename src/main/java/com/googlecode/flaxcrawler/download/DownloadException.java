package com.googlecode.flaxcrawler.download;

import java.io.IOException;
import java.net.URL;

/**
 * Downloader exception
 * @author ameshkov
 */
public class DownloadException extends IOException {

    private URL url;

    /**
     * Creates an instance of the {@code DownloadException}
     */
    public DownloadException() {
    }

    /**
     * Creates an instance of the {@code DownloadException}
     * @param message
     */
    public DownloadException(String message) {
        super(message);
    }

    /**
     * Creates an instance of the {@code DownloadException}
     * @param cause
     */
    public DownloadException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an instance of the {@code DownloadException}
     * @param message
     * @param cause
     */
    public DownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an instance of the {@code DownloadException}
     * @param message
     * @param url
     */
    public DownloadException(String message, URL url) {
        super(message);
        this.url = url;
    }

    /**
     * Creates an instance of the {@code DownloadException}
     * @param message
     * @param cause
     * @param url
     */
    public DownloadException(String message, Throwable cause, URL url) {
        super(message, cause);
        this.url = url;
    }

    /**
     * Returns url being downloaded
     * @return
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets url being downloaded
     * @param url
     */
    public void setUrl(URL url) {
        this.url = url;
    }
}
