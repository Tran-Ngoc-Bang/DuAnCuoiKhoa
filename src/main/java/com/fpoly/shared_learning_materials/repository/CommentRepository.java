package com.fpoly.shared_learning_materials.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fpoly.shared_learning_materials.domain.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByDocumentIdAndStatus(Long documentId, String status);
}
