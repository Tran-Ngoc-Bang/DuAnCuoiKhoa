package com.fpoly.shared_learning_materials.dto;

import java.time.LocalDateTime;

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
	private String userName;
	private String userAvatar;
	private String status; // active, hidden...
	private LocalDateTime createdAt;

	private boolean reported;
	private Long reportId;
	private String reporterName;
	private String reportReason;
	private LocalDateTime reportTime;

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
}
