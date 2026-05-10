package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.utils.RedisCache;
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
    private static final long CACHE_TTL = 30;

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
        Comment comment = new Comment();
        comment.setId(id);
        comment.setStatus(status);
        commentMapper.updateById(comment);
        Comment existing = commentMapper.selectById(id);
        if (existing != null) {
            String cacheKey = COMMENT_KEY + ":" + existing.getArticleId();
            redisCache.delete(cacheKey);
            log.info("Comment status updated, cache cleared: {}", cacheKey);
        }
    }
}
