package com.blog.service;

import com.blog.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {

    private final ArticleService articleService;
    private final RedisCache redisCache;

    private static final String ARTICLE_DETAIL_KEY = "article:detail";

    /**
     * 异步更新文章浏览量，并删除对应缓存保持数据一致。
     * 从同步调用改为异步，消除 UPDATE 操作对读请求的阻塞。
     */
    @Async
    public void incrementViewCountAsync(Long articleId) {
        try {
            articleService.incrementViewCount(articleId);
            // 删除缓存，下次读取时从数据库获取最新 viewCount
            redisCache.delete(ARTICLE_DETAIL_KEY + ":" + articleId);
            log.debug("Async view count updated for article: {}", articleId);
        } catch (Exception e) {
            log.error("Failed to async increment view count for article: {}", articleId, e);
        }
    }
}
