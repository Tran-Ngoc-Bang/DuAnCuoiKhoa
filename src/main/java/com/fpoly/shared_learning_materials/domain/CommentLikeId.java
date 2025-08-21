package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeId implements Serializable {

    private Long commentId;
    private Long userId;
}