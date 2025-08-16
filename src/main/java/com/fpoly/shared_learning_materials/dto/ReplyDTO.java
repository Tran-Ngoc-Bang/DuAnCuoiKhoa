package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReplyDTO {
    private Long id;
    private Long commentId;
    private Long userId;
    private String userFullName;
    private String userAvatarUrl;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    
	public ReplyDTO(Long id, Long commentId, Long userId, String userFullName, String userAvatarUrl, String content,
			String status, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.commentId = commentId;
		this.userId = userId;
		this.userFullName = userFullName;
		this.userAvatarUrl = userAvatarUrl;
		this.content = content;
		this.status = status;
		this.createdAt = createdAt;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getCommentId() {
		return commentId;
	}
	public void setCommentId(Long commentId) {
		this.commentId = commentId;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getUserFullName() {
		return userFullName;
	}
	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}
	public String getUserAvatarUrl() {
		return userAvatarUrl;
	}
	public void setUserAvatarUrl(String userAvatarUrl) {
		this.userAvatarUrl = userAvatarUrl;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
    
    
}
