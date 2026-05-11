package com.blog.controller;

import com.blog.service.SiteConfigService;
import com.blog.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    @GetMapping("/site-config")
    public Result<Map<String, String>> getPublic() {
        return Result.success(siteConfigService.getAll());
    }

    @GetMapping("/admin/site-config")
    public Result<Map<String, String>> getAdmin() {
        return Result.success(siteConfigService.getAll());
    }

    @PutMapping("/admin/site-config")
    public Result<Map<String, String>> update(@RequestBody Map<String, String> body) {
        return Result.success(siteConfigService.updateAll(body));
    }
}
