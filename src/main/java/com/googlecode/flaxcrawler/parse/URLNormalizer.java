package com.googlecode.flaxcrawler.parse;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import com.googlecode.flaxcrawler.utils.UrlUtils;

/**
 * Utility class for URL normalizing
 * @author ameshkov
 */
public class URLNormalizer {

    /**
     * Normalizes specified url
     * @param url
     * @param contextUrl
     * @return
     */
    public static URL normalize(String url, String contextUrl) {
        return normalize(url, contextUrl, "UTF-8");
    }

    /**
     * Normalizes specified url
     * @param url
     * @param contextUrl
     * @param charset Used for url encoding
     * @return
     */
    public static URL normalize(String url, String contextUrl, String charset) {
        try {
            return normalize(url, new URL(contextUrl), charset);
        } catch (Exception ex) {
            // Ignoring
            return null;
        }
    }

    public static URL normalize(String url, URL contextUrl, String charset) {
        URL normalized = null;

        try {
            normalized = new URL(url);
        } catch (MalformedURLException ex) {
            try {
                // Cannot be parsed as URL, trying to use context url
                normalized = new URL(contextUrl, url);
            } catch (MalformedURLException e) {
                // Ignoring exception
                return null;
            }
        }

        return encode(normalized, charset);
    }

    /**
     * Encodes url if needed. <br/>
     * TODO: Dirty, refactor this
     * @param url
     * @param charset Charset used for url encoding
     * @return
     */
    private static URL encode(URL url, String charset) {
        try {
            String encoded = url.toString();

            if (encoded.contains("#")) {
                encoded = encoded.substring(0, encoded.indexOf("#"));
            }
            encoded = encoded.replace(" ", "%20");
            return new URL(encoded);
        } catch (Exception ex) {
            return null;
        }
    }
}
