package com.fpoly.shared_learning_materials.repository;

import java.util.List;
// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
	List<Reply> findByCommentIdAndDeletedAtIsNullOrderByCreatedAtAsc(Long commentId);
}
