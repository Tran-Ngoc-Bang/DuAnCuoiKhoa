package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DocumentAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalyticsService.class);

    @Autowired
    private DocumentRepository documentRepository;

    // Cache để tránh spam view/download trong cùng session
    private final ConcurrentHashMap<String, LocalDateTime> viewCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> downloadCache = new ConcurrentHashMap<>();

    // Thời gian cache (5 phút)
    private static final int CACHE_DURATION_MINUTES = 5;

    /**
     * Tăng lượt xem cho document
     */
    @Transactional
    public boolean incrementViewCount(Long documentId, String sessionId, String ipAddress) {
        try {
            // Tạo cache key
            String cacheKey = documentId + "_" + sessionId + "_" + ipAddress;

            // Kiểm tra cache để tránh spam
            if (isCached(cacheKey, viewCache)) {
                logger.debug("View count request cached for document: {}, session: {}", documentId, sessionId);
                return false;
            }

            // Cập nhật cache
            viewCache.put(cacheKey, LocalDateTime.now());

            // Tăng view count trong database
            int updatedRows = documentRepository.incrementViewCount(documentId);

            if (updatedRows > 0) {
                logger.info("Incremented view count for document: {}", documentId);
                return true;
            } else {
                logger.warn("Failed to increment view count for document: {}", documentId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error incrementing view count for document: {}", documentId, e);
            return false;
        }
    }

    /**
     * Tăng lượt tải cho document
     */
    @Transactional
    public boolean incrementDownloadCount(Long documentId, String sessionId, String ipAddress) {
        try {
            // Tạo cache key
            String cacheKey = documentId + "_" + sessionId + "_" + ipAddress;

            // Kiểm tra cache để tránh spam
            if (isCached(cacheKey, downloadCache)) {
                logger.debug("Download count request cached for document: {}, session: {}", documentId, sessionId);
                return false;
            }

            // Cập nhật cache
            downloadCache.put(cacheKey, LocalDateTime.now());

            // Tăng download count trong database
            int updatedRows = documentRepository.incrementDownloadCount(documentId);

            if (updatedRows > 0) {
                logger.info("Incremented download count for document: {}", documentId);
                return true;
            } else {
                logger.warn("Failed to increment download count for document: {}", documentId);
                return false;
            }

        } catch (Exception e) {
            logger.error("Error incrementing download count for document: {}", documentId, e);
            return false;
        }
    }

    /**
     * Lấy thống kê view/download cho document
     */
    @Transactional(readOnly = true)
    public DocumentStats getDocumentStats(Long documentId) {
        try {
            Document document = documentRepository.findById(documentId).orElse(null);
            if (document == null) {
                return null;
            }

            return DocumentStats.builder()
                    .documentId(documentId)
                    .viewsCount(document.getViewsCount() != null ? document.getViewsCount() : 0L)
                    .downloadsCount(document.getDownloadsCount() != null ? document.getDownloadsCount() : 0L)
                    .build();

        } catch (Exception e) {
            logger.error("Error getting document stats for document: {}", documentId, e);
            return null;
        }
    }

    /**
     * Kiểm tra xem request có bị cache không
     */
    private boolean isCached(String cacheKey, ConcurrentHashMap<String, LocalDateTime> cache) {
        LocalDateTime lastRequest = cache.get(cacheKey);
        if (lastRequest == null) {
            return false;
        }

        // Kiểm tra xem có quá thời gian cache không
        LocalDateTime cacheExpiry = lastRequest.plusMinutes(CACHE_DURATION_MINUTES);
        if (LocalDateTime.now().isAfter(cacheExpiry)) {
            cache.remove(cacheKey);
            return false;
        }

        return true;
    }

    /**
     * Dọn dẹp cache định kỳ
     */
    public void cleanupCache() {
        LocalDateTime now = LocalDateTime.now();

        // Dọn view cache
        viewCache.entrySet().removeIf(entry -> now.isAfter(entry.getValue().plusMinutes(CACHE_DURATION_MINUTES)));

        // Dọn download cache
        downloadCache.entrySet().removeIf(entry -> now.isAfter(entry.getValue().plusMinutes(CACHE_DURATION_MINUTES)));

        logger.debug("Cache cleanup completed. View cache size: {}, Download cache size: {}",
                viewCache.size(), downloadCache.size());
    }

    /**
     * Inner class để chứa thống kê document
     */
    public static class DocumentStats {
        private Long documentId;
        private Long viewsCount;
        private Long downloadsCount;

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private DocumentStats stats = new DocumentStats();

            public Builder documentId(Long documentId) {
                stats.documentId = documentId;
                return this;
            }

            public Builder viewsCount(Long viewsCount) {
                stats.viewsCount = viewsCount;
                return this;
            }

            public Builder downloadsCount(Long downloadsCount) {
                stats.downloadsCount = downloadsCount;
                return this;
            }

            public DocumentStats build() {
                return stats;
            }
        }

        // Getters
        public Long getDocumentId() {
            return documentId;
        }

        public Long getViewsCount() {
            return viewsCount;
        }

        public Long getDownloadsCount() {
            return downloadsCount;
        }
    }
}