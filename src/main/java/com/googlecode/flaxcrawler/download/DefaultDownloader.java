package com.googlecode.flaxcrawler.download;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import com.googlecode.flaxcrawler.model.Page;

/**
 * Base class for all downloaders
 * @author ameshkov
 */
public class DefaultDownloader implements Downloader {

    private Logger log = Logger.getLogger(this.getClass());
    private ProxyController proxyController;
    private int triesCount = 1;
    private long maxContentLength;
    private int connectionTimeout = 30000;
    private int readTimeout = 30000;
    private Map<String, String> headers;
    private String[] allowedContentTypes = new String[]{"text/html"};

    /**
     * Sets request headers
     * @param headers
     */
    public void setRequestHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Sets connection timeout. Default - 30000 ms.
     * @param connectionTimeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Sets read timeout. Default - 30000 ms.
     * @param readTimeout
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Sets maximum tries count
     * @param triesCount
     */
    public void setTriesCount(int triesCount) {
        this.triesCount = triesCount;
    }

    /**
     * Sets proxy controller
     * @param proxyController
     */
    public void setProxyController(ProxyController proxyController) {
        this.proxyController = proxyController;
    }

    /**
     * Sets maximum content length
     * @param maxContentLength
     */
    public void setMaxContentLength(long maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    /**
     * Sets allowed content types. By default text/html is allowed only.
     * @param allowedContentTypes
     */
    public void setAllowedContentTypes(String[] allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    /**
     * Returns downloader's logger
     * @return
     */
    protected Logger getLogger() {
        return log;
    }

    /**
     * Sets default headers for the specified request
     * @param request
     */
    protected void setDefaultHeaders(Request request) {
        request.addHeader("User-Agent", "FlaxCrawler/1.0");
        request.addHeader("Connection", "close");
        request.addHeader("Pragma", "no-cache");
        request.addHeader("Cache-Control", "no-cache");
        request.addHeader("Accept-Encoding", "gzip,defalte");
        request.addHeader("Accept-Language", "ru,en-us;q=0.7,en;q=0.3");
    }

    /**
     * Creates request for this downloader
     * @param url
     * @return
     */
    protected Request createRequest(URL url) {
        // Creating request instance
        Request request = new Request(url);

        // Setting default headers
        setDefaultHeaders(request);

        // Setting custom headers
        if (headers != null) {
            request.getHeaders().putAll(headers);
        }
        return request;
    }

    /**
     * Downloads web page. Returns {@code null} if Page cannot be downloaded due to constraints (contentType, maxContentLength)
     * @param url
     * @return
     */
    public Page download(URL url) throws DownloadException {
        Request request = createRequest(url);

        for (int i = 0; i < triesCount; i++) {
            try {
                log.debug("Downloading from " + url + ", try number " + (i + 1));

                // Using proxy
                Proxy proxy = proxyController == null ? null : proxyController.getProxy();

                // Sending HEAD request using specified proxy
                Page headPage = headRequest(request, proxy);

                if (headPage != null && headPage.getResponseCode() >= 300 && headPage.getResponseCode() < 400) {
                    log.debug("Server redirected our request to " + url);
                    // No need to request it again
                    return headPage;
                }

                if (headPage.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    log.debug("Cannot download " + request.getUrl() + (proxy == null ? "" : " through proxy " + proxy));
                    return null;
                }

                if (!checkConstaints(headPage)) {
                    log.info("Request to " + request.getUrl() + " violates this downloader constraints");
                    return null;
                }

                // Downloading using the same proxy
                Page page = download(request, proxy);
                return page;
            } catch (DownloadException ex) {
                log.info("DownloadException while downloading from " + request.getUrl() + ": " + ex.getMessage());
            }
        }

        String message = "Could not download " + url;
        log.error(message);
        throw new DownloadException(message);
    }

    /**
     * Checks downloader constraints. Returns true is everything is OK.
     * @param page
     * @return
     */
    protected boolean checkConstaints(Page page) {
        log.debug("Checking constraints for page " + page.getUrl());

        int contentLength = NumberUtils.toInt(page.getHeader("content-length"));

        // If "Content-Length" header is specified - checking maxContentLength
        if (maxContentLength > 0 && contentLength > 0 && contentLength < maxContentLength) {
            log.info(page.getUrl() + " content length exceeded limit, stopping downloading");
            return false;
        }

        String contentType = page.getHeader("content-type");

        // If "Content-Type" header is not specified - something is definitely wrong
        if (contentType == null) {
            log.info(page.getUrl() + " content type is not specified, stopping downloading");
            return false;
        }

        if (allowedContentTypes == null) {
            return true;
        } else {
            for (String allowedContentType : allowedContentTypes) {
                if (contentType.startsWith(allowedContentType)) {
                    return true;
                }
            }
        }

        log.info(page.getUrl() + " content type (" + contentType + ") is not allowed");
        return false;
    }

    /**
     * Creates connection for the specified request
     * @param request
     * @return
     */
    protected HttpURLConnection createConnection(Request request, Proxy proxy) throws IOException {
        log.debug("Opening connection to " + request.getUrl() + (proxy == null ? " not using proxy " : " using proxy " + proxy));

        // Openging connection
        HttpURLConnection connection = (HttpURLConnection) (proxy == null ? request.getUrl().openConnection() : request.getUrl().openConnection(proxy));
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);

        // Setting request headers
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            log.debug("Setting request header " + entry.getKey() + "=" + entry.getValue());
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }

        return connection;
    }

