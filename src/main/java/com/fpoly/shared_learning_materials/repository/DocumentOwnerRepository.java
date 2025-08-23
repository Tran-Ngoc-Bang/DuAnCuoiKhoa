package com.fpoly.shared_learning_materials.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.DocumentOwner;
import com.fpoly.shared_learning_materials.domain.DocumentOwnerId;

@Repository
public interface DocumentOwnerRepository extends JpaRepository<DocumentOwner, DocumentOwnerId> {
	List<DocumentOwner> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
     @Query("SELECT COUNT(DISTINCT do.user.id) FROM DocumentOwner do WHERE do.document.deletedAt IS NULL")
    long countDistinctContributors();
}