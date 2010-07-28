package com.googlecode.flaxcrawler.download;

import org.junit.Ignore;
import org.junit.Test;
import com.googlecode.flaxcrawler.parse.URLNormalizer;

/**
 *
 * @author ameshkov
 */
public class TestURLNormalizer {

    @Test
    @Ignore
    public void testUrlNormalizer() {

        String[] urls = new String[]{"http://vipzone.ws/index.php?do=los tpassword",
            "/templates/vipzone/images/favicon.ico", "/securitypc/", "2010/07/02/",
            "/soft/69259-scramby-v2.0.40.0.html", "http://vipzone.ws/tags/%CF%F0%EE%E3%F0%E0%EC%EC%E0/"};
        String contextUrl = "http://vipzone.ws/test/";

        for (String url : urls) {
            System.out.println(URLNormalizer.normalize(url, contextUrl));
        }
    }
}
