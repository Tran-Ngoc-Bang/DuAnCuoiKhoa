package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title", nullable = false, columnDefinition = "nvarchar(255)")
	private String title;

	@Column(name = "slug", unique = true, nullable = false)
	private String slug;

	@Column(name = "description", columnDefinition = "nvarchar(max)")
	private String description;

	@Column(name = "price", precision = 18, scale = 2)
	private BigDecimal price = BigDecimal.ZERO;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_id")
	private File file;

	@Column(name = "status", length = 20)
	private String status = "draft";

	@Column(name = "visibility", length = 20)
	private String visibility = "public";

	@Column(name = "downloads_count")
	private Long downloadsCount = 0L;

	@Column(name = "views_count")
	private Long viewsCount = 0L;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "published_at")
	private LocalDateTime publishedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<DocumentCategory> documentCategories = new ArrayList<>();

	@OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
	private List<Comment> comments = new ArrayList<>();

	@Transient 
    public Double getRating() {
        if (comments == null || comments.isEmpty()) {
            return 0.0;
        }
        return comments.stream()
                .filter(c -> c.getRating() != null)
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0.0);
    }


	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public enum DocumentStatus {
		DRAFT, PENDING, APPROVED, REJECTED, DELETED
	}

	public enum DocumentType {
		PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT, ZIP, RAR, OTHER
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public Long getDownloadsCount() {
		return downloadsCount;
	}

	public void setDownloadsCount(Long downloadsCount) {
		this.downloadsCount = downloadsCount;
	}

	public Long getViewsCount() {
		return viewsCount;
	}

	public void setViewsCount(Long viewsCount) {
		this.viewsCount = viewsCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public LocalDateTime getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(LocalDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

}