package com.fpoly.shared_learning_materials.repository;

import java.util.List;

// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.DocumentCategory;
import com.fpoly.shared_learning_materials.domain.DocumentCategoryId;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, DocumentCategoryId> {

	 @Query("SELECT dc.category.name, COUNT(dc.document.id) " +
		       "FROM DocumentCategory dc " +
		       "WHERE dc.document.deletedAt IS NULL " +
		       "GROUP BY dc.category.name " +
		       "ORDER BY COUNT(dc.document.id) DESC")
		List<Object[]> countDocumentsByCategory();
//	 @Query("SELECT dc FROM DocumentCategory dc WHERE dc.id.documentId = :documentId")
//	    DocumentCategory findByDocumentId(Long documentId);

	@Query("SELECT COUNT(dc) FROM DocumentCategory dc WHERE dc.category.id = :categoryId")
    int countByCategoryId(@Param("categoryId") Long categoryId);
	
	    @Modifying
	    @Transactional
	    @Query("DELETE FROM DocumentCategory dc WHERE dc.id.documentId = :documentId")
	    void deleteByDocumentId(@Param("documentId") Long documentId);
	    
	    List<DocumentCategory> findByDocumentId(Long documentId);
	    
	    @Modifying
	    @Query("DELETE FROM DocumentCategory dc WHERE dc.category.id = :categoryId")
	    void deleteByCategoryId(@Param("categoryId") Long categoryId);

	    @Modifying
	    @Query("DELETE FROM DocumentCategory dc WHERE dc.category.id IN :categoryIds")
	    void deleteByCategoryIdIn(@Param("categoryIds") List<Long> categoryIds);
}

