package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
	
	boolean existsByContentAndDeletedAtIsNull(String content);
}
