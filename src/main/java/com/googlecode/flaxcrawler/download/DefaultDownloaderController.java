package com.googlecode.flaxcrawler.download;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.googlecode.flaxcrawler.utils.UrlUtils;

/**
 * Default controller implementation. Provides generic downloader for all urls
 * or custom downloader for specific domain.
 * @author ameshkov
 */
public class DefaultDownloaderController implements DownloaderController {

    private Downloader genericDownloader;
    private Map<String, Downloader> customDownloaders = new HashMap<String, Downloader>();

    /**
     * Creates an instance of the {@code DefaultDownloaderController} class.
     * {@link DefaultDownloader} is used as generic downloader.
     */
    public DefaultDownloaderController() {
        this(new DefaultDownloader());
    }

    /**
     * Creates an instance of the {@code DefaultDownloaderController} class.
     * @param genericDownloader
     */
    public DefaultDownloaderController(Downloader genericDownloader) {
        this.genericDownloader = genericDownloader;
    }

    /**
     * Creates an instance of the {@code DefaultDownloaderController} class.
     * @param genericDownloader
     * @param customDownloaders Custom downloaders map. Key is domain name, value is custom downloader for this domain.
     */
    public DefaultDownloaderController(Downloader genericDownloader, Map<String, Downloader> customDownloaders) {
        this.genericDownloader = genericDownloader;
        this.customDownloaders.putAll(customDownloaders);
    }

    /**
     * Sets generic downloader (should be used for all urls except compatible for custom downloader)
     * @param genericDownloader
     */
    public void setGenericDownloader(Downloader genericDownloader) {
        this.genericDownloader = genericDownloader;
    }

    /**
     * Adds custom downloader for the specified domain
     * @param domainName
     * @param customDownloader
     */
    public void addCustomDownloader(String domainName, Downloader customDownloader) {
        customDownloaders.put(domainName, customDownloader);
    }

    /**
     * Returns downloader for the spec
     * @param url
     * @return
     */
    public Downloader getDownloader(URL url) {
        Downloader customDownloader = customDownloaders.get(UrlUtils.getDomainName(url));
        return customDownloader == null ? genericDownloader : customDownloader;
    }
}
