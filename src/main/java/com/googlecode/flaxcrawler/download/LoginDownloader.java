package com.googlecode.flaxcrawler.download;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.googlecode.flaxcrawler.model.Page;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Downloader that first logs into site's member zone
 * @author ameshkov
 */
public class LoginDownloader extends DefaultDownloader {

    private long lastLoginTime;
    private long sessionDuration = 1800000;
    private String postData;
    private String loginUrl;

    /**
     * Sets session duration (ms). After this time pass - downloader logs in one more time
     * @param sessionDuration
     */
    public void setSessionDuration(long sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    /**
     * Sets post data, that would be posted to the {@code loginUrl}.
     * @param postData
     */
    public void setPostData(String postData) {
        this.postData = postData;
    }

    /**
     * Sets login url
     * @param loginUrl
     */
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    @Override
    public Page download(URL url) throws DownloadException {
        synchronized (this) {
            if (System.currentTimeMillis() - lastLoginTime > sessionDuration) {
                getLogger().info("Login downloader should log in to " + loginUrl);
                login();
            }
        }

        return super.download(url);
    }

    /**
     * Posts {@code postData} to the specified {@code loginUrl}
     */
    private synchronized void login() {
        HttpURLConnection connection = null;

        try {
            Request request = createRequest(new URL(loginUrl));
            connection = createConnection(request, Proxy.NO_PROXY);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(postData);
            out.flush();

            connection.getResponseCode();
            String cookieHeader = "";

//            for (int i = 0;; i++) {
//                String headerName = connection.getHeaderFieldKey(i);
//                String headerValue = connection.getHeaderField(i);
//
//                if (headerName == null && headerValue == null) {
//                    // No more headers
//                    break;
//                }
//                if ("Set-Cookie".equalsIgnoreCase(headerName)) {
//                    // Parse cookie
//                    String cookieValue = getCookieValue(headerValue);
//                    cookieHeader += cookieValue + ";";
//                }
//            }

            Map<String, String> cookieHeaders = new HashMap<String, String>();
            Map<String, List<String>> headersMap = connection.getHeaderFields();
            if (headersMap != null && headersMap.size() > 0) {
                List<String> cookies = headersMap.get("Set-Cookie");
                if (cookies != null && cookies.size() > 0) {
                    for (String cookieString : cookies) {
                        String cookieKey = StringUtils.substringBefore(cookieString, "=");
                        String cookieValue = getCookieValue(cookieString);
                        String exCookieValue = cookieHeaders.get(cookieKey);
                        if (exCookieValue == null) {
                            cookieHeaders.put(cookieKey, cookieValue);
                        } else if (cookieValue.length() > exCookieValue.length()) {
                            cookieHeaders.put(cookieKey, cookieValue);
                        }
                    }
                }
            }
            for (String cookieKey : cookieHeaders.keySet()) {
                cookieHeader += cookieHeaders.get(cookieKey) + ";";
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Cookie", cookieHeader);
            setRequestHeaders(headers);
        } catch (Exception ex) {
            getLogger().error("Error logging in to " + loginUrl + " using post data " + postData, ex);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Gets cookie value from the Set-Cookie header
     * @param setCookieHeader
     * @return
     */
    private String getCookieValue(String setCookieHeader) {
        String[] fields = setCookieHeader.split(";\\s*");

        String cookieValue = fields[0];
        return cookieValue;
    }
}
