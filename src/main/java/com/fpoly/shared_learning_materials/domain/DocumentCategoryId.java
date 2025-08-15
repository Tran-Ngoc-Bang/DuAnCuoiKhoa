package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Embeddable
@Data
public class DocumentCategoryId implements Serializable {

    private Long documentId;
    private Long categoryId;
	public DocumentCategoryId(Long documentId, Long categoryId) {
		super();
		this.documentId = documentId;
		this.categoryId = categoryId;
	}
	public Long getDocumentId() {
		return documentId;
	}
	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
    
    
}