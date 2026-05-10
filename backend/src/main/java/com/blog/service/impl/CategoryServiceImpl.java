package com.blog.service.impl;

import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.blog.utils.RedisCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RedisCache redisCache;

    private static final String CATEGORY_ALL_KEY = "category:all";
    private static final long CACHE_TTL = 60;

    @Override
    public List<Category> listAll() {
        @SuppressWarnings("unchecked")
        List<Category> cached = redisCache.get(CATEGORY_ALL_KEY);
        if (cached != null) {
            log.debug("Category cache hit");
            return cached;
        }
        List<Category> result = categoryMapper.selectAllWithCount();
        redisCache.set(CATEGORY_ALL_KEY, result, CACHE_TTL, TimeUnit.MINUTES);
        log.debug("Category cache set");
        return result;
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }
}
