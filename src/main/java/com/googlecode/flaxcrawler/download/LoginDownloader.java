package com.googlecode.flaxcrawler.download;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import com.googlecode.flaxcrawler.model.Page;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;

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
                lastLoginTime = System.currentTimeMillis();
            }
        }

        return super.download(url);
    }

    /**
     * Posts {@code postData} to the specified {@code loginUrl}
     */
    private synchronized void login() {
        HttpURLConnection connection = null;
        String connectionHeader = null;
        OutputStream out = null;

        try {
            Request request = createRequest(new URL(loginUrl));
            connection = createConnection(request, Proxy.NO_PROXY);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Connection", "close");
            out = connection.getOutputStream();
            OutputStreamWriter outwriter = new OutputStreamWriter(out);
            outwriter.write(postData);
            outwriter.flush();

            connection.getResponseCode();
            Map<String, String> cookies = parseCookies(connection);

            StringBuffer sb = new StringBuffer();
            Iterator<Entry<String, String>> it = cookies.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                sb.append(entry.getKey());
                sb.append("=");
                sb.append(entry.getValue());
                sb.append("; ");
            }

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Cookie", sb.toString());
            setRequestHeaders(headers);
            getLogger().info("Logged in to " + loginUrl + " successfully");

            InputStream io = null;
            try {
                io = connection.getInputStream();
                getLogger().debug("Server response is " + IOUtils.toString(io));
            } finally {
                if (io != null) {
                    io.close();
                }
            }
        } catch (Exception ex) {
            getLogger().error("Error logging in to " + loginUrl + " using post data " + postData, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    // Ignore
                }
            }
            connectionHeader = connection.getHeaderField("Connection");
            cleanUpConnection(connection, connectionHeader);
            connection.disconnect();
        }
    }

    /**
     * Parsing cookies
     * @param connection
     * @return
     */
    private Map<String, String> parseCookies(HttpURLConnection connection) {
        Map<String, String> cookies = new HashMap<String, String>();

        for (int i = 0;; i++) {
            String headerName = connection.getHeaderFieldKey(i);
            String headerValue = connection.getHeaderField(i);

            if (headerName == null && headerValue == null) {
                // No more headers
                break;
            }
            if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                StringTokenizer st = new StringTokenizer(headerValue, ";");

                // the specification dictates that the first name/value pair
                // in the string is the cookie name and value, so let's handle
                // them as a special case:

                if (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    String name = token.substring(0, token.indexOf("="));
                    String value = token.substring(token.indexOf("=") + 1, token.length());
                    cookies.put(name, value);
                }
            }
        }

        return cookies;
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
