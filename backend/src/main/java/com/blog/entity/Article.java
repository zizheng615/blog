package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String slug;
    private String summary;
    private String content;
    private String contentMd;
    private String coverImage;
    private Long categoryId;
    private Long authorId;
    private String articleType;
    private String status;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isTop;
    private LocalDateTime publishedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Category category;

    @TableField(exist = false)
    private List<Tag> tags;

    @TableField(exist = false)
    private User author;
}
