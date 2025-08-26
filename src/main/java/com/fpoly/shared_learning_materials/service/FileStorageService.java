package com.fpoly.shared_learning_materials.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir:src/main/resources/static/uploads/documents}")
    private String uploadDir;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Autowired(required = false)
    private AzureBlobStorageService azureBlobStorageService;

    /**
     * Upload file to storage (local or Azure based on profile)
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null) {
            return uploadToAzure(file);
        } else {
            return uploadToLocal(file);
        }
    }

    /**
     * Upload file to Azure Blob Storage
     */
    private String uploadToAzure(MultipartFile file) throws IOException {
        try {
            String blobUrl = azureBlobStorageService.uploadFile(file);
            log.info("File uploaded to Azure Blob Storage: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            log.error("Failed to upload to Azure Blob Storage, falling back to local storage", e);
            return uploadToLocal(file);
        }
    }

    /**
     * Upload file to local storage
     */
    private String uploadToLocal(MultipartFile file) throws IOException {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/documents/file/" + uniqueFilename;
            log.info("File uploaded to local storage: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("Failed to upload file to local storage", e);
            throw e;
        }
    }

    /**
     * Download file from storage
     */
    public InputStream downloadFile(String fileUrl) throws IOException {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null && isAzureBlobUrl(fileUrl)) {
            return downloadFromAzure(fileUrl);
        } else {
            return downloadFromLocal(fileUrl);
        }
    }

    /**
     * Download file from Azure Blob Storage
     */
    private InputStream downloadFromAzure(String blobUrl) throws IOException {
        try {
            return azureBlobStorageService.downloadFile(blobUrl);
        } catch (Exception e) {
            log.error("Failed to download from Azure Blob Storage", e);
            throw new IOException("Failed to download file from Azure Blob Storage", e);
        }
    }

    /**
     * Download file from local storage
     */
    private InputStream downloadFromLocal(String fileUrl) throws IOException {
        try {
            // Remove leading slash and get relative path
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath);

            if (!Files.exists(filePath)) {
                throw new IOException("File not found: " + filePath);
            }

            return Files.newInputStream(filePath);
        } catch (IOException e) {
            log.error("Failed to download file from local storage", e);
            throw e;
        }
    }

    /**
     * Delete file from storage
     */
    public boolean deleteFile(String fileUrl) {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null && isAzureBlobUrl(fileUrl)) {
            return deleteFromAzure(fileUrl);
        } else {
            return deleteFromLocal(fileUrl);
        }
    }

    /**
     * Delete file from Azure Blob Storage
     */
    private boolean deleteFromAzure(String blobUrl) {
        try {
            return azureBlobStorageService.deleteFile(blobUrl);
        } catch (Exception e) {
            log.error("Failed to delete from Azure Blob Storage", e);
            return false;
        }
    }

    /**
     * Delete file from local storage
     */
    private boolean deleteFromLocal(String fileUrl) {
        try {
            // Remove leading slash and get relative path
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted from local storage: {}", filePath);
                return true;
            } else {
                log.warn("File not found for deletion: {}", filePath);
                return false;
            }
        } catch (IOException e) {
            log.error("Failed to delete file from local storage", e);
            return false;
        }
    }

    /**
     * Generate secure download URL
     */
    public String generateSecureDownloadUrl(String fileUrl, int expirationMinutes) {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null && isAzureBlobUrl(fileUrl)) {
            try {
                return azureBlobStorageService.generateSecureDownloadUrl(fileUrl, expirationMinutes);
            } catch (Exception e) {
                log.error("Failed to generate secure download URL for Azure", e);
                return fileUrl; // Return original URL if failed
            }
        } else {
            // For local storage, return the original URL
            return fileUrl;
        }
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String fileUrl) {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null && isAzureBlobUrl(fileUrl)) {
            return azureBlobStorageService.fileExists(fileUrl);
        } else {
            try {
                String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
                Path filePath = Paths.get(relativePath);
                return Files.exists(filePath);
            } catch (Exception e) {
                log.error("Error checking file existence", e);
                return false;
            }
        }
    }

    /**
     * Get file size
     */
    public long getFileSize(String fileUrl) {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null && isAzureBlobUrl(fileUrl)) {
            return azureBlobStorageService.getFileSize(fileUrl);
        } else {
            try {
                String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
                Path filePath = Paths.get(relativePath);
                if (Files.exists(filePath)) {
                    return Files.size(filePath);
                }
                return -1;
            } catch (IOException e) {
                log.error("Error getting file size", e);
                return -1;
            }
        }
    }

    /**
     * Check if URL is Azure Blob Storage URL
     */
    private boolean isAzureBlobUrl(String url) {
        return url != null && (url.contains("blob.core.windows.net") || url.startsWith("https://"));
    }

    /**
     * Test storage connectivity
     */
    public boolean testStorageConnection() {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null) {
            return azureBlobStorageService.testConnection();
        } else {
            try {
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                return Files.isWritable(uploadPath);
            } catch (IOException e) {
                log.error("Local storage test failed", e);
                return false;
            }
        }
    }

    /**
     * Get storage type
     */
    public String getStorageType() {
        if ("azure".equals(activeProfile) && azureBlobStorageService != null) {
            return "Azure Blob Storage";
        } else {
            return "Local Storage";
        }
    }
}