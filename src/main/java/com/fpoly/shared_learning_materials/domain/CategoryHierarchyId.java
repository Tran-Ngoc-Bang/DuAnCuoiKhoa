package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHierarchyId implements Serializable {

    private Long parentId;
    private Long childId;
}