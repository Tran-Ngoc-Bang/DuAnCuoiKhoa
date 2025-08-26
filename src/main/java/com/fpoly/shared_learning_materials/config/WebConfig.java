package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.interceptor.UserStatusInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserStatusInterceptor userStatusInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from classpath
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");

        // Also serve from file system for development
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/static/uploads/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStatusInterceptor)
                .addPathPatterns("/**") // Áp dụng cho tất cả request
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/confirm",
                        "/forgot-password",
                        "/verify-reset-code",
                        "/reset-password",
                        "/assets/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/.well-known/**",
                        "/payment/**",
                        "/coin-packages/**",
                        "/error/**"); // Loại trừ các trang public
    }
}