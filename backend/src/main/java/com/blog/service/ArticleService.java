package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;

import java.util.List;

public interface ArticleService {
    Page<Article> listPage(Integer page, Integer size, Long categoryId, Long tagId, String articleType, String status);
    Article getById(Long id);
    Article getBySlug(String slug);
    Article create(Article article, List<Long> tagIds);
    Article update(Article article, List<Long> tagIds);
    void delete(Long id);
    void incrementViewCount(Long id);
    List<Article> listByTagId(Long tagId);
}
