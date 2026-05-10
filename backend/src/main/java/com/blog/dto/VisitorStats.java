package com.blog.dto;

import lombok.Data;

@Data
public class VisitorStats {
    private Long totalPv;
    private Long totalUv;
    private Long todayPv;
    private Long todayUv;
}
