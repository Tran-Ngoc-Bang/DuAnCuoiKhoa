package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
	Optional<Document> findFirstByDeletedAtIsNull();
	List<Document> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();
	long countByCreatedAtBetweenAndDeletedAtIsNull(LocalDateTime start, LocalDateTime end);
}
