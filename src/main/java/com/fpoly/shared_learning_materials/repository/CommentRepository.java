package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
	long countByDeletedAtIsNull();

	long countByDeletedAtIsNullAndCreatedAtAfter(LocalDateTime dateTime);

	long countByStatusNotAndDeletedAtIsNull(String status);

	Page<Comment> findByDeletedAtIsNull(Pageable pageable);

	Page<Comment> findByDeletedAtIsNullAndCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);

	Page<Comment> findByDeletedAtIsNullAndStatusNot(String status, Pageable pageable);

	Page<Comment> findByDeletedAtIsNullAndIdIn(List<Long> ids, Pageable pageable);

	List<Comment> findByDocumentIdAndStatus(Long documentId, String status);

	Page<Comment> findByDocumentIdAndStatusOrderByCreatedAtDesc(Long documentId, String status, Pageable pageable);

	Page<Comment> findByDocumentIdOrderByCreatedAtDesc(Long documentId, Pageable pageable);

	@Query("SELECT COUNT(c) FROM Comment c WHERE c.document.id = :documentId AND c.status = :status")
	long countByDocumentIdAndStatus(@Param("documentId") Long documentId, @Param("status") String status);

	@Query("SELECT c FROM Comment c WHERE c.document.id = :documentId AND c.status IN :statuses ORDER BY c.createdAt DESC")
	Page<Comment> findByDocumentIdAndStatusIn(@Param("documentId") Long documentId,
			@Param("statuses") List<String> statuses, Pageable pageable);

	@Query("SELECT AVG(c.rating) FROM Comment c WHERE c.document.id = :documentId AND c.status = 'active' AND c.rating IS NOT NULL")
	Double getAverageRatingByDocumentId(@Param("documentId") Long documentId);

	List<Comment> findByDocumentIdInAndStatus(List<Long> documentIds, String status);

	@Query("SELECT COALESCE(SUM(c.rating), 0) " +
			"FROM Comment c " +
			"WHERE c.document.file.uploadedBy.id = :userId AND c.deletedAt IS NULL")
	Integer sumRatingsByUserDocuments(@Param("userId") Long userId);
}
