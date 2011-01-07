package com.googlecode.flaxcrawler.download;

import org.junit.Ignore;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.googlecode.flaxcrawler.model.Page;
import static org.junit.Assert.*;

/**
 *
 */
public class TestDefaultDownloader {

    @Test
    @Ignore
    public void testDownload() throws MalformedURLException, DownloadException {
        List<Proxy> proxies = new ArrayList<Proxy>();
        proxies.add(Proxy.NO_PROXY);

        DefaultProxyController proxyController = new DefaultProxyController();
        proxyController.setProxies(proxies);

        DefaultDownloader downloader = new DefaultDownloader();
        downloader.setProxyController(proxyController);
        downloader.setTriesCount(3);

        Page page = downloader.download(new URL("http://www.wikipedia.org"));
        assertNotNull(page);

        System.out.println("Response time: " + page.getResponseTime());
        System.out.println("Content length: " + page.getContent().length);
        System.out.println("Response code: " + page.getResponseCode());
        System.out.println("Content charset: " + page.getCharset());
        System.out.println("Content encoding: " + page.getContentEncoding());
    }
}
