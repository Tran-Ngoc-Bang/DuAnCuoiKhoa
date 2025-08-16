package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.fpoly.shared_learning_materials.domain.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    long countByStatus(String status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    Optional<Document> findBySlug(String slug);

    Optional<Document> findBySlugAndDeletedAtIsNull(String slug);

    Page<Document> findAll(Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.slug = :slug")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Document> findBySlugWithLock(@Param("slug") String slug);

    // Đếm tài liệu miễn phí (price = 0 hoặc null)
    @Query("SELECT COUNT(d) FROM Document d WHERE (d.price = 0 OR d.price IS NULL) AND d.deletedAt IS NULL")
    long countFreeDocuments();

    // Đếm tài liệu premium (price > 0)
    @Query("SELECT COUNT(d) FROM Document d WHERE d.price > 0 AND d.deletedAt IS NULL")
    long countPremiumDocuments();

    // Lấy danh sách tài liệu miễn phí
    @Query("SELECT d FROM Document d WHERE (d.price = 0 OR d.price IS NULL) AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    Page<Document> findFreeDocuments(Pageable pageable);

    // Lấy danh sách tài liệu premium
    @Query("SELECT d FROM Document d WHERE d.price > 0 AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    Page<Document> findPremiumDocuments(Pageable pageable);

    // Đếm tài liệu active (không bị xóa)
    @Query("SELECT COUNT(d) FROM Document d WHERE d.deletedAt IS NULL")
    long countActiveDocuments();

    // Tổng lượt tải
    @Query("SELECT COALESCE(SUM(d.downloadsCount), 0) FROM Document d WHERE d.deletedAt IS NULL")
    long getTotalDownloads();

    // Lấy tài liệu mới đăng tải cho dashboard
    @Query(value = """
            SELECT
                d.id,
                d.title,
                COALESCE(u.full_name, u.username) AS uploaderName,
                d.created_at AS uploadDate,
                d.status,
                d.downloads_count AS downloadsCount,
                CASE
                    WHEN d.price = 0 OR d.price IS NULL THEN 'Miễn phí'
                    ELSE 'Premium'
                END AS documentType
            FROM documents d
            LEFT JOIN document_owners do ON d.id = do.document_id AND do.ownership_type = 'owner'
            LEFT JOIN users u ON do.user_id = u.id
            WHERE d.deleted_at IS NULL
            ORDER BY d.created_at DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findRecentDocumentsForDashboard(Pageable pageable);

    // Lấy tất cả tài liệu (không bị xóa)
    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    Page<Document> findAllActiveDocuments(Pageable pageable);

    // Lấy tài liệu mới đăng tải (sắp xếp theo ngày tạo giảm dần)
    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Document> findRecentDocuments(Pageable pageable);

    // Lấy tài liệu được tạo trong khoảng thời gian
    @Query("SELECT d FROM Document d WHERE d.createdAt >= :fromDate AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Document> findDocumentsCreatedAfter(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

    // =======

    // Page<Document> findAll(Pageable pageable);

    // Lấy tài liệu mới nhất với thông tin chi tiết và danh mục thực tế
    @Query(value = """
            SELECT
                d.id,
                d.title,
                d.status,
                d.created_at AS uploadDate,
                COALESCE(u.full_name, u.username, 'Người dùng') AS uploaderName,
                COALESCE(c.name, 'Chưa phân loại') AS categoryName
            FROM documents d
            LEFT JOIN document_owners do ON d.id = do.document_id AND do.ownership_type = 'owner'
            LEFT JOIN users u ON do.user_id = u.id
            LEFT JOIN document_categories dc ON d.id = dc.document_id
            LEFT JOIN categories c ON dc.category_id = c.id
            WHERE d.deleted_at IS NULL
            ORDER BY d.created_at DESC
            LIMIT ?1
            """, nativeQuery = true)
    List<Map<String, Object>> findLatestDocumentsWithDetails(@Param("limit") int limit);

}
