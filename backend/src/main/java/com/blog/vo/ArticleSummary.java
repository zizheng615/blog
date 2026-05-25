package com.blog.vo;

import com.blog.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章列表专用 VO，排除 content/contentMd 大字段，减少网络传输和内存占用。
 */
@Data
public class ArticleSummary {

    private Long id;
    private String title;
    private String slug;
    private String summary;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 关联分类，列表展示用 */
    private Category category;
}
