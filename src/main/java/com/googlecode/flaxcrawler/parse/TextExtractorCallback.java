package com.googlecode.flaxcrawler.parse;

import it.unimi.dsi.parser.callback.TextExtractor;
import com.googlecode.flaxcrawler.model.Page;

/**
 * {@link ParserCallback} for extracting page title and text
 * @author ameshkov
 */
public class TextExtractorCallback extends TextExtractor implements ParserCallback {

    public void startPage(Page page) {
        // Doing nothing
    }

    public void endPage(Page page) {
        page.setTitle(this.title.toString());
    }
}
