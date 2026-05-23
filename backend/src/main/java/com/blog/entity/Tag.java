package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tag")
public class Tag {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String slug;
    private Long categoryId;
    private String color;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Integer articleCount;

    @TableField(exist = false)
    private Long articleId;
}
