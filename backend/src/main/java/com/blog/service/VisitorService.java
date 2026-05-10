package com.blog.service;

import com.blog.dto.VisitorStats;
import com.blog.entity.Visitor;

public interface VisitorService {
    void record(Visitor visitor);
    VisitorStats getStats();
}
