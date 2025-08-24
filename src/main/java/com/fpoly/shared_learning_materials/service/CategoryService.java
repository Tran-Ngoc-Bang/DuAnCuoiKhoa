package com.fpoly.shared_learning_materials.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fpoly.shared_learning_materials.domain.Category;
import com.fpoly.shared_learning_materials.domain.CategoryHierarchy;
import com.fpoly.shared_learning_materials.domain.CategoryHierarchyId;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.CategoryTreeDTO;
import com.fpoly.shared_learning_materials.repository.CategoryHierarchyRepository;
import com.fpoly.shared_learning_materials.repository.CategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentCategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentTagRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.domain.DocumentTag;
import com.fpoly.shared_learning_materials.domain.Tag;
import com.fpoly.shared_learning_materials.domain.DocumentCategory;
import com.fpoly.shared_learning_materials.util.CategoryIconMapper;

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
    @Autowired
    private DocumentTagRepository documentTagRepository;

    public List<CategoryDTO> getAllCategories() {
        return getCategoriesByDeletedStatus(null); // Lấy tất cả
    }

    public List<CategoryDTO> getActiveCategories() {
        return getCategoriesByDeletedStatus(false); // Chỉ lấy chưa xóa
    }

    public List<CategoryDTO> getDeletedCategories() {
        return getCategoriesByDeletedStatus(true); // Chỉ lấy đã xóa
    }

    // New methods to get only root categories (for grid view)
    public List<CategoryDTO> getRootCategories() {
        return getRootCategoriesByDeletedStatus(null);
    }

    public List<CategoryDTO> getActiveRootCategories() {
        return getRootCategoriesByDeletedStatus(false);
    }

    public List<CategoryDTO> getDeletedRootCategories() {
        return getRootCategoriesByDeletedStatus(true);
    }

    private List<CategoryDTO> getCategoriesByDeletedStatus(Boolean isDeleted) {
        List<Category> categories;

        if (isDeleted == null) {
            // Lấy tất cả
            categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (isDeleted) {
            // Chỉ lấy đã xóa (deletedAt != null)
            System.out.println("=== GETTING DELETED CATEGORIES ===");
            long totalDeletedCount = categoryRepository.countByDeletedAtIsNotNull();
            System.out.println("Total deleted categories in DB: " + totalDeletedCount);

            categories = categoryRepository.findByDeletedAtIsNotNull(Sort.by(Sort.Direction.DESC, "deletedAt"));
            System.out.println("Retrieved deleted categories from query: " + categories.size());
        } else {
            // Chỉ lấy chưa xóa (deletedAt == null)
            categories = categoryRepository.findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<CategoryHierarchy> hierarchies = categoryHierarchyRepository.findAll();
        System.out.println("Hierarchies loaded: " + hierarchies.size());
        hierarchies.forEach(h -> System.out.println("Hierarchy: parentId=" + h.getId().getParentId() + ", childId="
                + h.getId().getChildId() + ", level=" + h.getLevel()));

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

        // Tạo map để lấy tên danh mục cha - cần lấy TẤT CẢ categories để có thể lấy tên
        // parent
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        System.out.println("Converting " + categories.size() + " categories to DTOs");

        // OPTIMIZATION: Batch load document counts and popular tags
        List<Long> categoryIds = categories.stream().map(Category::getId).collect(Collectors.toList());

        // Batch load document counts
        Map<Long, Long> documentCountMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Object[]> docCountResults = documentCategoryRepository.countDocumentsByCategoryIds(categoryIds);
            for (Object[] result : docCountResults) {
                Long catId = (Long) result[0];
                Long count = (Long) result[1];
                documentCountMap.put(catId, count);
            }
        }

        // Batch load popular tags
        Map<Long, List<String>> popularTagsMap = getPopularTagsForCategories(categoryIds, 5);

        List<CategoryDTO> result = categories.stream().map(cat -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setSlug(cat.getSlug());
            dto.setDescription(cat.getDescription());
            dto.setStatus(cat.getStatus());
            dto.setSortOrder(cat.getSortOrder());
            dto.setCreatedById(cat.getCreatedBy() != null ? cat.getCreatedBy().getId() : null);
            dto.setCreatedByName(cat.getCreatedBy() != null
                    ? userRepository.findById(cat.getCreatedBy().getId()).map(User::getFullName).orElse("Không có")
                    : "Không có");
            dto.setCreatedAt(cat.getCreatedAt());
            dto.setUpdatedAt(cat.getUpdatedAt());
            dto.setDeletedAt(cat.getDeletedAt());
            dto.setDocuments(documentCountMap.getOrDefault(cat.getId(), 0L).intValue());
            dto.setSubcategories(subcategoryCount.getOrDefault(cat.getId(), 0L).intValue());
            dto.setHierarchyCreatedAt(hierarchyCreatedAtMap.get(cat.getId()));
            dto.setParentId(parentIdMap.get(cat.getId()));

            // Set parent name nếu có parent
            Long parentId = parentIdMap.get(cat.getId());
            if (parentId != null) {
                dto.setParentName(categoryNameMap.get(parentId));
            }

            // Set popular tags from batch loaded data
            dto.setPopularTags(popularTagsMap.getOrDefault(cat.getId(), new ArrayList<>()));

            return dto;
        }).collect(Collectors.toList());

        System.out.println("Converted to " + result.size() + " DTOs");
        return result;
    }

    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        if (categoryDTO.getName() == null || categoryDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        // Kiểm tra tên danh mục đã tồn tại chưa
        if (categoryRepository.existsByNameAndDeletedAtIsNull(categoryDTO.getName().trim())) {
            throw new IllegalArgumentException("Tên danh mục '" + categoryDTO.getName() + "' đã tồn tại");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setSlug(generateSlug(categoryDTO.getName())); // Implement slug generation
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : "active");
        category.setSortOrder(categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : 0);

        if (categoryDTO.getCreatedById() != null) {
            User createdBy = userRepository.findById(categoryDTO.getCreatedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "User not found with id: " + categoryDTO.getCreatedById()));
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
            // hierarchy.setLevel(1);
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
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));

        // Kiểm tra tên danh mục đã tồn tại chưa (trừ chính nó)
        if (categoryRepository.existsByNameAndDeletedAtIsNullAndIdNot(categoryDTO.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên danh mục '" + categoryDTO.getName() + "' đã tồn tại");
        }

        category.setName(categoryDTO.getName());
        category.setSlug(generateSlug(categoryDTO.getName()));
        category.setDescription(categoryDTO.getDescription());
        category.setStatus(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : "active");
        category.setSortOrder(
                categoryDTO.getSortOrder() != null ? categoryDTO.getSortOrder() : category.getSortOrder());
        category.setUpdatedAt(LocalDateTime.now());

        category = categoryRepository.save(category);

        // Update hierarchy
        categoryHierarchyRepository.deleteByIdChildId(id); // Remove old hierarchy

        Long parentId = categoryDTO.getParentId();
        if (categoryDTO.getParentId() != null && categoryDTO.getParentId() > 0
                && !categoryDTO.getParentId().equals(id)) {
            CategoryHierarchy hierarchy = new CategoryHierarchy();
            hierarchy.setId(new CategoryHierarchyId(categoryDTO.getParentId(), category.getId()));
            hierarchy.setParent(categoryRepository.findById(categoryDTO.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found")));
            hierarchy.setChild(category);
            int parentLevel = categoryHierarchyRepository.findByIdChildId(categoryDTO.getParentId())
                    .map(CategoryHierarchy::getLevel)
                    .orElse(0);
            hierarchy.setLevel(parentLevel + 1);
            // hierarchy.setLevel(1);
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

    // Soft delete một danh mục
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        if (category.getDeletedAt() != null) {
            throw new IllegalArgumentException("Category is already deleted");
        }

        // Soft delete: chỉ set deletedAt
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        // Soft delete các danh mục con
        softDeleteChildCategories(categoryId);
    }

    // Soft delete các danh mục con
    private void softDeleteChildCategories(Long parentId) {
        List<CategoryHierarchy> children = categoryHierarchyRepository.findByIdParentId(parentId);
        for (CategoryHierarchy child : children) {
            Long childId = child.getId().getChildId();
            Category childCategory = categoryRepository.findById(childId).orElse(null);
            if (childCategory != null && childCategory.getDeletedAt() == null) {
                childCategory.setDeletedAt(LocalDateTime.now());
                categoryRepository.save(childCategory);
                // Đệ quy xóa các danh mục con của danh mục con này
                softDeleteChildCategories(childId);
            }
        }
    }

    // Soft delete hàng loạt danh mục
    @Transactional
    public void deleteCategories(List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            deleteCategory(categoryId); // Sử dụng soft delete cho từng danh mục
        }
    }

    // Xóa vĩnh viễn danh mục (hard delete)
    @Transactional
    public void permanentDeleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));

        if (category.getDeletedAt() == null) {
            throw new IllegalArgumentException("Category must be soft deleted first");
        }

        // Xóa các quan hệ trong CategoryHierarchy (cha hoặc con)
        categoryHierarchyRepository.deleteByParentIdOrChildId(categoryId, categoryId);

        // Xóa các quan hệ trong DocumentCategory
        documentCategoryRepository.deleteByCategoryId(categoryId);

        // Xóa danh mục vĩnh viễn
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    public void bulkPermanentDelete(List<Long> categoryIds) {
        for (Long categoryId : categoryIds) {
            permanentDeleteCategory(categoryId); // Gọi phương thức xóa vĩnh viễn cho từng danh mục
        }
    }

    @Transactional
    public void restoreCategory(Long categoryId) {
        System.out.println("=== RESTORE CATEGORY DEBUG ===");
        System.out.println("Attempting to restore category ID: " + categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + categoryId));

        System.out.println("Found category: " + category.getName() + ", deletedAt: " + category.getDeletedAt());

        if (category.getDeletedAt() == null) {
            throw new IllegalArgumentException("Danh mục này chưa bị xóa");
        }

        // Kiểm tra xem danh mục này có phải là danh mục con không
        CategoryHierarchy hierarchy = categoryHierarchyRepository.findByIdChildId(categoryId).orElse(null);
        System.out.println("Found hierarchy for child ID " + categoryId + ": " + (hierarchy != null ? "YES" : "NO"));

        if (hierarchy != null) {
            // Đây là danh mục con, kiểm tra xem danh mục cha đã được khôi phục chưa
            Long parentId = hierarchy.getId().getParentId();
            System.out.println("Parent ID: " + parentId);

            Category parentCategory = categoryRepository.findById(parentId).orElse(null);

            if (parentCategory != null) {
                System.out.println("Parent category: " + parentCategory.getName() + ", deletedAt: "
                        + parentCategory.getDeletedAt());

                if (parentCategory.getDeletedAt() != null) {
                    // Danh mục cha vẫn bị xóa
                    System.out.println("VALIDATION FAILED: Parent category is still deleted");
                    throw new IllegalArgumentException(
                            "Không thể khôi phục danh mục con. Bạn cần khôi phục danh mục cha '"
                                    + parentCategory.getName() + "' trước.");
                } else {
                    System.out.println("Parent category is active, can restore child");
                }
            } else {
                System.out.println("Parent category not found!");
            }
        } else {
            System.out.println("This is a root category (no parent)");
        }

        // Khôi phục danh mục
        category.setDeletedAt(null);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);

        System.out.println("Successfully restored category: " + category.getName() + " (ID: " + categoryId + ")");
    }

    // Method to get only root categories (no parent)
    private List<CategoryDTO> getRootCategoriesByDeletedStatus(Boolean isDeleted) {
        List<Category> categories;

        if (isDeleted == null) {
            // Lấy tất cả
            categories = categoryRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else if (isDeleted) {
            // Chỉ lấy đã xóa (deletedAt != null)
            categories = categoryRepository.findByDeletedAtIsNotNull(Sort.by(Sort.Direction.DESC, "deletedAt"));
        } else {
            // Chỉ lấy chưa xóa (deletedAt == null)
            categories = categoryRepository.findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<CategoryHierarchy> hierarchies = categoryHierarchyRepository.findAll();

        // Get all child IDs to filter out child categories
        Set<Long> childIds = hierarchies.stream()
                .map(h -> h.getId().getChildId())
                .collect(Collectors.toSet());

        // Filter to get only root categories (categories that are not children)
        List<Category> rootCategories = categories.stream()
                .filter(cat -> !childIds.contains(cat.getId()))
                .collect(Collectors.toList());

        // Tính số danh mục con cho mỗi root category
        Map<Long, Long> subcategoryCount = hierarchies.stream()
                .collect(Collectors.groupingBy(h -> h.getId().getParentId(), Collectors.counting()));

        // Tạo map để lấy tên danh mục cha - cần lấy TẤT CẢ categories để có thể lấy tên
        // parent
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, String> categoryNameMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        List<CategoryDTO> result = rootCategories.stream().map(cat -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setId(cat.getId());
            dto.setName(cat.getName());
            dto.setSlug(cat.getSlug());
            dto.setDescription(cat.getDescription());
            dto.setStatus(cat.getStatus());
            dto.setSortOrder(cat.getSortOrder());
            dto.setCreatedById(cat.getCreatedBy() != null ? cat.getCreatedBy().getId() : null);
            dto.setCreatedByName(cat.getCreatedBy() != null
                    ? userRepository.findById(cat.getCreatedBy().getId()).map(User::getFullName).orElse("Không có")
                    : "Không có");
            dto.setCreatedAt(cat.getCreatedAt());
            dto.setUpdatedAt(cat.getUpdatedAt());
            dto.setDeletedAt(cat.getDeletedAt());

            // Đếm documents trong category và subcategories
            long totalDocuments = countDocumentsInCategoryAndSubcategories(cat.getId(), hierarchies);
            dto.setDocuments((int) totalDocuments);

            dto.setSubcategories(subcategoryCount.getOrDefault(cat.getId(), 0L).intValue());
            dto.setParentId(null); // Root categories have no parent
            dto.setParentName(null);

            // Set icon information
            dto.setIconClass(CategoryIconMapper.getIconClassForCategory(cat.getName()));
            dto.setIconName(CategoryIconMapper.getIconForCategory(cat.getName()));
            if ("dynamic-icon".equals(dto.getIconClass())) {
                dto.setIconStyle(CategoryIconMapper.getDynamicIconStyle(cat.getName()));
            }

            // Get popular tags for this category
            List<String> popularTags = getPopularTagsForCategory(cat.getId(), 4);
            dto.setPopularTags(popularTags);

            return dto;
        }).collect(Collectors.toList());

        return result;
    }

    // Method to count documents in a category and all its subcategories - OPTIMIZED
    // VERSION
    private long countDocumentsInCategoryAndSubcategories(Long categoryId, List<CategoryHierarchy> hierarchies) {
        // Tìm tất cả subcategories (recursive)
        Set<Long> allSubcategoryIds = getAllSubcategoryIds(categoryId, hierarchies);

        // Thêm category hiện tại vào danh sách để đếm
        allSubcategoryIds.add(categoryId);

        // Batch count documents cho tất cả categories trong một lần query
        if (allSubcategoryIds.isEmpty()) {
            return 0;
        }

        List<Object[]> countResults = documentCategoryRepository.countDocumentsByCategoryIds(
                new ArrayList<>(allSubcategoryIds));

        // Sum up all counts
        return countResults.stream()
                .mapToLong(result -> (Long) result[1])
                .sum();
    }

    // Helper method to get all subcategory IDs recursively
    private Set<Long> getAllSubcategoryIds(Long parentId, List<CategoryHierarchy> hierarchies) {
        Set<Long> subcategoryIds = new HashSet<>();

        // Tìm direct children
        List<Long> directChildren = hierarchies.stream()
                .filter(h -> h.getId().getParentId().equals(parentId))
                .map(h -> h.getId().getChildId())
                .collect(Collectors.toList());

        // Thêm direct children
        subcategoryIds.addAll(directChildren);

        // Recursively get children of children
        for (Long childId : directChildren) {
            subcategoryIds.addAll(getAllSubcategoryIds(childId, hierarchies));
        }

        return subcategoryIds;
    }

    // Get subcategories tree for a parent category
    public List<CategoryTreeDTO> getSubcategoriesTree(Long parentId) {
        List<CategoryHierarchy> hierarchies = categoryHierarchyRepository.findByIdParentId(parentId);
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

        return hierarchies.stream()
                .map(h -> buildCategoryTree(h.getId().getChildId(), categoryMap))
                .filter(tree -> tree != null)
                .collect(Collectors.toList());
    }

    private CategoryTreeDTO buildCategoryTree(Long categoryId, Map<Long, Category> categoryMap) {
        Category category = categoryMap.get(categoryId);
        if (category == null)
            return null;

        CategoryTreeDTO tree = new CategoryTreeDTO();
        tree.setId(category.getId());
        tree.setName(category.getName());
        tree.setSlug(category.getSlug());
        tree.setDescription(category.getDescription());
        tree.setStatus(category.getStatus());
        tree.setDocuments(documentCategoryRepository.countByCategoryId(category.getId()));
        tree.setCreatedAt(category.getCreatedAt());
        tree.setUpdatedAt(category.getUpdatedAt());
        tree.setDeletedAt(category.getDeletedAt());

        // Get children
        List<CategoryHierarchy> childHierarchies = categoryHierarchyRepository.findByIdParentId(categoryId);
        List<CategoryTreeDTO> children = childHierarchies.stream()
                .map(h -> buildCategoryTree(h.getId().getChildId(), categoryMap))
                .filter(child -> child != null)
                .collect(Collectors.toList());

        tree.setChildren(children);
        tree.setSubcategories(children.size());

        return tree;
    }

    // Get subcategories tree for a parent category - OPTIMIZED VERSION
    public List<CategoryTreeDTO> getSubcategoriesTreeOptimized(Long parentId) {
        List<CategoryHierarchy> hierarchies = categoryHierarchyRepository.findByIdParentId(parentId);
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, Category> categoryMap = allCategories.stream()
                .collect(Collectors.toMap(Category::getId, cat -> cat));

        // Batch load document counts for all categories
        List<Long> categoryIds = allCategories.stream().map(Category::getId).collect(Collectors.toList());
        Map<Long, Long> documentCountMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Object[]> docCountResults = documentCategoryRepository.countDocumentsByCategoryIds(categoryIds);
            for (Object[] result : docCountResults) {
                Long catId = (Long) result[0];
                Long count = (Long) result[1];
                documentCountMap.put(catId, count);
            }
        }

        return hierarchies.stream()
                .map(h -> buildCategoryTreeOptimized(h.getId().getChildId(), categoryMap, documentCountMap))
                .filter(tree -> tree != null)
                .collect(Collectors.toList());
    }

    private CategoryTreeDTO buildCategoryTreeOptimized(Long categoryId, Map<Long, Category> categoryMap,
            Map<Long, Long> documentCountMap) {
        Category category = categoryMap.get(categoryId);
        if (category == null)
            return null;

        CategoryTreeDTO tree = new CategoryTreeDTO();
        tree.setId(category.getId());
        tree.setName(category.getName());
        tree.setSlug(category.getSlug());
        tree.setDescription(category.getDescription());
        tree.setStatus(category.getStatus());
        tree.setDocuments(documentCountMap.getOrDefault(category.getId(), 0L).intValue());
        tree.setCreatedAt(category.getCreatedAt());
        tree.setUpdatedAt(category.getUpdatedAt());
        tree.setDeletedAt(category.getDeletedAt());

        // Get children
        List<CategoryHierarchy> childHierarchies = categoryHierarchyRepository.findByIdParentId(categoryId);
        List<CategoryTreeDTO> children = childHierarchies.stream()
                .map(h -> buildCategoryTreeOptimized(h.getId().getChildId(), categoryMap, documentCountMap))
                .filter(child -> child != null)
                .collect(Collectors.toList());

        tree.setChildren(children);
        tree.setSubcategories(children.size());

        return tree;
    }

    // Helper method to generate slug
    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
    }

    /**
     * Get popular tags for a category - OPTIMIZED VERSION
     */
    public List<String> getPopularTagsForCategory(Long categoryId, int limit) {
        // Sử dụng JOIN query để lấy tất cả tags trong một lần query
        List<Object[]> tagResults = documentCategoryRepository.findPopularTagsByCategoryId(categoryId);

        return tagResults.stream()
                .map(result -> (String) result[0]) // Tag name
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get popular tags for multiple categories - BATCH OPTIMIZED VERSION
     */
    public Map<Long, List<String>> getPopularTagsForCategories(List<Long> categoryIds, int limit) {
        if (categoryIds.isEmpty()) {
            return new HashMap<>();
        }

        // Lấy tất cả popular tags cho tất cả categories trong một lần query
        List<Object[]> allTagResults = documentCategoryRepository.findPopularTagsByCategoryIds(categoryIds);

        // Group results by category ID and apply limit
        Map<Long, List<String>> result = new HashMap<>();
        Map<Long, Integer> categoryCounts = new HashMap<>();

        for (Object[] row : allTagResults) {
            Long catId = (Long) row[0];
            String tagName = (String) row[1];

            int currentCount = categoryCounts.getOrDefault(catId, 0);
            if (currentCount < limit) {
                result.computeIfAbsent(catId, k -> new ArrayList<>()).add(tagName);
                categoryCounts.put(catId, currentCount + 1);
            }
        }

        return result;
    }
}