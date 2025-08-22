package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String status;
    private Integer sortOrder;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long documentCount;
    private Long parentId;
    private int documents;
    private int views;
    private int downloads;
    private int subcategories;
    private LocalDateTime hierarchyCreatedAt;
    private LocalDateTime deletedAt;
    private int level; // Cấp độ phân cấp (0 = root, 1 = level 1, etc.)
    private String parentName; // Tên danh mục cha

    // Icon information
    private String iconClass; // CSS class cho icon
    private String iconStyle; // Inline style cho dynamic icons
    private String iconName; // FontAwesome icon name

    // Subcategories information
    private List<String> subcategoryNames; // Tên các danh mục con

    // Tags information
    private List<String> tags; // Các tag của danh mục
    private List<String> popularTags; // Các tag phổ biến của documents trong danh mục

}