package com.fpoly.shared_learning_materials.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private BigDecimal price;
    private Long fileId;
    private Double fileSize;
    private String status;
    private String visibility;
    private Long downloadsCount;
    private Long viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private LocalDateTime deletedAt;
    private String fileType;
    private List<Long> categoryIds;
    private List<String> tagNames;
    private Long userId;
    private String authorName; // Thêm trường cho tên tác giả
    private List<String> categoryNames;
    private String thumbnail;
    private String authorAvatar;
    private String fileName;
    private String filePath;
    private List<CommentDTO> comments;
    private Double averageRating;
    private Map<String, Integer> ratingDistribution;
    private List<DocumentContentDTO> contents;
    private Long parentId;

}
