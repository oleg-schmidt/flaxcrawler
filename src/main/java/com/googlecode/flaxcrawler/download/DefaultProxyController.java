package com.googlecode.flaxcrawler.download;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of the proxy controller. Starts special monitoring thread used to check if proxies are alive.
 * Balances proxies, returning next proxy from the list after each request
 * @author ameshkov
 */
public class DefaultProxyController implements ProxyController {

    private List<Proxy> proxies;
    private int lastSelectedProxy = 0;

    /**
     * Creates an instance of the {@code DefaultProxyController}
     */
    public DefaultProxyController() {
    }

    /**
     * Creates an instance of the {@code DefaultProxyController}
     * @param proxies
     */
    public DefaultProxyController(Collection<? extends Proxy> proxies) {
        this();
        setProxies(proxies);
    }

    /**
     * Creates an instance of the {@code DefaultProxyController}.
     * @param proxies Key is proxy host, value is proxy port.
     */
    public DefaultProxyController(Map<String, Integer> proxies) {
        this();

        this.proxies = new ArrayList<Proxy>();

        for (Map.Entry<String, Integer> entry : proxies.entrySet()) {
            this.proxies.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(entry.getKey(), entry.getValue())));
        }
    }

    /**
     * Sets proxies list
     * @param proxies
     */
    public void setProxies(Collection<? extends Proxy> proxies) {
        synchronized (this) {
            this.proxies = new LinkedList<Proxy>(proxies);
            this.lastSelectedProxy = 0;
        }
    }

    /**
     * Returns proxies list
     * @return
     */
    public List<? extends Proxy> getProxies() {
        return proxies;
    }

    /**
     * Gets a proxy from the proxy list
     * @return
     */
    public Proxy getProxy() {
        synchronized (this) {
            if (proxies == null || proxies.size() == 0) {
                return null;
            }

            if (lastSelectedProxy == proxies.size() - 1) {
                lastSelectedProxy = 0;
            } else {
                lastSelectedProxy++;
            }

            Proxy proxy = proxies.get(lastSelectedProxy);
            return proxy;
        }
    }
}
