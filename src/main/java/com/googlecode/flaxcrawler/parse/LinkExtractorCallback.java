package com.googlecode.flaxcrawler.parse;

import it.unimi.dsi.parser.BulletParser;
import it.unimi.dsi.parser.callback.LinkExtractor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import com.googlecode.flaxcrawler.model.Page;
import it.unimi.dsi.parser.Attribute;

/**
 * Parser callback for extracting page urls
 * @author ameshkov
 */
public class LinkExtractorCallback extends LinkExtractor implements ParserCallback {

    @Override
    public void configure(BulletParser parser) {
        super.configure(parser);
    }

    public void startPage(Page page) {
        // Doing nothing
    }

    public void endPage(Page page) {
        URL baseUrl = page.getUrl();
        try {
            baseUrl = base() == null ? page.getUrl() : new URL(base());
        } catch (Exception ex) {
            // Ignore
        }

        List<URL> links = new ArrayList<URL>();

        for (String url : this.urls) {
            URL normalized = URLNormalizer.normalize(url, baseUrl, page.getCharset());
            if (normalized != null) {
                links.add(normalized);
            }
        }

        page.setLinks(links);
    }
}
