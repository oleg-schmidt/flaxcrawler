package com.googlecode.flaxcrawler.download;

import com.googlecode.flaxcrawler.download.DownloadException;
import com.googlecode.flaxcrawler.download.DefaultProxyController;
import com.googlecode.flaxcrawler.download.LoginDownloader;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import static org.junit.Assert.*;
import org.junit.Test;
import com.googlecode.flaxcrawler.model.Page;

public class TestLoginDownloader {

    @Test
    @Ignore
    public void testLoginDownloader() throws DownloadException, MalformedURLException {
        List<Proxy> proxies = new ArrayList<Proxy>();
        proxies.add(Proxy.NO_PROXY);

        DefaultProxyController proxyController = new DefaultProxyController();
        proxyController.setProxies(proxies);

        LoginDownloader downloader = new LoginDownloader();
        downloader.setLoginUrl("http://qiq.ru/?action=login");
        downloader.setPostData("login=lex9889&pass=qweasdzxc");
        downloader.setProxyController(proxyController);
        downloader.setTriesCount(3);

        Page page = downloader.download(new URL("http://qiq.ru/"));
        assertNotNull(page);

        System.out.println("Response time: " + page.getResponseTime());
        System.out.println("Content length: " + page.getContent().length);
        System.out.println("Response code: " + page.getResponseCode());
        System.out.println("Content charset: " + page.getCharset());
        System.out.println("Content encoding: " + page.getContentEncoding());

    }
}
