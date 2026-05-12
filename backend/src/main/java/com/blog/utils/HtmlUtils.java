package com.blog.utils;

import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern OBJECT_PATTERN = Pattern.compile(
        "<object[^>]*?>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EVENT_PATTERN = Pattern.compile(
        "\\s*on\\w+\\s*=\\s*['\"]?[^'\"]*['\"]?", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_PROTOCOL = Pattern.compile(
        "javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATA_PROTOCOL = Pattern.compile(
        "data:text/html", Pattern.CASE_INSENSITIVE);

    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        String result = html;
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        result = OBJECT_PATTERN.matcher(result).replaceAll("");
        result = EVENT_PATTERN.matcher(result).replaceAll("");
        result = JS_PROTOCOL.matcher(result).replaceAll("");
        result = DATA_PROTOCOL.matcher(result).replaceAll("");
        return result;
    }

    public static String escapeHtml(String html) {
        if (html == null) return null;
        return html.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

    /**
     * Truncate a string to fit a column. Returns null for null input.
     * Used to keep User-Agent / page URL / referer values within their
     * DB column width — WeChat in-app browser UAs and shared URLs can be
     * very long and would otherwise trigger `Data too long` SQL errors.
     */
    public static String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
