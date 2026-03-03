package com.weblog.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing Apache Combined Log Format entries.
 * 
 * Format: 
 *   IP - - [timestamp] "METHOD URL PROTOCOL" statusCode size "referrer" "userAgent"
 * 
 * Example:
 *   54.36.149.41 - - [22/Jan/2019:03:56:14 +0330] "GET /filter/27|13%20%D9%85%DA%AF%D8%A7%D9%BE%DB%8C%DA%A9%D8%B3%D9%84 HTTP/1.1" 200 30577 "-" "Mozilla/5.0 ..."
 */
public class LogParser {

    // Regex pattern for Apache Combined Log Format
    private static final String LOG_PATTERN =
        "^(\\S+)"                    // 1. IP address
        + " (\\S+)"                  // 2. Identity (usually -)
        + " (\\S+)"                  // 3. User (usually -)
        + " \\[([^\\]]+)\\]"        // 4. Timestamp
        + " \"([A-Z]+)"             // 5. HTTP Method
        + " (\\S+)"                 // 6. URL/Resource
        + " (\\S+)\""               // 7. Protocol
        + " (\\d{3})"               // 8. Status Code
        + " (\\d+|-)"               // 9. Response Size
        + "( \"([^\"]*)\")?"        // 10-11. Referrer (optional)
        + "( \"([^\"]*)\")?";       // 12-13. User Agent (optional)

    private static final Pattern pattern = Pattern.compile(LOG_PATTERN);

    private String ip;
    private String identity;
    private String user;
    private String timestamp;
    private String httpMethod;
    private String url;
    private String protocol;
    private int statusCode;
    private long responseSize;
    private String referrer;
    private String userAgent;
    private int hour;

    /**
     * Parses a single Apache Combined Log Format line.
     * 
     * @param logLine the raw log line string
     * @return true if parsing was successful, false otherwise
     */
    public boolean parse(String logLine) {
        if (logLine == null || logLine.isEmpty()) {
            return false;
        }

        Matcher matcher = pattern.matcher(logLine);
        if (!matcher.find()) {
            return false;
        }

        try {
            this.ip = matcher.group(1);
            this.identity = matcher.group(2);
            this.user = matcher.group(3);
            this.timestamp = matcher.group(4);
            this.httpMethod = matcher.group(5);
            this.url = matcher.group(6);
            this.protocol = matcher.group(7);
            this.statusCode = Integer.parseInt(matcher.group(8));

            String sizeStr = matcher.group(9);
            this.responseSize = sizeStr.equals("-") ? 0 : Long.parseLong(sizeStr);

            this.referrer = matcher.group(11) != null ? matcher.group(11) : "-";
            this.userAgent = matcher.group(13) != null ? matcher.group(13) : "-";

            // Extract hour from timestamp (format: 22/Jan/2019:03:56:14 +0330)
            this.hour = extractHour(this.timestamp);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extracts the hour (0-23) from the Apache timestamp string.
     */
    private int extractHour(String timestamp) {
        // Timestamp format: 22/Jan/2019:03:56:14 +0330
        // The hour starts after the first ':'
        int colonIndex = timestamp.indexOf(':');
        if (colonIndex >= 0 && colonIndex + 3 <= timestamp.length()) {
            return Integer.parseInt(timestamp.substring(colonIndex + 1, colonIndex + 3));
        }
        return -1;
    }

    // ==================== Getters ====================

    public String getIp() {
        return ip;
    }

    public String getIdentity() {
        return identity;
    }

    public String getUser() {
        return user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getHour() {
        return hour;
    }

    /**
     * Returns a status code category string (2xx, 3xx, 4xx, 5xx).
     */
    public String getStatusCategory() {
        int category = statusCode / 100;
        switch (category) {
            case 2: return "2xx_Success";
            case 3: return "3xx_Redirect";
            case 4: return "4xx_ClientError";
            case 5: return "5xx_ServerError";
            default: return category + "xx_Other";
        }
    }
}
