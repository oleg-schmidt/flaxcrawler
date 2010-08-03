package com.googlecode.flaxcrawler.proxy;

import com.googlecode.flaxcrawler.utils.UrlUtils;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing proxy lists
 */
public class ProxyGrabber {

    private static Map<String, Pattern> grabberPatterns = new HashMap<String, Pattern>();

    static {
        // Initializing default grabber pattern
        addGrabberPattern("default", Pattern.compile("([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\s{0,2}:\\s{0,2}([0-9]{1,6})"));
    }

    /**
     * Adds pattern for parsing proxy from the html page. Regexp should contain five groups in following order: (1).(2).(3).(4):(5).
     * @param domainName
     * @param pattern
     */
    public static void addGrabberPattern(String domainName, Pattern pattern) {
        grabberPatterns.put(domainName, pattern);
    }

    /**
     * Parses proxies from the specified list of urls
     * @param urls
     * @return
     */
    public static List<Proxy> grab(Collection<? extends URL> urls) {

        List<Proxy> proxys = new ArrayList<Proxy>();

        for (URL url : urls) {
            try {
                String responseBody = UrlUtils.downloadString(url, Proxy.NO_PROXY);

                List<Proxy> parsed = parse(responseBody, url.getHost());

                for (Proxy proxy : parsed) {
                    if (!proxys.contains(proxy)) {
                        proxys.add(proxy);
                    }
                }
            } catch (Exception ex) {
                // Ignore
            }
        }

        return proxys;
    }

    private static List<Proxy> parse(String text, String host) {
        List<Proxy> proxys = new ArrayList<Proxy>();
        Pattern pattern = getGrabberPattern(host);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String ip = matcher.group(1) + "." + matcher.group(2) + "." + matcher.group(3) + "." + matcher.group(4);
            Integer port = Integer.valueOf(matcher.group(5));

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
            if (!proxys.contains(proxy)) {
                proxys.add(proxy);
            }
        }

        return proxys;
    }

    private static Pattern getGrabberPattern(String host) {
        Pattern pattern = grabberPatterns.get(host);

        if (pattern == null) {
            pattern = grabberPatterns.get("default");
        }

        return pattern;
    }
}
