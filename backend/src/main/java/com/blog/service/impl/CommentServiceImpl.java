package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final RedisCache redisCache;

    private static final String COMMENT_KEY = "comment:article";
    private static final String ARTICLE_DETAIL_KEY = "article:detail";
    private static final long CACHE_TTL = 30;

    private void syncArticleCommentCount(Long articleId) {
        if (articleId == null) return;
        long count = commentMapper.selectCount(
            new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, "APPROVED")
        );
        Article article = new Article();
        article.setId(articleId);
        article.setCommentCount((int) count);
        articleMapper.updateById(article);
        redisCache.delete(ARTICLE_DETAIL_KEY + ":" + articleId);
        redisCache.deleteByPattern("article:list:*");
        log.debug("Article comment count synced: articleId={}, count={}", articleId, count);
    }

    @Override
    public List<Comment> listByArticleId(Long articleId) {
        String cacheKey = COMMENT_KEY + ":" + articleId;
        @SuppressWarnings("unchecked")
        List<Comment> cached = redisCache.get(cacheKey);
        if (cached != null) {
            log.debug("Comment cache hit: {}", cacheKey);
            return cached;
        }

        List<Comment> rootComments = commentMapper.selectRootCommentsByArticleId(articleId);
        for (Comment comment : rootComments) {
            comment.setReplies(commentMapper.selectRepliesByParentId(comment.getId()));
        }
        redisCache.set(cacheKey, rootComments, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Comment cache set: {}", cacheKey);
        return rootComments;
    }

    @Override
    public Comment create(Comment comment) {
        commentMapper.insert(comment);
        String cacheKey = COMMENT_KEY + ":" + comment.getArticleId();
        redisCache.delete(cacheKey);
        syncArticleCommentCount(comment.getArticleId());
        log.info("Comment created, cache cleared: {}", cacheKey);
        return comment;
    }

    @Override
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        commentMapper.deleteById(id);
        if (comment != null) {
            String cacheKey = COMMENT_KEY + ":" + comment.getArticleId();
            redisCache.delete(cacheKey);
            syncArticleCommentCount(comment.getArticleId());
            log.info("Comment deleted, cache cleared: {}", cacheKey);
        }
    }

    @Override
    public List<Comment> listAll() {
        List<Comment> comments = commentMapper.selectAllWithArticleInfo();
        Map<Long, String> titleMap = new HashMap<>();
        for (Comment c : comments) {
            if (c.getArticleId() != null && !titleMap.containsKey(c.getArticleId())) {
                Article article = articleMapper.selectById(c.getArticleId());
                titleMap.put(c.getArticleId(), article != null ? article.getTitle() : null);
            }
        }
        for (Comment c : comments) {
            c.setArticleTitle(titleMap.get(c.getArticleId()));
        }
        return comments;
    }

    @Override
    public void updateStatus(Long id, String status) {
        Comment existing = commentMapper.selectById(id);
        if (existing == null) {
            return;
        }
        String oldStatus = existing.getStatus();
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
            .eq(Comment::getId, id)
            .set(Comment::getStatus, status));
        String cacheKey = COMMENT_KEY + ":" + existing.getArticleId();
        redisCache.delete(cacheKey);
        if (!"APPROVED".equals(oldStatus) && "APPROVED".equals(status)) {
            syncArticleCommentCount(existing.getArticleId());
        } else if ("APPROVED".equals(oldStatus) && !"APPROVED".equals(status)) {
            syncArticleCommentCount(existing.getArticleId());
        }
        log.info("Comment status updated: id={}, {} -> {}", id, oldStatus, status);
    }
}
