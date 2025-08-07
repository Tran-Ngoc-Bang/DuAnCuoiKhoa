package com.fpoly.shared_learning_materials.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.CategoryHierarchy;
import com.fpoly.shared_learning_materials.domain.CategoryHierarchyId;

@Repository
public interface CategoryHierarchyRepository extends JpaRepository<CategoryHierarchy, CategoryHierarchyId> {
	 List<CategoryHierarchy> findByParentId(Long parentId);
	    Optional<CategoryHierarchy> findByChildId(Long childId);
	    void deleteByChildId(Long childId);
	    void deleteByParentId(Long parentId);
	    void deleteByIdChildId(Long childId);
	    
	    @Modifying
	    @Query("DELETE FROM CategoryHierarchy ch WHERE ch.id.parentId = :parentId OR ch.id.childId = :childId")
	    void deleteByParentIdOrChildId(@Param("parentId") Long parentId, @Param("childId") Long childId);

	    @Modifying
	    @Query("DELETE FROM CategoryHierarchy ch WHERE ch.id.parentId IN :parentIds OR ch.id.childId IN :childIds")
	    void deleteByParentIdInOrChildIdIn(@Param("parentIds") List<Long> parentIds, @Param("childIds") List<Long> childIds);
	    Optional<CategoryHierarchy> findByIdChildId(Long childId);
	    
	    List<CategoryHierarchy> findByIdParentId(Long parentId);
	    
}
