package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Nếu bạn muốn đếm tất cả báo cáo
    @Query("SELECT COUNT(r) FROM Report r")
    long countAllReports();

    // Tìm các báo cáo theo trạng thái
    List<Report> findByStatus(String status);

    // Tìm báo cáo theo người báo cáo và trạng thái
    List<Report> findByReporter_IdAndStatus(Long reporterId, String status);

    // Tìm báo cáo theo ID của tài liệu
    List<Report> findByDocument_Id(Long documentId);

    // Tìm báo cáo theo trạng thái và loại báo cáo
    List<Report> findByStatusAndType(String status, String type);

    // Tìm báo cáo theo ID
    Optional<Report> findById(Long id);

    // ✅ Thêm dòng này để fix lỗi
    long countByStatus(String status);

    // Đếm số báo cáo có trạng thái "new" (mới)
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'new'")
    long countNewReports();

    // Đếm số báo cáo có trạng thái "pending" (đang xử lý)
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'pending'")
    long countProcessingReports();

    // Đếm số báo cáo có trạng thái "resolved"
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'resolved'")
    long countResolvedReports();

    // Đếm số báo cáo có trạng thái "rejected"
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'rejected'")
    long countRejectedReports();

    @Query(value = """
            SELECT
                r.id AS report_id,
                d.title AS document_title,
                u.full_name AS reporter_name,
                r.type AS violation_type,
                r.created_at AS report_date,
                r.status AS report_status
            FROM reports r
            LEFT JOIN documents d ON r.document_id = d.id
            LEFT JOIN users u ON r.reporter_id = u.id
            ORDER BY r.created_at DESC
            """, nativeQuery = true)
    List<Object[]> findAllReportsWithDetails();

    @Query(value = """
                        SELECT r.id, d.title AS documentTitle, u.full_name AS reporterName,
                            r.type AS violationType, r.created_at AS reportDate,
                            r.status
                        FROM Reports r
                        JOIN Documents d ON r.document_id = d.id
                        JOIN Users u ON r.reporter_id = u.id
                        WHERE (:keyword IS NULL OR d.title LIKE CONCAT('%', :keyword, '%') OR u.full_name LIKE CONCAT('%', :keyword, '%'))
                        AND (:violationType IS NULL OR r.type = :violationType)
            AND (:status IS NULL OR r.status = :status)
                        """, nativeQuery = true)
    List<Map<String, Object>> searchReportsWithFilters(
            @Param("keyword") String keyword,
            @Param("violationType") String violationType,
            @Param("status") String status);

    @Query("""
                SELECT r FROM Report r
                LEFT JOIN FETCH r.reporter
                LEFT JOIN FETCH r.document
                WHERE r.id = :id
            """)
    Optional<Report> findReportWithDetailsById(@Param("id") Long id);

    @Query("""
                SELECT r.id AS reportId,
                       r.createdAt AS reportDate,
                       r.type AS violationType,
                       r.reason AS reason,
                       r.status AS status,
                       d.title AS documentTitle,
                       u.fullName AS documentUploader,
                       d.createdAt AS documentUploadDate,
                       d.downloadsCount AS documentDownloads,
                       reporter.fullName AS reporterName,
                       reporter.email AS reporterEmail,
                       r.reviewedAt AS reviewedAt,
                       reviewer.fullName AS reviewerName
                FROM Report r
                JOIN r.document d
                JOIN r.reporter reporter
                LEFT JOIN r.reviewer reviewer
                JOIN DocumentOwner do ON do.document = d AND do.ownershipType = 'owner'
                JOIN do.user u
                WHERE r.id = :id
            """)
    Optional<Map<String, Object>> findReportDetailsById(@Param("id") Long id);

    Optional<Report> findFirstByCommentIdAndStatusOrderByCreatedAtAsc(Long commentId, String status);

    Optional<Report> findFirstByCommentIdAndStatusOrderByCreatedAtDesc(Long commentId, String status);
}
