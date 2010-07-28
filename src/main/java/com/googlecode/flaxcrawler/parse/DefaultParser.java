package com.googlecode.flaxcrawler.parse;

import it.unimi.dsi.parser.BulletParser;
import java.util.ArrayList;
import java.util.List;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Default parser implementation. Extracts links from page.
 * @author ameshkov
 */
public class DefaultParser implements Parser {

    private List<ParserCallback> parserCallbacks = new ArrayList<ParserCallback>();

    /**
     * Adds parser callback
     * @param parserCallback
     */
    public void addParserCallback(ParserCallback parserCallback) {
        this.parserCallbacks.add(parserCallback);
    }

    /**
     * Creates an instance of the {@code DefaultParser}. Two {@link ParserCallback} used by default: {@link LinkExtractorCallback} and {@link TextExtractorCallback}.
     */
    public DefaultParser() {
        parserCallbacks.add(new LinkExtractorCallback());
        parserCallbacks.add(new TextExtractorCallback());
    }

    public void parse(Page page) {
        for (ParserCallback parserCallback : parserCallbacks) {
            parserCallback.startPage(page);

            BulletParser bulletParser = new BulletParser();
            bulletParser.setCallback(parserCallback);
            bulletParser.parse(page.getContentString().toCharArray());

            parserCallback.endPage(page);
        }
    }
}
