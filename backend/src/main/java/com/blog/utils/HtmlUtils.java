package com.blog.utils;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

import java.util.regex.Pattern;

public class HtmlUtils {

    /**
     * 使用 AntiSamy 进行精确的 HTML 净化。
     * 相比原正则方案，AntiSamy 基于 DOM 解析，不会误伤普通文本中的 onxxx=yyy 等内容。
     *
     * 策略说明：
     * - 允许大多数博客常用的 HTML 标签和属性
     * - 自动过滤 script、事件处理器、javascript: 协议等 XSS 向量
     * - 保留代码块、公式、图片、视频等富媒体内容的结构
     */
    private static final AntiSamy ANTI_SAMY = new AntiSamy();
    private static final Policy POLICY;

    static {
        Policy p = null;
        try {
            // 优先尝试 anythinggoes 策略（最宽松，适合富文本博客）
            p = Policy.getInstance(HtmlUtils.class.getResourceAsStream("/antisamy-anythinggoes.xml"));
        } catch (Exception e1) {
            try {
                // 回退到 myspace 策略
                p = Policy.getInstance(HtmlUtils.class.getResourceAsStream("/antisamy-myspace.xml"));
            } catch (Exception e2) {
                // 回退到 ebay 策略
                try {
                    p = Policy.getInstance(HtmlUtils.class.getResourceAsStream("/antisamy-ebay.xml"));
                } catch (Exception e3) {
                    // 最后尝试 slashdot
                    try {
                        p = Policy.getInstance(HtmlUtils.class.getResourceAsStream("/antisamy-slashdot.xml"));
                    } catch (Exception e4) {
                        // 如果所有内置策略都不可用，将使用 fallback
                    }
                }
            }
        }
        POLICY = p;
    }

    // Fallback 正则：仅在后端策略不可用时使用，已做精确化改进
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "<script\\b[^<]*(?:(?!<\\/script>)<[^<]*)*<\\/script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern OBJECT_PATTERN = Pattern.compile(
            "<object\\b[^<]*(?:(?!<\\/object>)<[^<]*)*<\\/object>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 精确匹配 HTML 标签内的事件属性。
     * 结构: (<tag 前缀)(\s+onxxx="..." 或 onxxx='...' 或 onxxx=...)(标签后缀>)
     * 用 $1$2 替换，保留标签其他属性。
     */
    private static final Pattern EVENT_PATTERN = Pattern.compile(
            "(<[a-zA-Z][^>]*?)\\s+on\\w+\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s>]*)((?:\\s[^>]*)?>",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_PROTOCOL = Pattern.compile(
            "javascript:", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATA_PROTOCOL = Pattern.compile(
            "data:text/html", Pattern.CASE_INSENSITIVE);

    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        // 优先使用 AntiSamy 做精确的 DOM 级净化
        if (POLICY != null) {
            try {
                CleanResults results = ANTI_SAMY.scan(html, POLICY);
                return results.getCleanHTML();
            } catch (Exception e) {
                // AntiSamy 失败时回退到改进的正则
            }
        }

        return fallbackSanitize(html);
    }

    private static String fallbackSanitize(String html) {
        String result = html;
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");
        result = OBJECT_PATTERN.matcher(result).replaceAll("");
        // 循环处理多个事件属性，直到没有匹配为止
        String prev;
        do {
            prev = result;
            result = EVENT_PATTERN.matcher(result).replaceAll("$1$2");
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
