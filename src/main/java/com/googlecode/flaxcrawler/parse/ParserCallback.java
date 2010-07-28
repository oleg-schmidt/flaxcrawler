package com.googlecode.flaxcrawler.parse;

import it.unimi.dsi.parser.callback.Callback;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Represents page content parser
 * @author ameshkov
 */
public interface ParserCallback extends Callback {

    /**
     * Receives notification of the beginning of the specified page parsing
     * @param page
     */
    void startPage(Page page);

    /**
     * Receives notification of the end of the specified page parsing
     * @param page
     */
    void endPage(Page page);
}
