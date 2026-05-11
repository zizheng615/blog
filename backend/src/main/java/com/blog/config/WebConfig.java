package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@ServletComponentScan("com.blog.interceptor")
public class WebConfig implements WebMvcConfigurer {

    @Value("${blog.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${blog.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolute = Paths.get(uploadDir).toAbsolutePath().toString().replace('\\', '/');
        if (!absolute.endsWith("/")) {
            absolute += "/";
        }
        String pattern = urlPrefix.endsWith("/") ? urlPrefix + "**" : urlPrefix + "/**";
        registry.addResourceHandler(pattern)
                .addResourceLocations("file:" + absolute);
    }
}
