package com.bbs.config;

import com.bbs.interceptor.JwtAuthenticationInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.io.File;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebMvcConfig.class);

    @Resource
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    @Value("${bbs.upload.path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传文件映射：/upload/** → 实际文件系统路径
        String location = "file:" + uploadPath;
        if (!location.endsWith("/") && !location.endsWith("\\")) {
            location += "/";
        }
        // 统一用正斜杠
        location = location.replace("\\", "/");
        log.info("上传资源映射: /upload/** → {}", location);
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);

        // 静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}