package com.blog.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlUtilsTest {

    @Test
    void testClassInitialization() {
        // 确保类能正常初始化，不抛 NoClassDefFoundError
        assertDoesNotThrow(() -> HtmlUtils.sanitize("<p>hello</p>"));
    }

    @Test
    void testNullAndEmpty() {
        assertNull(HtmlUtils.sanitize(null));
        assertEquals("", HtmlUtils.sanitize(""));
    }

    @Test
    void testNormalHtmlPreserved() {
        String html = "<h1>标题</h1><p>正文</p>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testScriptTagRemoved() {
        String html = "<p>hello</p><script>alert(1)</script><p>world</p>";
        assertEquals("<p>hello</p><p>world</p>", HtmlUtils.sanitize(html));
    }

    @Test
    void testScriptTagWithAttributesRemoved() {
        String html = "<script type=\"text/javascript\" src=\"evil.js\">alert(1)</script>";
        assertEquals("", HtmlUtils.sanitize(html));
    }

    @Test
    void testScriptTagMultilineRemoved() {
        String html = "<script>\n  var x = 1;\n  alert(x);\n</script>";
        assertEquals("", HtmlUtils.sanitize(html));
    }

    @Test
    void testEventAttributeRemoved() {
        String html = "<div onclick=\"alert(1)\">点击</div>";
        assertEquals("<div>点击</div>", HtmlUtils.sanitize(html));
    }

    @Test
    void testEventAttributeWithOtherAttrsPreserved() {
        String html = "<div class=\"x\" onclick=\"alert(1)\" id=\"y\">内容</div>";
        assertEquals("<div class=\"x\" id=\"y\">内容</div>", HtmlUtils.sanitize(html));
    }

    @Test
    void testMultipleEventAttributesRemoved() {
        String html = "<div onclick=\"a()\" onmouseover=\"b()\">内容</div>";
        assertEquals("<div>内容</div>", HtmlUtils.sanitize(html));
    }

    @Test
    void testTextWithOnEqualsNotRemoved() {
        // 关键测试：普通文本中的 onxxx=yyy 不应被误伤
        String html = "<p>设置 only=true 即可启用</p>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testTextWithOnceEqualsNotRemoved() {
        String html = "<p>参数 once=1 表示只执行一次</p>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testCodeBlockWithScriptTagNotRemoved() {
        // 代码块中的 </script> 是文本内容，不应被过滤
        // 但注意：sanitize 只处理原始 HTML，代码块在 <pre><code> 中的 </script> 仍然是 HTML 标签
        // 所以如果原始 HTML 中 <code> 内有未转义的 <script>，sanitize 会正确移除它（这是安全行为）
        // 但通常 Markdown 编辑器会将代码块内容转义为 &lt;script&gt;
        String html = "<pre><code>var s = \"&lt;script&gt;\";</code></pre>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testJavascriptProtocolRemoved() {
        String html = "<a href=\"javascript:alert(1)\">链接</a>";
        assertEquals("<a href=\"alert(1)\">链接</a>", HtmlUtils.sanitize(html));
    }

    @Test
    void testDataProtocolRemoved() {
        // data:text/html 协议被移除，同时内部的 script 标签也被移除
        String html = "<a href=\"data:text/html,<script>alert(1)</script>\">链接</a>";
        // 先移除 script 标签，再移除 data 协议
        assertEquals("<a href=\",\">链接</a>", HtmlUtils.sanitize(html));
    }

    @Test
    void testObjectTagRemoved() {
        String html = "<object data=\"evil.swf\"></object>";
        assertEquals("", HtmlUtils.sanitize(html));
    }

    @Test
    void testHeadingWithIdPreserved() {
        String html = "<h3 id=\"section-3-7\">3.7 节</h3><p>内容</p>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testMarkdownGeneratedHtmlPreserved() {
        // 模拟 MarkdownIt 生成的典型 HTML
        String html = "<h3>3.7 节</h3>\n"
                + "<p>设置 only=true 即可启用。</p>\n"
                + "<pre><code class=\"language-javascript\">const x = 1;\nconsole.log(x);\n</code></pre>\n"
                + "<blockquote><p>注意：once=1 表示只执行一次。</p></blockquote>";
        assertEquals(html, HtmlUtils.sanitize(html));
    }

    @Test
    void testEscapeHtml() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;", HtmlUtils.escapeHtml("<script>alert(1)</script>"));
    }

    @Test
    void testTruncate() {
        assertEquals("abc", HtmlUtils.truncate("abc", 10));
        assertEquals("abc", HtmlUtils.truncate("abcdef", 3));
        assertNull(HtmlUtils.truncate(null, 10));
    }
}
