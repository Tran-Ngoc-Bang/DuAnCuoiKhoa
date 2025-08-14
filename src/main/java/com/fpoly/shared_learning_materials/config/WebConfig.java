package com.fpoly.shared_learning_materials.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files from classpath
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/");
        
        // Also serve from file system for development
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:src/main/resources/static/uploads/");
    }
}