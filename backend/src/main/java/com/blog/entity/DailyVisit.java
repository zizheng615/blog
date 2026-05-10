package com.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_visit")
public class DailyVisit {
    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate visitDate;
    private Integer pvCount;
    private Integer uvCount;
    private LocalDateTime createdAt;
}
