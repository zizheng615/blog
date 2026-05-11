package com.blog.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/upload")
public class UploadController {

    @Value("${blog.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${blog.upload.url-prefix:/uploads}")
    private String urlPrefix;

    private static final Set<String> IMAGE_EXT = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"));
    private static final Set<String> VIDEO_EXT = new HashSet<>(Arrays.asList(
            "mp4", "webm", "ogg", "mov"));

    private static final long IMAGE_MAX_BYTES = 10L * 1024 * 1024; // 10MB
    private static final long VIDEO_MAX_BYTES = 100L * 1024 * 1024; // 100MB

    @PostMapping("/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        return doUpload(file, IMAGE_EXT, IMAGE_MAX_BYTES, "images");
    }

    @PostMapping("/video")
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file) {
        return doUpload(file, VIDEO_EXT, VIDEO_MAX_BYTES, "videos");
    }

    private Map<String, Object> doUpload(MultipartFile file, Set<String> allowedExt,
                                         long maxBytes, String subDir) {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("errno", 1);
            result.put("message", "文件不能为空");
            return result;
        }
        if (file.getSize() > maxBytes) {
            result.put("errno", 1);
            result.put("message", "文件超出大小限制");
            return result;
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!allowedExt.contains(ext)) {
            result.put("errno", 1);
            result.put("message", "不支持的文件类型");
            return result;
        }

        try {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            Path dir = Paths.get(uploadDir, subDir, datePath).toAbsolutePath();
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);

            String url = urlPrefix + "/" + subDir + "/" + datePath + "/" + filename;
            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            if ("images".equals(subDir)) {
                data.put("alt", "");
                data.put("href", url);
            }
            result.put("errno", 0);
            result.put("data", data);
            return result;
        } catch (IOException e) {
            log.error("Upload failed", e);
            result.put("errno", 1);
            result.put("message", "上传失败");
            return result;
        }
    }
}
