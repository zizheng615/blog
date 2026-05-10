package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.exception.BlogException;
import com.blog.exception.ErrorCode;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.ArticleTagMapper;
import com.blog.mapper.TagMapper;
import com.blog.service.ArticleService;
import com.blog.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final RedisCache redisCache;

    private static final String ARTICLE_LIST_KEY = "article:list";
    private static final String ARTICLE_DETAIL_KEY = "article:detail";
    private static final long CACHE_TTL = 30;

    @Override
    public Page<Article> listPage(Integer page, Integer size, Long categoryId, Long tagId, String articleType, String status) {
        String cacheKey = String.format("%s:%d:%d:%s:%s:%s:%s",
                ARTICLE_LIST_KEY, page, size,
                categoryId != null ? categoryId : "null",
                tagId != null ? tagId : "null",
                articleType != null ? articleType : "null",
                status != null ? status : "null");

        @SuppressWarnings("unchecked")
        Page<Article> cached = redisCache.get(cacheKey);
        if (cached != null) {
            log.debug("Article list cache hit: {}", cacheKey);
            return cached;
        }

        Page<Article> pageParam = new Page<>(page, size);
        String statusFilter = status != null ? status : "PUBLISHED";
        Page<Article> result = articleMapper.selectPageWithCategory(pageParam, categoryId, tagId, articleType, statusFilter);

        if (result != null && result.getRecords() != null) {
            for (Article article : result.getRecords()) {
                List<Tag> tags = tagMapper.selectByArticleId(article.getId());
                article.setTags(tags);
            }
        }

        redisCache.set(cacheKey, result, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Article list cached: {}", cacheKey);
        return result;
    }

    @Override
    public Article getById(Long id) {
        String cacheKey = ARTICLE_DETAIL_KEY + ":" + id;
        Article cached = redisCache.get(cacheKey);
        if (cached != null) {
            log.debug("Article detail cache hit: {}", cacheKey);
            return cached;
        }

        Article article = articleMapper.selectByIdWithCategory(id);
        if (article == null) {
            throw new BlogException(ErrorCode.ARTICLE_NOT_FOUND.getCode(), ErrorCode.ARTICLE_NOT_FOUND.getMessage());
        }
        article.setTags(tagMapper.selectByArticleId(id));
        redisCache.set(cacheKey, article, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Article detail cached: {}", cacheKey);
        return article;
    }

    @Override
    public Article getBySlug(String slug) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>()
                .eq(Article::getSlug, slug));
        if (article == null) {
            throw new BlogException(ErrorCode.ARTICLE_NOT_FOUND.getCode(), ErrorCode.ARTICLE_NOT_FOUND.getMessage());
        }
        article.setTags(tagMapper.selectByArticleId(article.getId()));
        return article;
    }

    @Override
    @Transactional
    public Article create(Article article, List<Long> tagIds) {
        articleMapper.insert(article);
        saveArticleTags(article.getId(), tagIds);
        clearArticleCache();
        log.info("Article created, cache cleared. id={}", article.getId());
        return article;
    }

    @Override
    @Transactional
    public Article update(Article article, List<Long> tagIds) {
        articleMapper.updateById(article);
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getArticleId, article.getId()));
        saveArticleTags(article.getId(), tagIds);
        clearArticleCache();
        redisCache.delete(ARTICLE_DETAIL_KEY + ":" + article.getId());
        log.info("Article updated, cache cleared. id={}", article.getId());
        return article;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
        clearArticleCache();
        redisCache.delete(ARTICLE_DETAIL_KEY + ":" + id);
        log.info("Article deleted, cache cleared. id={}", id);
    }

    @Override
    public void incrementViewCount(Long id) {
        articleMapper.incrementViewCount(id);
    }

    @Override
    public List<Article> listByTagId(Long tagId) {
        return articleMapper.selectByTagId(tagId);
    }

    private void clearArticleCache() {
        redisCache.deleteByPattern(ARTICLE_LIST_KEY + ":*");
        log.info("All article list cache cleared");
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (!CollectionUtils.isEmpty(tagIds)) {
            for (Long tagId : tagIds) {
                ArticleTag at = new ArticleTag();
                at.setArticleId(articleId);
                at.setTagId(tagId);
                articleTagMapper.insert(at);
            }
        }
    }
}
