package com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import com.blog.utils.HtmlUtils;
import com.blog.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String type) {
        Page<Article> articlePage = articleService.listPage(page, size, categoryId, tagId, type, "PUBLISHED");
        Map<String, Object> result = new HashMap<>();
        result.put("list", articlePage.getRecords());
        result.put("total", articlePage.getTotal());
        result.put("page", (int) articlePage.getCurrent());
        result.put("size", (int) articlePage.getSize());
        return Result.success(result);
    }

    @GetMapping("/articles/{id}")
    public Result<Article> getById(@PathVariable Long id) {
        articleService.incrementViewCount(id);
        Article article = articleService.getById(id);
        return Result.success(article);
    }

    @GetMapping("/articles/slug/{slug}")
    public Result<Article> getBySlug(@PathVariable String slug) {
        Article article = articleService.getBySlug(slug);
        return Result.success(article);
    }

    // Admin endpoints
    @GetMapping("/admin/articles")
    public Result<Map<String, Object>> listAdmin(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        Page<Article> articlePage = articleService.listPage(page, size, null, null, null,
                status != null ? status : null);
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
        Article created = articleService.create(article, article.getTags() != null ?
                article.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toList()) : null);
        return Result.success(created);
    }

    @PutMapping("/admin/articles/{id}")
    public Result<Article> update(@PathVariable Long id, @RequestBody Article article) {
        article.setId(id);
        if (article.getContent() != null) {
            article.setContent(HtmlUtils.sanitize(article.getContent()));
        }
        Article updated = articleService.update(article, article.getTags() != null ?
                article.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toList()) : null);
        return Result.success(updated);
    }

    @DeleteMapping("/admin/articles/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}
