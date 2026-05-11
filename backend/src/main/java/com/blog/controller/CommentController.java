package com.blog.controller;

import com.blog.entity.Comment;
import com.blog.service.CommentService;
import com.blog.utils.HtmlUtils;
import com.blog.utils.IpUtil;
import com.blog.utils.Result;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/articles/{articleId}/comments")
    public Result<List<Comment>> listByArticleId(@PathVariable Long articleId) {
        return Result.success(commentService.listByArticleId(articleId));
    }

    @PostMapping("/articles/{articleId}/comments")
    public Result<Comment> create(@PathVariable Long articleId, @RequestBody Comment comment,
                                     HttpServletRequest request) {
        comment.setArticleId(articleId);
        comment.setIpAddress(HtmlUtils.truncate(IpUtil.getIpAddress(request), 45));
        comment.setUserAgent(HtmlUtils.truncate(request.getHeader("User-Agent"), 512));
        if (comment.getContent() != null) {
            comment.setContent(HtmlUtils.sanitize(comment.getContent()));
        }
        return Result.success(commentService.create(comment));
    }

    @GetMapping("/admin/comments")
    public Result<List<Comment>> listAll() {
        return Result.success(commentService.listAll());
    }

    @PutMapping("/admin/comments/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        commentService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/admin/comments/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
