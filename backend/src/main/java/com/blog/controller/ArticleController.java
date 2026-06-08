package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.service.ArticleService;
import com.blog.service.AsyncTaskService;
import com.blog.service.UserService;
import com.blog.utils.HtmlUtils;
import com.blog.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final UserService userService;
    private final AsyncTaskService asyncTaskService;

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        User u = userService.getByUsername(auth.getName());
        return u != null ? u.getId() : null;
    }

    @GetMapping("/articles")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String type) {
        Page<com.blog.vo.ArticleSummary> articlePage = articleService.listPage(page, size, categoryId, tagId, type, "PUBLISHED");
        Map<String, Object> result = new HashMap<>();
        result.put("list", articlePage.getRecords());
        result.put("total", articlePage.getTotal());
        result.put("page", (int) articlePage.getCurrent());
        result.put("size", (int) articlePage.getSize());
        return Result.success(result);
    }

    @GetMapping("/articles/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        Article article = articleService.getById(id);
        asyncTaskService.incrementViewCountAsync(id);
        return Result.success(article);
    }

    @GetMapping("/articles/slug/{slug}")
    public Result<Article> getBySlug(@PathVariable String slug) {
        Article article = articleService.getBySlug(slug);
        return Result.success(article);
    }

    /**
     * 全文搜索文章：同时匹配标题、摘要、内容。
     * 默认只搜索已发布(PUBLISHED)文章，管理员可通过 status 参数覆盖。
     */
    @GetMapping("/articles/search")
    public Result<Map<String, Object>> search(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam String keyword,
            @RequestParam(required = false) String status) {
        String searchStatus = status != null ? status : "PUBLISHED";
        Page<com.blog.vo.ArticleSummary> articlePage = articleService.searchArticles(page, size, keyword.trim(), searchStatus);
        Map<String, Object> result = new HashMap<>();
        result.put("list", articlePage.getRecords());
        result.put("total", articlePage.getTotal());
        result.put("page", (int) articlePage.getCurrent());
        result.put("size", (int) articlePage.getSize());
        return Result.success(result);
    }

    // Admin endpoints
    @GetMapping("/admin/articles")
    public Result<Map<String, Object>> listAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<com.blog.vo.ArticleSummary> articlePage = articleService.listPage(page, size, null, null, null, status);
        Map<String, Object> result = new HashMap<>();
        result.put("list", articlePage.getRecords());
        result.put("total", articlePage.getTotal());
        result.put("page", (int) articlePage.getCurrent());
        result.put("size", (int) articlePage.getSize());
        return Result.success(result);
    }

    @PostMapping("/admin/articles")
    public Result<Article> create(@RequestBody Article article) {
        if (article.getContent() != null) {
            article.setContent(HtmlUtils.sanitize(article.getContent()));
        }
        if (article.getAuthorId() == null) {
            article.setAuthorId(currentUserId());
        }
        if ("PUBLISHED".equals(article.getStatus()) && article.getPublishedAt() == null) {
            article.setPublishedAt(LocalDateTime.now());
        }
        article.setSlug(ensureSlug(article.getSlug(), article.getTitle()));
        Article created = articleService.create(article, article.getTags() != null ?
                article.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toList()) : null);
        return Result.success(created);
    }

    @PutMapping("/admin/articles/{id}")
    public Result<Article> update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        log.info("Update article request: id={}, republish={}, publishedAt={}, status={}",
                id, article.getRepublish(), article.getPublishedAt(), article.getStatus());
        if (article.getContent() != null) {
            article.setContent(HtmlUtils.sanitize(article.getContent()));
        }
        boolean shouldRepublish = Boolean.TRUE.equals(article.getRepublish());
        if ("PUBLISHED".equals(article.getStatus())
                && (article.getPublishedAt() == null || shouldRepublish)) {
            article.setPublishedAt(LocalDateTime.now());
            log.info("Article republished: id={}, newPublishedAt={}", id, article.getPublishedAt());
        }
        article.setSlug(ensureSlug(article.getSlug(), article.getTitle()));
        Article updated = articleService.update(article, article.getTags() != null ?
                article.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toList()) : null);
        return Result.success(updated);
    }

    @DeleteMapping("/admin/articles/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /**
     * 保证 slug 非空且基本唯一：用户提供则原样保留（仅 trim），未提供则按标题生成
     * URL-friendly 段 + 毫秒时间戳兜底。标题不含 ASCII 字符时回退到 "article-{ts}"。
     */
    private String ensureSlug(String provided, String title) {
        if (provided != null && !provided.trim().isEmpty()) {
            return provided.trim();
        }
        String base = title == null ? "" : title.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "article";
        }
        if (base.length() > 80) {
            base = base.substring(0, 80);
        }
        return base + "-" + System.currentTimeMillis();
    }
}
