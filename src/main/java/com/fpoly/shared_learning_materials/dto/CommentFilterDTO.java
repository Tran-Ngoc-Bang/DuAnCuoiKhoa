package com.fpoly.shared_learning_materials.dto;

import lombok.Data;

@Data
public class CommentFilterDTO {
    private Long documentId;
    private Integer rating; // 1-5 stars, null for all
    private String sortBy; // "recent", "helpful", "high", "low"
    private Integer page = 0;
    private Integer size = 10;
}