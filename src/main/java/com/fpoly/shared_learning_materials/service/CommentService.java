package com.fpoly.shared_learning_materials.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.fpoly.shared_learning_materials.domain.Comment;
import com.fpoly.shared_learning_materials.domain.CommentLike;
import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.CommentDTO;
import com.fpoly.shared_learning_materials.dto.CommentFilterDTO;
import com.fpoly.shared_learning_materials.dto.ReplyDTO;
import com.fpoly.shared_learning_materials.repository.CommentRepository;
import com.fpoly.shared_learning_materials.repository.CommentLikeRepository;
import com.fpoly.shared_learning_materials.repository.ReplyRepository;
import com.fpoly.shared_learning_materials.repository.ReportRepository;

@Service
public class CommentService {

	@Autowired
	private CommentRepository commentRepository;

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private ReplyRepository replyRepository;

	@Autowired
	private CommentLikeRepository commentLikeRepository;

	public Page<CommentDTO> getCommentsWithPendingReports(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<Comment> commentPage = commentRepository.findByDeletedAtIsNull(pageable);
		return commentPage.map(c -> {
			CommentDTO dto = new CommentDTO(c.getId(), c.getDocument().getId(), c.getDocument().getTitle(),
					c.getUser().getId(), c.getUser().getFullName(), c.getContent(), c.getUser().getAvatarUrl(),
					c.getStatus(), c.getCreatedAt());
			dto.setReplies(mapRepliesToDto(c.getId()));

			Optional<Report> optReport = reportRepository.findFirstByCommentIdAndStatusOrderByCreatedAtAsc(c.getId(),
					"pending");
			optReport.ifPresent(dto::setReportInfo);

			return dto;
		});
	}

	public Page<CommentDTO> getCommentsReported(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		List<Long> reportedIds = reportRepository.findByStatus("pending").stream()
				.filter(r -> r.getComment() != null && r.getComment().getId() != null)
				.map(r -> r.getComment().getId())
				.distinct()
				.collect(Collectors.toList());

		if (reportedIds.isEmpty()) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		Page<Comment> commentPage = commentRepository.findByDeletedAtIsNullAndIdIn(reportedIds, pageable);

		return mapToDtoPage(commentPage);
	}

	public Page<CommentDTO> getCommentsHidden(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<Comment> commentPage = commentRepository.findByDeletedAtIsNullAndStatusNot("active", pageable);

		return mapToDtoPage(commentPage);
	}

	public long countAllActiveComments() {
		return commentRepository.countByDeletedAtIsNull();
	}

	public long countNewCommentsThisWeek() {
		LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
		return commentRepository.countByDeletedAtIsNullAndCreatedAtAfter(weekAgo);
	}

	public long countPendingReports() {
		return reportRepository.countReportedComments();
	}

	public long countHiddenComments() {
		return commentRepository.countByStatusNotAndDeletedAtIsNull("active");
	}

	public Page<CommentDTO> getCommentsNewThisWeek(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);

		Page<Comment> commentPage = commentRepository.findByDeletedAtIsNullAndCreatedAtAfter(weekAgo, pageable);

