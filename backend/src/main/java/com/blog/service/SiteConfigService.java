package com.blog.service;

import java.util.Map;

public interface SiteConfigService {
    Map<String, String> getAll();

    Map<String, String> updateAll(Map<String, String> updates);
}
