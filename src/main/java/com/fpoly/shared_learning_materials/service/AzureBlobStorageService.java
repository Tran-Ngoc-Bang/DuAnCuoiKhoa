package com.fpoly.shared_learning_materials.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AzureBlobStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Value("${azure.storage.account-name}")
    private String accountName;

    @Value("${azure.storage.account-key}")
    private String accountKey;

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient containerClient;

    private BlobServiceClient getBlobServiceClient() {
        if (blobServiceClient == null) {
            blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }
        return blobServiceClient;
    }

    private BlobContainerClient getContainerClient() {
        if (containerClient == null) {
            containerClient = getBlobServiceClient().getBlobContainerClient(containerName);
            // Create container if it doesn't exist
            if (!containerClient.exists()) {
                containerClient.create();
                log.info("Created Azure Blob Storage container: {}", containerName);
            }
        }
        return containerClient;
    }

    /**
     * Upload file to Azure Blob Storage
     * 
     * @param file MultipartFile to upload
     * @return Blob URL of uploaded file
     */
    public String uploadFile(MultipartFile file) throws IOException {
        try {
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            BlobClient blobClient = getContainerClient().getBlobClient(fileName);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());

            // Upload file
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            log.info("File uploaded successfully: {} ({} bytes)", fileName, file.getSize());
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Upload file from byte array
     * 
     * @param data        byte array data
     * @param fileName    original file name
     * @param contentType content type
     * @return Blob URL of uploaded file
     */
    public String uploadFile(byte[] data, String fileName, String contentType) throws IOException {
        try {
            String uniqueFileName = generateUniqueFileName(fileName);
            BlobClient blobClient = getContainerClient().getBlobClient(uniqueFileName);

            // Set content type
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(contentType);

            // Upload file
            blobClient.upload(new ByteArrayInputStream(data), data.length, true);
            blobClient.setHttpHeaders(headers);

            log.info("File uploaded successfully: {} ({} bytes)", uniqueFileName, data.length);
            return blobClient.getBlobUrl();
        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to Azure Blob Storage", e);
        }
    }

    /**
     * Download file from Azure Blob Storage
     * 
     * @param blobUrl Blob URL
     * @return InputStream of the file
     */
    public InputStream downloadFile(String blobUrl) throws IOException {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = getContainerClient().getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new IOException("File not found: " + blobName);
            }

            log.info("File downloaded successfully: {}", blobName);
            return blobClient.openInputStream();
        } catch (Exception e) {
            log.error("Error downloading file from Azure Blob Storage: {}", e.getMessage(), e);
            throw new IOException("Failed to download file from Azure Blob Storage", e);
        }
    }

    /**
     * Delete file from Azure Blob Storage
     * 
     * @param blobUrl Blob URL
     * @return true if deleted successfully
     */
    public boolean deleteFile(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = getContainerClient().getBlobClient(blobName);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info("File deleted successfully: {}", blobName);
                return true;
            } else {
                log.warn("File not found for deletion: {}", blobName);
                return false;
            }
        } catch (Exception e) {
            log.error("Error deleting file from Azure Blob Storage: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Generate secure download URL with SAS token
     * 
     * @param blobUrl           Blob URL
     * @param expirationMinutes expiration time in minutes
     * @return Secure download URL with SAS token
     */
    public String generateSecureDownloadUrl(String blobUrl, int expirationMinutes) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = getContainerClient().getBlobClient(blobName);

            if (!blobClient.exists()) {
                throw new IOException("File not found: " + blobName);
            }

            // Create SAS token
            BlobSasPermission permission = new BlobSasPermission()
                    .setReadPermission(true);

            BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(
                    OffsetDateTime.now().plusMinutes(expirationMinutes),
                    permission);

            String sasToken = blobClient.generateSas(sasSignatureValues);
            String secureUrl = blobClient.getBlobUrl() + "?" + sasToken;

            log.info("Generated secure download URL for: {} (expires in {} minutes)", blobName, expirationMinutes);
            return secureUrl;
        } catch (Exception e) {
            log.error("Error generating secure download URL: {}", e.getMessage(), e);
            return blobUrl; // Return original URL if SAS generation fails
        }
    }

    /**
     * Check if file exists in Azure Blob Storage
     * 
     * @param blobUrl Blob URL
     * @return true if file exists
     */
    public boolean fileExists(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = getContainerClient().getBlobClient(blobName);
            return blobClient.exists();
        } catch (Exception e) {
            log.error("Error checking file existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get file size from Azure Blob Storage
     * 
     * @param blobUrl Blob URL
     * @return file size in bytes, -1 if not found
     */
    public long getFileSize(String blobUrl) {
        try {
            String blobName = extractBlobNameFromUrl(blobUrl);
            BlobClient blobClient = getContainerClient().getBlobClient(blobName);

            if (blobClient.exists()) {
                return blobClient.getProperties().getBlobSize();
            }
            return -1;
        } catch (Exception e) {
            log.error("Error getting file size: {}", e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Generate unique file name to avoid conflicts
     * 
     * @param originalFileName original file name
     * @return unique file name
     */
    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extract blob name from blob URL
     * 
     * @param blobUrl full blob URL
     * @return blob name
     */
    private String extractBlobNameFromUrl(String blobUrl) {
        if (blobUrl == null || blobUrl.isEmpty()) {
            throw new IllegalArgumentException("Blob URL cannot be null or empty");
        }

        // Remove query parameters if present
        String urlWithoutQuery = blobUrl.split("\\?")[0];

        // Extract blob name from URL
        String[] parts = urlWithoutQuery.split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid blob URL format");
        }

        // Find the container name and get everything after it
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals(containerName)) {
                StringBuilder blobName = new StringBuilder();
                for (int j = i + 1; j < parts.length; j++) {
                    if (j > i + 1)
                        blobName.append("/");
                    blobName.append(parts[j]);
                }
                return blobName.toString();
            }
        }

        throw new IllegalArgumentException("Container name not found in blob URL");
    }

    /**
     * Check Azure Blob Storage connectivity
     * 
     * @return true if connection is successful
     */
    public boolean testConnection() {
        try {
            getContainerClient();
            log.info("Azure Blob Storage connection test successful");
            return true;
        } catch (Exception e) {
            log.error("Azure Blob Storage connection test failed: {}", e.getMessage(), e);
            return false;
        }
    }
}