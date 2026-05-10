package com.blog.controller;

import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.service.ArticleService;
import com.blog.service.TagService;
import com.blog.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;
    private final ArticleService articleService;

    @GetMapping("/tags")
    public Result<List<Tag>> listAll() {
        return Result.success(tagService.listAll());
    }

    @GetMapping("/tags/{slug}/articles")
    public Result<List<Article>> getArticlesByTag(@PathVariable String slug) {
        Tag tag = tagService.getBySlug(slug);
        if (tag == null) {
            return Result.success(Collections.emptyList());
        }
        return Result.success(articleService.listByTagId(tag.getId()));
    }
}
