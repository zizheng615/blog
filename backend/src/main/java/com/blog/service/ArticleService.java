package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;

import java.util.List;

public interface ArticleService {
    Page<com.blog.vo.ArticleSummary> listPage(Integer page, Integer size, Long categoryId, Long tagId, String articleType, String status);
    Article getById(Long id);
    Article getBySlug(String slug);

    /**
     * 全文搜索文章：同时匹配标题、摘要、内容。
     * 默认只搜索已发布(PUBLISHED)文章，管理员可通过 status 参数覆盖。
     */
    Page<com.blog.vo.ArticleSummary> searchArticles(Integer page, Integer size, String keyword, String status);

    Article create(Article article, List<Long> tagIds);
    Article update(Article article, List<Long> tagIds);
    void delete(Long id);
    void incrementViewCount(Long id);
    List<Article> listByTagId(Long tagId);
}
