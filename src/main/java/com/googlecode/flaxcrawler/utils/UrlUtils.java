package com.googlecode.flaxcrawler.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Helper for working with URLs (extract keywords, extract domain name)
 */
public class UrlUtils {

    public final static int DEFAULT_READ_TIMEOUT = 10000;
    public final static int DEFAULT_SOCKET_TIMEOUT = 10000;

    /**
     * Removes jsessionid from string
     * @param value
     * @return
     */
    public static String removeJSessionId(String str) {
        // Removing jsessionid
        if (!StringUtils.isEmpty(str) && StringUtils.contains(str.toLowerCase(), ";jsessionid")) {
            String value = str.substring(0, StringUtils.indexOf(str, ";jsessionid"));
            return value;
        }

        return str;
    }

    /**
     * Gets parameter value from the url.
     * If tryRewrited is true - tries to parse rewrited url using getRewritedParameter method
     * @param url
     * @param parameterName
     * @param tryRewrited
     * @return
     */
    public static String getParameter(String url, String parameterName, boolean tryRewrited) {
        String parameterValue = getParameter(url, parameterName);

        if (StringUtils.isEmpty(parameterValue) && tryRewrited) {
            parameterValue = getRewritedParameter(url, parameterName);
        }

        return parameterValue;
    }

    /**
     * Gets parameter value from the url
     * @param url
     * @param parameterName
     * @return
     */
    public static String getParameter(String url, String parameterName) {
        Map<String, String> parameters = getParameters(url);
        return parameters == null ? null : parameters.get(parameterName);
    }

    /**
     * For rewrited urls like 'http://test.com/q/search%20terms/anotherparameter/anotherparametervalue'
     * @param url
     * @param parameterName
     * @return
     */
    public static String getRewritedParameter(String url, String parameterName) {

        String[] parts = StringUtils.splitPreserveAllTokens(url, '/');

        if (parts == null || parts.length < 2) {
            return null;
        }

        String parameterValue = null;

        // Searching for the specified parameter name in url
        for (int i = 0; i < parts.length; i++) {
            if (parameterName.equals(parts[i])) {
                if (i < (parts.length - 1)) {
                    // parameter name found, breaking loop
                    parameterValue = parts[i + 1];
                    break;
                }
            }
        }

        // Removing jsessionid
        parameterValue = removeJSessionId(parameterValue);

        return parameterValue;
    }

    /**
     * Extracts domain name from the url. Also crops www.
     * @param url
     * @return
     */
    public static String getDomainName(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        try {
            return getDomainName(new URL(url));
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * Extracts domain name from the url. Also crops www.
     * @param url
     * @return
     */
    public static String getDomainName(URL url) {
        return StringUtils.lowerCase(cropWww(url.getHost()));
    }

    /**
     * Crops www. prefix from the domain name
     * @param domainName
     * @return
     */
    public static String cropWww(String domainName) {
        if (StringUtils.isEmpty(domainName)) {
            return null;
        }
        if (domainName.startsWith("www.")) {
            return domainName.substring(4);
        }

        return domainName;
    }

    /**
     * Adds parameter to a query string
     * @param url
     * @param parameterName
     * @param parameterValue
     */
    public static String addParameter(String url, String parameterName, String parameterValue) {
        try {
            if (url == null) {
                return null;
            }

            StringBuffer targetUrl = new StringBuffer();
            targetUrl.append(url);

            URL testURL = new URL(url);
            if (testURL.getQuery() != null) {
                targetUrl.append("&");
            } else if ("".equals(testURL.getPath())) {
                targetUrl.append("/?");
            } else {
                targetUrl.append("?");
            }

            targetUrl.append(parameterName);
            targetUrl.append("=");
            targetUrl.append(parameterValue);

            return targetUrl.toString();
        } catch (MalformedURLException ex) {
            return url;
        }
    }

    /**
     * Gets parameters from query string
     * @param url
     * @return
     */
    public static Map<String, String> getParameters(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        int index = url.indexOf("?");

        if (index == -1) {
            return null;
        }

        String query = url.substring(index + 1);
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] pair = param.split("=");

            if (pair.length == 2) {
                String name = pair[0];
                String value = pair[1];
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * Downloads content from the specified url using specified proxy (or do not using it).
     * Returns null if there's an error.
     * @param url
     * @param proxy
     */
    public static String downloadString(URL url, Proxy proxy) {
        return downloadString(url, proxy, DEFAULT_READ_TIMEOUT, DEFAULT_SOCKET_TIMEOUT);
    }

    /**
     * Downloads content from the specified url using specified proxy (or do not using it) and timeouts.
     * Returns null if there's an error.
     * @param url
     * @param proxy
     * @param readTimeout
     * @param socketTimeout
     * @return
     */
    public static String downloadString(URL url, Proxy proxy, int readTimeout, int socketTimeout) {
        URLConnection connection = null;
        InputStream inputStream = null;

        try {
            connection = proxy == null ? url.openConnection() : url.openConnection(proxy);
            connection.setReadTimeout(readTimeout);
            connection.setConnectTimeout(socketTimeout);

            connection.connect();
            inputStream = connection.getInputStream();
            return IOUtils.toString(inputStream);
        } catch (IOException ex) {
            // Ignoring exception
            return null;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                // Ignore
            }

            if (connection != null && connection instanceof HttpURLConnection) {
                ((HttpURLConnection) connection).disconnect();
            }
        }
    }
}
