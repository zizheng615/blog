package com.blog.service;

import com.blog.entity.Tag;
import java.util.List;

public interface TagService {
    List<Tag> listAll();
    List<Tag> listByArticleId(Long articleId);
    Tag getById(Long id);
    Tag getBySlug(String slug);
    Tag create(Tag tag);
    void delete(Long id);
}
