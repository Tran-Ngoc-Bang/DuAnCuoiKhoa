package com.fpoly.shared_learning_materials.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadConfig {
    
    @Value("${app.upload.dir:src/main/resources/static/uploads/documents}")
    private String uploadDir;
    
    public String getUploadDir() {
        return uploadDir;
    }
    
    public String getDocumentsDir() {
        return uploadDir;
    }
    
    public String getRelativePath(String fileName) {
        return "uploads/documents/" + fileName;
    }
}