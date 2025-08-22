package com.fpoly.shared_learning_materials.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.DocumentOwner;
import com.fpoly.shared_learning_materials.domain.DocumentOwnerId;
import com.fpoly.shared_learning_materials.domain.User;

@Repository
public interface DocumentOwnerRepository extends JpaRepository<DocumentOwner, DocumentOwnerId> {
	List<DocumentOwner> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
    public boolean existsByUserAndDocument(User user, Document document);
}