		return mapToDtoPage(commentPage);
	}

	private Page<CommentDTO> mapToDtoPage(Page<Comment> commentPage) {
		List<Long> idsOnPage = commentPage.getContent().stream().map(Comment::getId).collect(Collectors.toList());

		Map<Long, Report> firstPending = reportRepository.findByStatus("pending").stream()
				.filter(r -> r.getComment() != null && r.getComment().getId() != null)
				.filter(r -> idsOnPage.contains(r.getComment().getId()))
				.collect(Collectors.toMap(
						r -> r.getComment().getId(),
						r -> r,
						(r1, r2) -> r1.getCreatedAt().isBefore(r2.getCreatedAt()) ? r1 : r2));

		List<CommentDTO> dtos = commentPage.getContent().stream().map(c -> {
			CommentDTO dto = new CommentDTO(c.getId(), c.getDocument().getId(), c.getDocument().getTitle(),
					c.getUser().getId(), c.getUser().getFullName(), c.getContent(), c.getUser().getAvatarUrl(),
					c.getStatus(), c.getCreatedAt());
			Report rpt = firstPending.get(c.getId());
			if (rpt != null) {
				dto.setReportInfo(rpt);
			}

			dto.setReplies(mapRepliesToDto(c.getId()));

			return dto;
		}).collect(Collectors.toList());

		return new PageImpl<>(dtos, commentPage.getPageable(), commentPage.getTotalElements());
	}

	private CommentDTO mapToDto(Comment c) {
		CommentDTO dto = new CommentDTO(c.getId(), c.getDocument().getId(), c.getDocument().getTitle(),
				c.getUser().getId(), c.getUser().getFullName(), c.getContent(), c.getUser().getAvatarUrl(),
				c.getStatus(), c.getCreatedAt());
		return dto;
	}

	public void approveReport(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

		comment.setStatus("inactive");
		commentRepository.save(comment);

		Optional<Report> optReport = reportRepository.findFirstByCommentIdAndStatusOrderByCreatedAtAsc(commentId,
				"pending");
		if (optReport.isPresent()) {
			Report report = optReport.get();
			report.setStatus("active");
			report.setReviewedAt(LocalDateTime.now());
			reportRepository.save(report);
		}
	}

	public void rejectReport(Long commentId) {
		Comment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));

		comment.setStatus("active");
		commentRepository.save(comment);

		Optional<Report> optReport = reportRepository.findFirstByCommentIdAndStatusOrderByCreatedAtAsc(commentId,
				"pending");
		if (optReport.isPresent()) {
			Report report = optReport.get();
			report.setStatus("inactive");
			report.setReviewedAt(LocalDateTime.now());
			reportRepository.save(report);
		}
	}

	public void hideComment(Long commentId) {
		Comment c = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
		c.setStatus("inactive");
		commentRepository.save(c);
	}

	public void showComment(Long commentId) {
		Comment c = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
		c.setStatus("active");
		commentRepository.save(c);
	}

	public void deleteComment(Long commentId) {
		Comment c = commentRepository.findById(commentId)
				.orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
		c.setDeletedAt(LocalDateTime.now());
		commentRepository.save(c);
	}

	// Like/Dislike methods
	public Map<String, Object> likeComment(Long commentId, Long userId) {
		Map<String, Object> result = new java.util.HashMap<>();

		try {
			Comment comment = commentRepository.findById(commentId)
					.orElseThrow(() -> new RuntimeException("Comment not found"));

			User user = new User();
			user.setId(userId);

			// Check if user already liked this comment
			Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

			if (existingLike.isPresent()) {
				CommentLike like = existingLike.get();
				if (like.getLikeType() == CommentLike.LikeType.LIKE) {
					// User already liked, remove the like
					commentLikeRepository.delete(like);
					result.put("action", "removed");
					result.put("success", true);
				} else {
					// User disliked before, change to like
					like.setLikeType(CommentLike.LikeType.LIKE);
					commentLikeRepository.save(like);
					result.put("action", "liked");
					result.put("success", true);
				}
			} else {
				// Create new like
				CommentLike newLike = new CommentLike();
				newLike.setId(new com.fpoly.shared_learning_materials.domain.CommentLikeId(commentId, userId));
				newLike.setComment(comment);
				newLike.setUser(user);
				newLike.setLikeType(CommentLike.LikeType.LIKE);
				commentLikeRepository.save(newLike);
				result.put("action", "liked");
				result.put("success", true);
			}

			// Update counts
			result.put("likesCount", commentLikeRepository.countLikesByCommentId(commentId));
			result.put("dislikesCount", commentLikeRepository.countDislikesByCommentId(commentId));

		} catch (Exception e) {
			result.put("success", false);
			result.put("error", e.getMessage());
		}

		return result;
	}

	public Map<String, Object> dislikeComment(Long commentId, Long userId) {
		Map<String, Object> result = new java.util.HashMap<>();

		try {
			Comment comment = commentRepository.findById(commentId)
					.orElseThrow(() -> new RuntimeException("Comment not found"));

			User user = new User();
			user.setId(userId);

			// Check if user already disliked this comment
			Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId);

			if (existingLike.isPresent()) {
				CommentLike like = existingLike.get();
				if (like.getLikeType() == CommentLike.LikeType.DISLIKE) {
					// User already disliked, remove the dislike
					commentLikeRepository.delete(like);
					result.put("action", "removed");
					result.put("success", true);
				} else {
					// User liked before, change to dislike
					like.setLikeType(CommentLike.LikeType.DISLIKE);
					commentLikeRepository.save(like);
					result.put("action", "disliked");
					result.put("success", true);
				}
			} else {
				// Create new dislike
				CommentLike newLike = new CommentLike();
				newLike.setId(new com.fpoly.shared_learning_materials.domain.CommentLikeId(commentId, userId));
				newLike.setComment(comment);
				newLike.setUser(user);
				newLike.setLikeType(CommentLike.LikeType.DISLIKE);
				commentLikeRepository.save(newLike);
				result.put("action", "disliked");
				result.put("success", true);
			}

			// Update counts
			result.put("likesCount", commentLikeRepository.countLikesByCommentId(commentId));
			result.put("dislikesCount", commentLikeRepository.countDislikesByCommentId(commentId));

		} catch (Exception e) {
			result.put("success", false);
			result.put("error", e.getMessage());
		}

		return result;
	}

	public void populateLikeInfo(CommentDTO dto, Long currentUserId) {
		if (currentUserId != null) {
			Optional<CommentLike> userLike = commentLikeRepository.findByCommentIdAndUserId(dto.getId(), currentUserId);
			if (userLike.isPresent()) {
				dto.setIsLikedByCurrentUser(userLike.get().getLikeType() == CommentLike.LikeType.LIKE);
				dto.setIsDislikedByCurrentUser(userLike.get().getLikeType() == CommentLike.LikeType.DISLIKE);
			}
		}

		dto.setLikesCount(commentLikeRepository.countLikesByCommentId(dto.getId()));
		dto.setDislikesCount(commentLikeRepository.countDislikesByCommentId(dto.getId()));
	}

	public Page<CommentDTO> searchAll(String keyword, int page, int size) {
		Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Specification<Comment> spec = Specification.where((r, q, cb) -> cb.isNull(r.get("deletedAt")));

		if (keyword != null && !keyword.isBlank()) {
			spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("content")), "%" + keyword.toLowerCase() + "%"));
		}

		Page<Comment> comments = commentRepository.findAll(spec, pg);
		return mapToDtoPage(comments);
	}

	public Page<CommentDTO> searchVisible(String keyword, int page, int size) {
		Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

		List<Long> reportedIds = reportRepository.findByStatus("pending").stream()
				.filter(r -> r.getComment() != null && r.getComment().getId() != null)
				.map(r -> r.getComment().getId())
				.distinct()
				.collect(Collectors.toList());

		Specification<Comment> spec = Specification.where(
				(root, query, cb) -> cb.and(cb.isNull(root.get("deletedAt")), cb.equal(root.get("status"), "active")));

		if (!reportedIds.isEmpty()) {
			spec = spec.and((root, query, cb) -> cb.not(root.get("id").in(reportedIds)));
		}

		if (keyword != null && !keyword.isBlank()) {
			spec = spec.and(
					(root, query, cb) -> cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%"));
		}

		Page<Comment> comments = commentRepository.findAll(spec, pg);
		return mapToDtoPage(comments);
	}

	public Page<CommentDTO> searchHidden(String keyword, int page, int size) {
		Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Specification<Comment> spec = Specification
				.where((r, q, cb) -> cb.and(cb.isNull(r.get("deletedAt")), cb.notEqual(r.get("status"), "active")));

		if (keyword != null && !keyword.isBlank()) {
			spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("content")), "%" + keyword.toLowerCase() + "%"));
		}

		Page<Comment> comments = commentRepository.findAll(spec, pg);
		return mapToDtoPage(comments);
	}

	public Page<CommentDTO> searchReported(String keyword, int page, int size) {
		Pageable pg = PageRequest.of(page, size, Sort.by("createdAt").descending());

		List<Long> ids = reportRepository.findByStatus("pending").stream()
				.filter(r -> r.getComment() != null && r.getComment().getId() != null)
				.map(r -> r.getComment().getId())
				.distinct()
				.toList();

		if (ids.isEmpty()) {
			return new PageImpl<>(List.of(), pg, 0);
		}

		Specification<Comment> spec = Specification
				.where((r, q, cb) -> cb.and(cb.isNull(r.get("deletedAt")), r.get("id").in(ids)));

		if (keyword != null && !keyword.isBlank()) {
			spec = spec.and((r, q, cb) -> cb.like(cb.lower(r.get("content")), "%" + keyword.toLowerCase() + "%"));
		}

		Page<Comment> comments = commentRepository.findAll(spec, pg);
		return mapToDtoPage(comments);
	}

	public CommentDTO getCommentById(Long id) {
		Comment comment = commentRepository.findById(id).get();
		CommentDTO commentDTO = mapToDto(comment);
		commentDTO.setReplies(mapRepliesToDto(id));
		return commentDTO;
	}

	private List<ReplyDTO> mapRepliesToDto(Long commentId) {
		return replyRepository.findByCommentIdAndDeletedAtIsNullOrderByCreatedAtAsc(commentId)
				.stream()
				.map(r -> new ReplyDTO(
						r.getId(),
						r.getComment().getId(),
						r.getUser().getId(),
						r.getUser().getFullName(),
						r.getUser().getAvatarUrl(),
						r.getContent(),
						r.getStatus(),
						r.getCreatedAt()))
				.collect(Collectors.toList());
	}

	/**
	 * Get filtered and paginated comments for a document (MVC approach)
	 */
	public Page<CommentDTO> getFilteredComments(Long documentId, Integer rating, String sortBy, int page, int size) {
		// Create pageable with sorting
		Sort sort = Sort.by("createdAt").descending();
		if ("helpful".equals(sortBy)) {
			sort = Sort.by("likesCount").descending().and(Sort.by("createdAt").descending());
		} else if ("high".equals(sortBy)) {
			sort = Sort.by("rating").descending().and(Sort.by("createdAt").descending());
		} else if ("low".equals(sortBy)) {
			sort = Sort.by("rating").ascending().and(Sort.by("createdAt").descending());
		}

		Pageable pageable = PageRequest.of(page, size, sort);

		// Build specification for filtering
		Specification<Comment> spec = Specification.where(
				(root, query, cb) -> cb.and(
						cb.isNull(root.get("deletedAt")),
						cb.equal(root.get("document").get("id"), documentId),
						cb.equal(root.get("status"), "active")));

		// Add rating filter if specified
		if (rating != null && rating >= 1 && rating <= 5) {
			spec = spec.and((root, query, cb) -> cb.equal(root.get("rating"), rating));
		}

		// Get comments with filtering
		Page<Comment> commentPage = commentRepository.findAll(spec, pageable);

		// Convert to DTOs and add like/dislike info
		Page<CommentDTO> dtoPage = commentPage.map(this::mapToDto);

		return dtoPage;
	}
}
