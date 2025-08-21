package com.fpoly.shared_learning_materials.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentContentDTO {
    private String title;
    private String pageRange;
    private Integer pageNumber;
    private String description;
}