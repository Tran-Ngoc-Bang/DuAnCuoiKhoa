package com.fpoly.shared_learning_materials.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fpoly.shared_learning_materials.domain.File;
import com.fpoly.shared_learning_materials.domain.Document.DocumentStatus;

import lombok.Data;



@Data
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
    private List<CommentDTO> comments;
    
    
    
    
    
	public List<CommentDTO> getComments() {
		return comments;
	}
	public void setComments(List<CommentDTO> comments) {
		this.comments = comments;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getAuthorAvatar() {
		return authorAvatar;
	}
	public void setAuthorAvatar(String authorAvatar) {
		this.authorAvatar = authorAvatar;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
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
	public Long getFileId() {
		return fileId;
	}
	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}
	public Double getFileSize() {
		return fileSize;
	}
	public void setFileSize(Double fileSize) {
		this.fileSize = fileSize;
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
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public List<Long> getCategoryIds() {
		return categoryIds;
	}
	public void setCategoryIds(List<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}
	public List<String> getTagNames() {
		return tagNames;
	}
	public void setTagNames(List<String> tagNames) {
		this.tagNames = tagNames;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getAuthorName() {
		return authorName;
	}
	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
	public List<String> getCategoryNames() {
		return categoryNames;
	}
	public void setCategoryNames(List<String> categoryNames) {
		this.categoryNames = categoryNames;
	}
	    
	    
    
    
    
    
	    
	    
    
    
	
 
}
