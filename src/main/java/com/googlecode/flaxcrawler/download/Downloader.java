package com.googlecode.flaxcrawler.download;

import java.net.URL;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Page downloader
 * @author ameshkov
 */
public interface Downloader {

    /**
     * Downloads specified page
     * @param url
     * @return
     */
    Page download(URL url) throws DownloadException;
}
