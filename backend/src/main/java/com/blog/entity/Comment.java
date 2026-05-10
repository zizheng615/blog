package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private Long parentId;
    private String nickname;
    private String email;
    private String content;
    private String ipAddress;
    private String userAgent;
    private Boolean isAdmin;
    private String status;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private List<Comment> replies;

    @TableField(value = "article_title", exist = false)
    private String articleTitle;
}
