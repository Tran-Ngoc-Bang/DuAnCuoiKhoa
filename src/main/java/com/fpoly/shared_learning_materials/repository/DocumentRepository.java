package com.fpoly.shared_learning_materials.repository;

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
    Optional<Document> findBySlug(String slug);
    Page<Document> findAll(Pageable pageable);
    
    @Query("SELECT d FROM Document d WHERE d.slug = :slug")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Document> findBySlugWithLock(@Param("slug") String slug);
}