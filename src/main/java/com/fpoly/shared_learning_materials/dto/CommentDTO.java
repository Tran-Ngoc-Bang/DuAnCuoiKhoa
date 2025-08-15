package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fpoly.shared_learning_materials.domain.Report;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommentDTO {
	private Long id;
	private Long documentId;
	private String documentTitle;
	private Long userId;
	private String userFullName;
	private String content;
	private String userAvatar;
	private String status; // active, hidden...
	private LocalDateTime createdAt;

	private boolean reported;
	private Long reportId;
	private String reporterName;
	private String reportReason;
	private LocalDateTime reportTime;
	
	private List<ReplyDTO> replies;


	public CommentDTO(Long id, Long documentId, String documentTitle, Long userId, String userFullName, String content,
			String userAvatar, String status, LocalDateTime createdAt) {
		this.id = id;
		this.documentId = documentId;
		this.documentTitle = documentTitle;
		this.userId = userId;
		this.userFullName = userFullName;
		this.content = content;
		this.userAvatar = userAvatar;
		this.status = status;
		this.createdAt = createdAt;
	}

	public void setReportInfo(Report report) {
		this.reported = true;	
		this.reportId = report.getId();
		this.reporterName = report.getReporter().getFullName();
		this.reportReason = report.getReason();
		this.reportTime = report.getCreatedAt();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUserAvatar() {
		return userAvatar;
	}

	public void setUserAvatar(String userAvatar) {
		this.userAvatar = userAvatar;
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

	public boolean isReported() {
		return reported;
	}

	public void setReported(boolean reported) {
		this.reported = reported;
	}

	public Long getReportId() {
		return reportId;
	}

	public void setReportId(Long reportId) {
		this.reportId = reportId;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getReportReason() {
		return reportReason;
	}

	public void setReportReason(String reportReason) {
		this.reportReason = reportReason;
	}

	public LocalDateTime getReportTime() {
		return reportTime;
	}

	public void setReportTime(LocalDateTime reportTime) {
		this.reportTime = reportTime;
	}

	public List<ReplyDTO> getReplies() {
		return replies;
	}

	public void setReplies(List<ReplyDTO> replies) {
		this.replies = replies;
	}
	
}