    /**
     * Makes HEAD request and returns Response headers. Throws {@link DownloadException} if there's any exception downloading
     * this page. Usually it means that proxy is now dead.
     * @param request
     * @param proxy
     * @return {@link Page} object without content but with response code and headers
     */
    protected Page headRequest(Request request, Proxy proxy) throws DownloadException {
        long startTime = System.currentTimeMillis();

        HttpURLConnection connection = null;

        try {
            connection = createConnection(request, proxy);
            connection.setRequestMethod("HEAD");
            connection.connect();

            // Setting response properties
            int responseCode = connection.getResponseCode();

            // Setting response headers
            Map<String, String> responseHeaders = new HashMap<String, String>();
            for (String header : connection.getHeaderFields().keySet()) {
                responseHeaders.put(header == null ? null : header.toLowerCase(), connection.getHeaderField(header));
            }

            long responseTime = System.currentTimeMillis() - startTime;
            return createPage(request, null, responseCode, responseHeaders, null, responseTime);
        } catch (IOException ex) {
            String message = "Error while processing HEAD request to " + request.getUrl() + (proxy == null ? " not using proxy" : "using proxy " + proxy);
            log.info(message);
            throw new DownloadException(message, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Downloads page content
     * @param request
     * @param httpMethod
     * @param useProxy
     * @return
     */
    protected Page download(Request request, Proxy proxy) throws DownloadException {
        HttpURLConnection connection = null;
        byte[] content = null;
        Map<String, String> responseHeaders = new HashMap<String, String>();
        int responseCode = 0;
        String encoding = null;
        long startTime = System.currentTimeMillis();

        try {
            // Creating connection
            connection = createConnection(request, proxy);
            connection.setRequestMethod("GET");
            connection.connect();

            // Setting response properties
            responseCode = connection.getResponseCode();

            // Setting response headers
            for (String header : connection.getHeaderFields().keySet()) {
                responseHeaders.put(header == null ? null : header.toLowerCase(), connection.getHeaderField(header));
            }

            // Getting content type
            // First checking content encoding header
            String contentEncoding = responseHeaders.get("content-encoding");

            if (contentEncoding != null && "gzip".equals(contentEncoding)) {
                // Content is gzipped
                content = IOUtils.toByteArray(new GZIPInputStream(connection.getInputStream()));
            } else {
                // Content is plain
                content = IOUtils.toByteArray(connection.getInputStream());
            }

            // Trying to get charset from the "Content-Type" header
            encoding = getCharset(responseHeaders.get("content-type"));

            if (encoding == null) {
                // Trying to get charset from meta tag
                encoding = getCharsetFromMeta(content);
            }

            if (encoding == null) {
                //set by default
                encoding = "UTF-8";
            }
        } catch (SocketTimeoutException ex) {
            log.info("Timeout exception for url " + request.getUrl());
            // Setting response code to 408
            responseCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        } catch (IOException ex) {
            log.error("Error while requesting url " + request.getUrl(), ex);
            // Setting response code to 503
            responseCode = HttpURLConnection.HTTP_UNAVAILABLE;
        } finally {
            if (connection != null) {
                // Closing connection
                connection.disconnect();
            }
        }

        long responseTime = System.currentTimeMillis() - startTime;
        return createPage(request, content, responseCode, responseHeaders, encoding, responseTime);
    }

    /**
     * Creates {@link Page} instance
     * @param request
     * @param content
     * @param responseCode
     * @param responseHeaders
     * @param encoding
     * @param responseTime
     * @return
     */
    protected Page createPage(Request request, byte[] content, int responseCode, Map<String, String> responseHeaders, String encoding, long responseTime) {
        log.debug("Response code from " + request.getUrl() + " is " + responseCode);
        Page page = new Page(request.getUrl(), responseHeaders, responseCode, encoding, responseTime, content);
        return page;
    }

    /**
     * Tries to get encoding. First from the "Content-Type" header, then tries to guess it from the content
     * @param content
     * @param contentType
     * @return
     */
    protected String getCharset(String contentType) {
        String charset = null;

        // Parsing Content-Type header first
        if (contentType != null) {
            String[] parts = contentType.split(";");

            for (int i = 1; i < parts.length && charset == null; i++) {
                final String t = parts[i].trim();
                final int index = t.toLowerCase().indexOf("charset=");
                if (index != -1) {
                    // Encoding found successfully, returning
                    charset = t.substring(index + 8);
                    return charset;
                }
            }
        }

        return charset;
    }

    /**
     * Tries to get charset from {@code meta} tag.
     * Very simple implementation.
     * @param content
     * @return
     */
    protected String getCharsetFromMeta(byte[] content) {
        try {
            String utf8string = new String(content, "UTF-8");

            Pattern metaRegexp = Pattern.compile("<meta\\s*[^>]*\\s*content=(\"|')?text/html;\\s+charset=([^\"';]+)(\"|'|;)?[^>]*>", Pattern.CASE_INSENSITIVE);
            Matcher matcher = metaRegexp.matcher(utf8string);

            if (matcher.find()) {
                return matcher.group(2);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
