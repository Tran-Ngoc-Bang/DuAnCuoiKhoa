package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.User;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAll(Pageable pageable);

    @Query("SELECT FUNCTION('MONTH', d.createdAt) as month, COUNT(d) as count " +
            "FROM Document d " +
            "WHERE FUNCTION('YEAR', d.createdAt) = :year " +
            "GROUP BY FUNCTION('MONTH', d.createdAt)")
    List<Object[]> countMonthlyUploads(@Param("year") int year);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.createdAt BETWEEN :start AND :end AND d.deletedAt IS NULL")
    long countNewDocumentsBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(d.downloadsCount) FROM Document d WHERE d.createdAt <= :end AND d.deletedAt IS NULL")
    Long sumDownloadsUntil(LocalDateTime end);

    long countByCreatedAtAfter(LocalDateTime fromDate);

    @Query("SELECT COALESCE(SUM(d.downloadsCount), 0) FROM Document d")
    long sumDownloadsCount();

    @Query("SELECT d FROM Document d ORDER BY d.downloadsCount DESC")
    List<Document> findTopByOrderByDownloadsCountDesc(org.springframework.data.domain.Pageable pageable);

    default List<Document> findTopByOrderByDownloadsCountDesc(int limit) {
        return findTopByOrderByDownloadsCountDesc(org.springframework.data.domain.PageRequest.of(0, limit));
    }

    @Query(value = """
            SELECT
                d.title AS document_title,
                u.full_name AS uploader_name,
                STRING_AGG(c.name, ', ') AS category_name,
                d.created_at AS upload_date,
                d.status AS document_status,
                d.id AS document_id
            FROM Documents d
            INNER JOIN Document_Owners do ON d.id = do.document_id AND do.ownership_type = 'owner'
            INNER JOIN Users u ON do.user_id = u.id
            LEFT JOIN Document_Categories dc ON d.id = dc.document_id
            LEFT JOIN Categories c ON dc.category_id = c.id
            WHERE d.deleted_at IS NULL
            GROUP BY d.id, d.title, u.full_name, d.created_at, d.status
            ORDER BY d.created_at DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findLatestDocuments();

    // Lấy tài liệu mới nhất với thông tin chi tiết (có limit)
    @Query(value = """
            SELECT TOP (?1)
                d.title AS document_title,
                COALESCE(u.full_name, u.username) AS uploader_name,
                COALESCE(STRING_AGG(c.name, ', '), 'Chưa phân loại') AS category_name,
                d.created_at AS upload_date,
                d.status AS document_status,
                d.id AS document_id
            FROM Documents d
            LEFT JOIN Document_Owners do ON d.id = do.document_id AND do.ownership_type = 'owner'
            LEFT JOIN Users u ON do.user_id = u.id
            LEFT JOIN Document_Categories dc ON d.id = dc.document_id
            LEFT JOIN Categories c ON dc.category_id = c.id
            WHERE d.deleted_at IS NULL
            GROUP BY d.id, d.title, u.full_name, u.username, d.created_at, d.status
            ORDER BY d.created_at DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findLatestDocumentsWithDetails(@Param("limit") int limit);

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

    // Lấy tất cả tài liệu (không bị xóa)
    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    Page<Document> findAllActiveDocuments(Pageable pageable);

    // Lấy tài liệu mới đăng tải (sắp xếp theo ngày tạo giảm dần)
    @Query("SELECT d FROM Document d WHERE d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Document> findRecentDocuments(Pageable pageable);

    // Lấy tài liệu được tạo trong khoảng thời gian
    @Query("SELECT d FROM Document d WHERE d.createdAt >= :fromDate AND d.deletedAt IS NULL ORDER BY d.createdAt DESC")
    List<Document> findDocumentsCreatedAfter(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

}
