package com.fpoly.shared_learning_materials.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.DocumentCategory;
import com.fpoly.shared_learning_materials.domain.DocumentCategoryId;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, DocumentCategoryId>, JpaSpecificationExecutor<DocumentCategory> {
	    @Query("SELECT dc.category.name, COUNT(dc.document.id) " +
		       "FROM DocumentCategory dc " +
		       "WHERE dc.document.deletedAt IS NULL " +
		       "GROUP BY dc.category.name " +
		       "ORDER BY COUNT(dc.document.id) DESC")
		List<Object[]> countDocumentsByCategory();

}
