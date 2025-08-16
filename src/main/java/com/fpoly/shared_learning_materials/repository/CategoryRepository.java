package com.fpoly.shared_learning_materials.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Category;

@Repository

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    
    // Validation queries - kiểm tra tên trùng lặp (chỉ với danh mục chưa xóa)
    boolean existsByNameAndDeletedAtIsNull(String name);
    boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id);
    
    // Find by name queries
    Optional<Category> findByNameAndDeletedAtIsNull(String name);
    
    // Soft delete queries
    List<Category> findByDeletedAtIsNull(Sort sort);
    List<Category> findByDeletedAtIsNotNull(Sort sort);
    
    // Count queries
    long countByDeletedAtIsNull();
    long countByDeletedAtIsNotNull();
}

