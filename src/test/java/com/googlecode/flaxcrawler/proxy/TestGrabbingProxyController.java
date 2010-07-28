package com.googlecode.flaxcrawler.proxy;

import com.googlecode.flaxcrawler.proxy.GrabbingProxyController;
import com.googlecode.flaxcrawler.proxy.ProxyGrabber;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ameshkov
 */
public class TestGrabbingProxyController {

    @Test
    @Ignore
    public void testGrabbing() throws MalformedURLException {
        ProxyGrabber.addGrabberPattern("www.proxyforest.com", Pattern.compile("proxy\\([0-9]*,'([0-9]{1,3})','([0-9]{1,3})','([0-9]{1,3})','([0-9]{1,3})',([0-9]{1,6})\\);"));
        ProxyGrabber.addGrabberPattern("www.xroxy.com", Pattern.compile("<a href='proxy:.*?host=([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3}).*?port=([0-9]{1,6})"));

        List<URL> urlsToGrab = new ArrayList<URL>();
        urlsToGrab.add(new URL("http://www.proxyforest.com/proxy.htm?pages=0"));
        urlsToGrab.add(new URL("http://www.xroxy.com/proxylist.htm"));
        urlsToGrab.add(new URL("http://www.xroxy.com/proxylist.php?pnum=1"));
        urlsToGrab.add(new URL("http://www.xroxy.com/proxylist.php?pnum=2"));
        urlsToGrab.add(new URL("http://www.xroxy.com/proxylist.php?pnum=3"));

        GrabbingProxyController proxyController = new GrabbingProxyController(urlsToGrab, new URL("http://ya.ru/"));
        assertTrue(proxyController.getProxies().size() > 0);
        System.out.println("Found " + proxyController.getProxies().size() + " alive proxies");
    }
}
