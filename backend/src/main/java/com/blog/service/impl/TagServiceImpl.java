package com.blog.service.impl;

import com.blog.entity.Tag;
import com.blog.exception.BlogException;
import com.blog.exception.ErrorCode;
import com.blog.mapper.TagMapper;
import com.blog.service.TagService;
import com.blog.utils.RedisCache;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final RedisCache redisCache;

    private static final String TAG_ALL_KEY = "tag:all";
    private static final long CACHE_TTL = 60;

    // 预设标签颜色池 — 高区分度、不刺眼、白底可读
    private static final String[] TAG_COLORS = {
        "#409EFF", // 蓝
        "#13C2C2", // 青
        "#52C41A", // 绿
        "#FA8C16", // 橙
        "#F5222D", // 红
        "#722ED1", // 紫
        "#EB2F96", // 玫红
        "#2F54EB", // 靛蓝
        "#FAAD14", // 金黄
        "#1890FF", // 亮蓝
        "#52C41A", // 草绿
        "#FA541C", // 橙红
    };
    private static final Random RANDOM = new Random();

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

    @Override
    public void delete(Long id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BlogException(ErrorCode.BAD_REQUEST.getCode(), "标签不存在");
        }
        tagMapper.deleteById(id);
        redisCache.delete(TAG_ALL_KEY);
        redisCache.deleteByPattern("article:list:*");
        redisCache.deleteByPattern("article:detail:*");
        log.info("Tag deleted: id={}, name={}", id, tag.getName());
    }

    @Override
    public Tag create(Tag tag) {
        String name = tag.getName().trim();
        tag.setName(name);
        if (tag.getSlug() == null || tag.getSlug().trim().isEmpty()) {
            tag.setSlug(name.toLowerCase().replaceAll("\\s+", "-"));
        }
        if (tag.getColor() == null || tag.getColor().trim().isEmpty()) {
            tag.setColor(TAG_COLORS[RANDOM.nextInt(TAG_COLORS.length)]);
        }
        Tag existsByName = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, name));
        if (existsByName != null) {
            throw new BlogException(ErrorCode.BAD_REQUEST.getCode(), "标签 \"" + name + "\" 已存在");
        }
        Tag existsBySlug = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getSlug, tag.getSlug()));
        if (existsBySlug != null) {
            throw new BlogException(ErrorCode.BAD_REQUEST.getCode(), "标签别名 \"" + tag.getSlug() + "\" 已存在");
        }
        tagMapper.insert(tag);
        redisCache.delete(TAG_ALL_KEY);
        log.info("Tag created, cache cleared. id={}, name={}", tag.getId(), tag.getName());
        return tag;
    }
}
