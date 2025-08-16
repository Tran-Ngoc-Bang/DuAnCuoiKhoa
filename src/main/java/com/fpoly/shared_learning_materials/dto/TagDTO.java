package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private long documentCount;
    private String category;
    private String status;
    private String color;
    private double tagSize;
    private int searchCount;

   
    public TagDTO(Long id, String name, String slug, String description) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
    }

    // Constructor for simple usage
    public TagDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}