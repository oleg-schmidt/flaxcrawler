package com.googlecode.flaxcrawler.download;

import java.net.URL;

/**
 * Manages downloaders
 * @author ameshkov
 */
public interface DownloaderController {

    /**
     * Returns {@link Downloader} for the specified url
     * @param url
     * @return
     */
    Downloader getDownloader(URL url);
}
