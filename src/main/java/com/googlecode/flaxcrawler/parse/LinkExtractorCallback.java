package com.googlecode.flaxcrawler.parse;

import it.unimi.dsi.parser.callback.LinkExtractor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Parser callback for extracting page urls
 * @author ameshkov
 */
public class LinkExtractorCallback extends LinkExtractor implements ParserCallback {

    public void startPage(Page page) {
        // Doing nothing
    }

    public void endPage(Page page) {
        List<URL> links = new ArrayList<URL>();

        for (String url : this.urls) {
            URL normalized = URLNormalizer.normalize(url, page.getUrl(), page.getCharset());
            if (normalized != null) {
                links.add(normalized);
            }
        }

        page.setLinks(links);
    }
}
