package com.blog.utils;

import java.util.regex.Pattern;

/**
 * HTML 净化工具。
 *
 * 使用精确的正则表达式过滤 XSS 攻击向量，不依赖外部 XML 策略文件，
 * 避免类初始化失败风险。
 */
public class HtmlUtils {

    /**
     * 匹配并移除 &lt;script&gt;...&lt;/script&gt;（含内容和标签）。
     * 支持跨行、标签内带属性。
     */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 匹配并移除 &lt;object&gt;...&lt;/object&gt;。
     */
    private static final Pattern OBJECT_PATTERN = Pattern.compile(
            "<object\\b[^<]*(?:(?!</object>)<[^<]*)*</object>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 精确匹配 HTML 标签内的双引号事件属性并移除，保留标签的其他属性。
     */
    private static final Pattern EVENT_ATTR_DQ = Pattern.compile(
            "(<[a-zA-Z][^>]*?)\\s+on\\w+\\s*=\\s*\"[^\"]*\"((?:\\s[^>]*)?)",
            Pattern.CASE_INSENSITIVE);

    /**
     * 精确匹配 HTML 标签内的单引号事件属性并移除，保留标签的其他属性。
     */
    private static final Pattern EVENT_ATTR_SQ = Pattern.compile(
            "(<[a-zA-Z][^>]*?)\\s+on\\w+\\s*=\\s*'[^']*'((?:\\s[^>]*)?)",
            Pattern.CASE_INSENSITIVE);

    /**
     * 精确匹配 HTML 标签内的无引号事件属性并移除，保留标签的其他属性。
     */
    private static final Pattern EVENT_ATTR_NQ = Pattern.compile(
            "(<[a-zA-Z][^>]*?)\\s+on\\w+\\s*=\\s*[^\\s\"'>]+((?:\\s[^>]*)?)",
            Pattern.CASE_INSENSITIVE);

    /** 移除 javascript: 协议 */
    private static final Pattern JS_PROTOCOL = Pattern.compile(
            "javascript:", Pattern.CASE_INSENSITIVE);

    /** 移除 data:text/html 协议 */
    private static final Pattern DATA_PROTOCOL = Pattern.compile(
            "data:text/html", Pattern.CASE_INSENSITIVE);

    /**
     * 净化 HTML，移除 XSS 攻击向量。
     *
     * @param html 原始 HTML
     * @return 净化后的 HTML
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        String result = html;
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        result = OBJECT_PATTERN.matcher(result).replaceAll("");
        // 循环处理直到没有匹配，依次处理双引号、单引号、无引号的事件属性
        String prev;
        do {
            prev = result;
            result = EVENT_ATTR_DQ.matcher(result).replaceAll("$1$2");
            result = EVENT_ATTR_SQ.matcher(result).replaceAll("$1$2");
            result = EVENT_ATTR_NQ.matcher(result).replaceAll("$1$2");
        } while (!result.equals(prev));
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
