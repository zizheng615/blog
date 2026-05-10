package com.blog.service;

import com.blog.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> listByArticleId(Long articleId);
    Comment create(Comment comment);
    void delete(Long id);
    List<Comment> listAll();
    void updateStatus(Long id, String status);
}
