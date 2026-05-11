package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.SiteConfig;
import com.blog.mapper.SiteConfigMapper;
import com.blog.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SiteConfigServiceImpl implements SiteConfigService {

    private final SiteConfigMapper siteConfigMapper;

    @Override
    public Map<String, String> getAll() {
        List<SiteConfig> list = siteConfigMapper.selectList(null);
        Map<String, String> result = new LinkedHashMap<>();
        for (SiteConfig sc : list) {
            result.put(sc.getConfigKey(), sc.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional
    public Map<String, String> updateAll(Map<String, String> updates) {
        if (updates == null || updates.isEmpty()) {
            return getAll();
        }
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            String value = entry.getValue();
            SiteConfig existing = siteConfigMapper.selectOne(
                    new LambdaQueryWrapper<SiteConfig>().eq(SiteConfig::getConfigKey, key));
            if (existing == null) {
                SiteConfig sc = new SiteConfig();
                sc.setConfigKey(key);
                sc.setConfigValue(value);
                siteConfigMapper.insert(sc);
            } else {
                existing.setConfigValue(value);
                siteConfigMapper.updateById(existing);
            }
        }
        return getAll();
    }
}
