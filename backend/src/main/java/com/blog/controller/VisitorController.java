package com.blog.controller;

import com.blog.dto.VisitorDTO;
import com.blog.dto.VisitorStats;
import com.blog.entity.Visitor;
import com.blog.service.VisitorService;
import com.blog.utils.HtmlUtils;
import com.blog.utils.IpUtil;
import com.blog.utils.Result;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VisitorController {

    private static final int FIELD_MAX = 512;

    private final VisitorService visitorService;

    @PostMapping("/visitors/record")
    public Result<Void> record(@RequestBody VisitorDTO dto, HttpServletRequest request) {
        Visitor visitor = new Visitor();
        visitor.setIpAddress(HtmlUtils.truncate(IpUtil.getIpAddress(request), 45));
        visitor.setUserAgent(HtmlUtils.truncate(request.getHeader("User-Agent"), FIELD_MAX));
        visitor.setPageUrl(HtmlUtils.truncate(dto.getPageUrl(), FIELD_MAX));
        visitor.setReferer(HtmlUtils.truncate(dto.getReferer(), FIELD_MAX));
        visitor.setVisitDate(LocalDate.now());
        visitorService.record(visitor);
        return Result.success();
    }

    @GetMapping("/visitors/stats")
    public Result<VisitorStats> getStats() {
        return Result.success(visitorService.getStats());
    }
}
