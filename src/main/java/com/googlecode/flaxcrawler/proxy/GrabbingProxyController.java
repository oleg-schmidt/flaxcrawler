package com.googlecode.flaxcrawler.proxy;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import com.googlecode.flaxcrawler.download.DefaultProxyController;
import com.googlecode.flaxcrawler.utils.UrlUtils;

/**
 * Proxy controller implementation that periodically reloads proxies from the specified urls.
 * <b>Attention:</b> {@code GrabbingProxyController} initializes for a long time (because of initial proxy check).
 * @author ameshkov
 */
public class GrabbingProxyController extends DefaultProxyController {

    private final static int GRAB_PERIOD = 10000;
    private Logger log = Logger.getLogger(this.getClass());
    private Timer grabberTimer;
    private Collection<? extends URL> urlsToGrab;
    private URL proxyCheckUrl;

    /**
     * Creates an instance of the {@code GrabbingProxyController} class.
     * @param urlsToGrab Urls used to grab proxies
     * @param proxyCheckUrl Url used to check if proxy is alive (comparing content downloaded with and without proxy)
     */
    public GrabbingProxyController(Collection<? extends URL> urlsToGrab, URL proxyCheckUrl) {
        super();

        log.info("Initializing GrabbingProxyController with " + urlsToGrab.size() + " proxy source urls");
        this.urlsToGrab = urlsToGrab;
        this.proxyCheckUrl = proxyCheckUrl;

        // Initial load
        grabProxies();

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                grabProxies();
            }
        };
        grabberTimer = new Timer();
        grabberTimer.schedule(timerTask, GRAB_PERIOD);
    }

    /**
     * Creates an instance of the {@code GrabbingProxyController} class.
     * @param urlsToGrab Urls used to grab proxies
     * @param proxyCheckUrl Url used to check if proxy is alive (comparing content downloaded with and without proxy)
     */
    public GrabbingProxyController(Collection<? extends String> urlsToGrab, String proxyCheckUrl) throws MalformedURLException {
        this(toUrlsList(urlsToGrab), new URL(proxyCheckUrl));
    }

    /**
     * Converts list of strings to list of URLs
     * @param urls
     * @return
     * @throws MalformedURLException
     */
    private static List<URL> toUrlsList(Collection<? extends String> urls) throws MalformedURLException {
        List<URL> urlsList = new ArrayList<URL>();
        for (String url : urls) {
            urlsList.add(new URL(url));
        }
        return urlsList;
    }

    /**
     * Loads proxies from specified urls
     */
    protected void grabProxies() {
        log.info("Grabbing proxies");
        List<Proxy> proxies = ProxyGrabber.grab(urlsToGrab);
        log.info(proxies.size() + " proxies found");
        List<Proxy> alive = checkProxies(proxies, proxyCheckUrl);
        log.info(alive.size() + " proxies are alive");
        setProxies(proxies);
    }

    /**
     * Checks what proxies are alive and returns them.
     * @param proxies
     * @param proxyCheckUrl
     * @return
     */
    protected List<Proxy> checkProxies(List<Proxy> proxies, URL proxyCheckUrl) {
        log.debug("Checking proxies...");

        List<Proxy> aliveProxies = new ArrayList<Proxy>();

        log.debug("Downloading page standard...");

        // Downloading page standard
        String pageStandard = UrlUtils.downloadString(proxyCheckUrl, null);

        // Checking proxies
        for (Proxy proxy : proxies) {
            log.debug("Checking proxy " + proxy);
            String pageWithProxy = UrlUtils.downloadString(proxyCheckUrl, proxy);

            if (pageWithProxy != null && pageWithProxy.equals(pageStandard)) {
                log.debug("Proxy " + proxy + " is alive");
                aliveProxies.add(proxy);
            } else {
                log.debug("Proxy " + proxy + " is dead");
            }
        }

        return aliveProxies;
    }
}
