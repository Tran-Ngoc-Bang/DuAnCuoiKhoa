package com.fpoly.shared_learning_materials.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fpoly.shared_learning_materials.domain.Category;
import com.fpoly.shared_learning_materials.domain.CategoryHierarchy;
import com.fpoly.shared_learning_materials.domain.CategoryHierarchyId;
import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.SubcategoryDTO;
import com.fpoly.shared_learning_materials.repository.CategoryHierarchyRepository;
import com.fpoly.shared_learning_materials.repository.CategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentCategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

@Service
public class CategoryService {

	@Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private CategoryHierarchyRepository categoryHierarchyRepository;
    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;
    @Autowired
    private UserRepository userRepository;

    public List<CategoryDTO> getAllCategories() {
        // Sắp xếp theo thời gian tạo mới nhất trước
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<CategoryHierarchy> hierarchies = categoryHierarchyRepository.findAll();
        System.out.println("Hierarchies loaded: " + hierarchies.size());
        hierarchies.forEach(h -> System.out.println("Hierarchy: parentId=" + h.getId().getParentId() + ", childId=" + h.getId().getChildId() + ", level=" + h.getLevel()));
        
        // Tính số danh mục con
        Map<Long, Long> subcategoryCount = hierarchies.stream()
            .collect(Collectors.groupingBy(h -> h.getId().getParentId(), Collectors.counting()));

        // Lấy ngày tạo từ CategoryHierarchy
        Map<Long, LocalDateTime> hierarchyCreatedAtMap = hierarchies.stream()
            .collect(Collectors.toMap(h -> h.getId().getChildId(), CategoryHierarchy::getCreatedAt, (a, b) -> a));
        
        Map<Long, Long> parentIdMap = hierarchies.stream()
                .collect(Collectors.toMap(h -> h.getId().getChildId(), h -> h.getId().getParentId(), (a, b) -> {
                
                    return a;
                }));

        return categories.stream().map(cat -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setSlug(cat.getSlug());
            dto.setDescription(cat.getDescription());
            dto.setStatus(cat.getStatus());
            dto.setSortOrder(cat.getSortOrder());
            dto.setCreatedById(cat.getCreatedBy() != null ? cat.getCreatedBy().getId() : null);
            dto.setCreatedByName(cat.getCreatedBy() != null ? 
                    userRepository.findById(cat.getCreatedBy().getId()).map(User::getFullName).orElse("Không có") : "Không có");
            dto.setCreatedAt(cat.getCreatedAt());
            dto.setUpdatedAt(cat.getUpdatedAt());
            dto.setDeletedAt(cat.getDeletedAt());
            dto.setDocuments(documentCategoryRepository.countByCategoryId(cat.getId()));
            dto.setSubcategories(subcategoryCount.getOrDefault(cat.getId(), 0L).intValue());
            dto.setHierarchyCreatedAt(hierarchyCreatedAtMap.get(cat.getId()));
            dto.setParentId(parentIdMap.get(cat.getId()));
            return dto;
        }).collect(Collectors.toList());
    }
    
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
   
        if (categoryDTO.getName() == null || categoryDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

  
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setSlug(generateSlug(categoryDTO.getName())); // Implement slug generation
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : "active");
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        if (username != null && !username.equals("anonymousUser")) {
//            User createdBy = new User(); // Giả định có UserService để lấy User
//            createdBy.setId(categoryDTO.getCreatedById()); // Cần inject UserService để lấy User thực tế
//            category.setCreatedBy(createdBy);
//        }
        if (categoryDTO.getCreatedById() != null) {
            User createdBy = userRepository.findById(categoryDTO.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + categoryDTO.getCreatedById()));
            category.setCreatedBy(createdBy);
        }
      
        category = categoryRepository.save(category);

   
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0) {
            CategoryHierarchy hierarchy = new CategoryHierarchy();
            hierarchy.setId(new CategoryHierarchyId(categoryDTO.getParentId(), category.getId()));
            hierarchy.setParent(categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found")));
            hierarchy.setChild(category);
            int parentLevel = categoryHierarchyRepository.findByIdChildId(categoryDTO.getParentId())
                    .map(CategoryHierarchy::getLevel)
                    .orElse(0);
                hierarchy.setLevel(parentLevel + 1);
//            hierarchy.setLevel(1); 
            hierarchy.setCreatedAt(LocalDateTime.now());
            categoryHierarchyRepository.save(hierarchy);
        }

        // Convert back to DTO
        CategoryDTO resultDTO = new CategoryDTO();
        resultDTO.setId(category.getId());
        resultDTO.setName(category.getName());
        resultDTO.setSlug(category.getSlug());
        resultDTO.setDescription(category.getDescription());
        resultDTO.setParentId(categoryDTO.getParentId());
        resultDTO.setStatus(category.getStatus());
        resultDTO.setSortOrder(category.getSortOrder());
        resultDTO.setCreatedById(category.getCreatedBy() != null ? category.getCreatedBy().getId() : null);
        resultDTO.setCreatedAt(category.getCreatedAt());
        resultDTO.setUpdatedAt(category.getUpdatedAt());
        resultDTO.setDeletedAt(category.getDeletedAt());
        resultDTO.setParentId(categoryDTO.getParentId());
        return resultDTO;
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        if (categoryDTO.getName() == null || categoryDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        category.setName(categoryDTO.getName());
        category.setSlug(generateSlug(categoryDTO.getName()));
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : "active");
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : category.getSortOrder());
        category.setUpdatedAt(LocalDateTime.now());

        category = categoryRepository.save(category);

        // Update hierarchy
        categoryHierarchyRepository.deleteByIdChildId(id); // Remove old hierarchy
        
        Long parentId = categoryDTO.getParentId();
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0 && !categoryDTO.getParentId().equals(id)) {
            CategoryHierarchy hierarchy = new CategoryHierarchy();
            hierarchy.setId(new CategoryHierarchyId(categoryDTO.getParentId(), category.getId()));
            hierarchy.setParent(categoryRepository.findById(categoryDTO.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found")));
            hierarchy.setChild(category);
            int parentLevel = categoryHierarchyRepository.findByIdChildId(categoryDTO.getParentId())
                    .map(CategoryHierarchy::getLevel)
                    .orElse(0);
                hierarchy.setLevel(parentLevel + 1);
//            hierarchy.setLevel(1);
            hierarchy.setCreatedAt(LocalDateTime.now());
            categoryHierarchyRepository.save(hierarchy);
        }

        CategoryDTO resultDTO = new CategoryDTO();
        resultDTO.setId(category.getId());
        resultDTO.setName(category.getName());
        resultDTO.setSlug(category.getSlug());
        resultDTO.setDescription(category.getDescription());
        resultDTO.setStatus(category.getStatus());
        resultDTO.setSortOrder(category.getSortOrder());
        resultDTO.setCreatedById(category.getCreatedBy() != null ? category.getCreatedBy().getId() : null);
        resultDTO.setCreatedAt(category.getCreatedAt());
        resultDTO.setUpdatedAt(category.getUpdatedAt());
        resultDTO.setDeletedAt(category.getDeletedAt());
        resultDTO.setParentId(categoryDTO.getParentId());
        return resultDTO;
    }
    
 // Xóa một danh mục
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found with id: " + categoryId);
        }

        // Xóa các quan hệ trong CategoryHierarchy (cha hoặc con)
        categoryHierarchyRepository.deleteByParentIdOrChildId(categoryId, categoryId);

        // Xóa các quan hệ trong DocumentCategory
        documentCategoryRepository.deleteByCategoryId(categoryId);

        // Xóa danh mục
        categoryRepository.deleteById(categoryId);
    }

    // Xóa hàng loạt danh mục
    @Transactional
    public void deleteCategories(List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            if (!categoryRepository.existsById(categoryId)) {
                throw new IllegalArgumentException("Category not found with id: " + categoryId);
            }
        }

        // Xóa các quan hệ trong CategoryHierarchy
        categoryHierarchyRepository.deleteByParentIdInOrChildIdIn(categoryIds, categoryIds);

        // Xóa các quan hệ trong DocumentCategory
        documentCategoryRepository.deleteByCategoryIdIn(categoryIds);

        // Xóa các danh mục
        categoryRepository.deleteAllById(categoryIds);
    }
    
    // Helper method to generate slug
    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    }
}