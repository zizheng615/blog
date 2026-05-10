package com.blog.service.impl;

import com.blog.entity.Tag;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.blog.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final RedisCache redisCache;

    private static final String TAG_ALL_KEY = "tag:all";
    private static final long CACHE_TTL = 60;

    @Override
    public List<Tag> listAll() {
        @SuppressWarnings("unchecked")
        List<Tag> cached = redisCache.get(TAG_ALL_KEY);
        if (cached != null) {
            log.debug("Tag cache hit");
            return cached;
        }
        List<Tag> result = tagMapper.selectAllWithCount();
        redisCache.set(TAG_ALL_KEY, result, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Tag cache set");
        return result;
    }

    @Override
    public List<Tag> listByArticleId(Long articleId) {
        return tagMapper.selectByArticleId(articleId);
    }

    @Override
    public Tag getById(Long id) {
        return tagMapper.selectById(id);
    }

    @Override
    public Tag getBySlug(String slug) {
        return tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getSlug, slug));
    }
}
