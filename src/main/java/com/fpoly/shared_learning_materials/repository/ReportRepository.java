package com.fpoly.shared_learning_materials.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
	long countByStatus(String status);

	List<Report> findByStatus(String status);

	Optional<Report> findFirstByCommentIdAndStatusOrderByCreatedAtAsc(Long commentId, String status);

	Optional<Report> findFirstByCommentIdAndStatusOrderByCreatedAtDesc(Long commentId, String status);
}
