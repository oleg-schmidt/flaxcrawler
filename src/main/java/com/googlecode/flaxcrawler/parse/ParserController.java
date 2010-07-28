package com.googlecode.flaxcrawler.parse;

import com.googlecode.flaxcrawler.model.Page;

/**
 * Manages parsers. Returns parser for the specified {@link Page}
 * @author ameshkov
 */
public interface ParserController {

    /**
     * Returns {@link Parser} for the specified {@link Page}
     * @param page
     * @return
     */
    Parser getParser(Page page);
}
