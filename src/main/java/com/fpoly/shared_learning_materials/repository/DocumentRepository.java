package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import com.fpoly.shared_learning_materials.domain.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    Optional<Document> findFirstByDeletedAtIsNull();

    List<Document> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

    long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);

    Page<Document> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Document> findByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);

    long countByStatus(String status);

    boolean existsBySlug(String slug);

    Optional<Document> findBySlug(String slug);

    Page<Document> findAll(Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.slug = :slug")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Document> findBySlugWithLock(@Param("slug") String slug);

    // Tìm documents đã xóa (soft delete)
    Page<Document> findByDeletedAtIsNotNull(Pageable pageable);

    // Tìm documents chưa xóa (active)
    Page<Document> findByDeletedAtIsNull(Pageable pageable);

    List<Document> findByDeletedAtIsNull();

    List<Document> findByDeletedAtIsNullOrderByCreatedAtDesc();

    List<Document> findByDeletedAtIsNotNull();

    List<Document> findByDeletedAtIsNotNullOrderByDeletedAtDesc();

    // Đếm documents chưa xóa
    long countByDeletedAtIsNull();

    // Đếm documents đã xóa
    long countByDeletedAtIsNotNull();

    // Tăng lượt xem
    @Query("UPDATE Document d SET d.viewsCount = COALESCE(d.viewsCount, 0) + 1 WHERE d.id = :documentId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int incrementViewCount(@Param("documentId") Long documentId);

    // Tăng lượt tải
    @Query("UPDATE Document d SET d.downloadsCount = COALESCE(d.downloadsCount, 0) + 1 WHERE d.id = :documentId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int incrementDownloadCount(@Param("documentId") Long documentId);

    @Query("SELECT d FROM Document d WHERE (:q IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Document> findByKeyword(@Param("q") String q, Pageable pageable);

    @Query("SELECT DISTINCT d FROM Document d " +
            "JOIN d.documentCategories dc " +
            "WHERE dc.category.id IN :categoryIds " +
            "AND d.id <> :documentId")
    List<Document> findRelatedDocuments(@Param("categoryIds") List<Long> categoryIds,
            @Param("documentId") Long documentId,
            Pageable pageable);

}
