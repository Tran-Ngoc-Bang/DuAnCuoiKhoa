package com.fpoly.shared_learning_materials.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.dto.CommentDTO;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.CommentService;
import com.fpoly.shared_learning_materials.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin/comments")
public class CommentsController extends BaseAdminController {
	@Autowired
	CommentService commentService;

	public CommentsController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }
	@GetMapping
	public String showComments(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size,
			@RequestParam(defaultValue = "all") String tab, @RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "all") String statusFilter, Model model) {
		Page<CommentDTO> comments;

		switch (tab) {
			case "recent":
				comments = commentService.getCommentsNewThisWeek(page, size);
				break;
			case "reported":
				comments = commentService.getCommentsReported(page, size);
				break;
			case "hidden":
				comments = commentService.getCommentsHidden(page, size);
				break;
			default:
				comments = commentService.getCommentsWithPendingReports(page, size);
		}

		if (keyword != null && !keyword.isBlank() || !"all".equals(statusFilter)) {
			switch (statusFilter) {
				case "visible":
					comments = commentService.searchVisible(keyword, page, size);
					break;
				case "hidden":
					comments = commentService.searchHidden(keyword, page, size);
					break;
				case "reported":
					comments = commentService.searchReported(keyword, page, size);
					break;
				default: // all
					comments = commentService.searchAll(keyword, page, size);
			}
		}

		model.addAttribute("totalComments", commentService.countAllActiveComments());
		model.addAttribute("newComments", commentService.countNewCommentsThisWeek());
		model.addAttribute("reportedCount", commentService.countPendingReports());
		model.addAttribute("hiddenCount", commentService.countHiddenComments());

		model.addAttribute("commentsPage", comments);

		model.addAttribute("current", page);
		model.addAttribute("currentPage", "comments");
		model.addAttribute("current", page);
		model.addAttribute("totalPages", comments.getTotalPages());
		model.addAttribute("activeTab", tab);
		model.addAttribute("keyword", keyword);
		model.addAttribute("statusFilter", statusFilter);

		return "admin/comments/show";
	}

	@GetMapping("/{id}/report/reject")
	public String rejectReport(@PathVariable("id") Long commentId, HttpServletRequest request,
			RedirectAttributes redirectAttrs) {
		commentService.rejectReport(commentId);
		redirectAttrs.addFlashAttribute("successMessage", "Đã từ chối báo cáo và hiện lại bình luận.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/comments");
	}

	@GetMapping("/{id}/report/approve")
	public String approveReport(@PathVariable("id") Long commentId, HttpServletRequest request,
			RedirectAttributes redirectAttrs) {
		commentService.approveReport(commentId);
		redirectAttrs.addFlashAttribute("successMessage", "Đã chấp nhận báo cáo và ẩn bình luận.");
		String referer = request.getHeader("Referer");
		return "redirect:" + (referer != null ? referer : "/admin/comments");
	}

	@GetMapping("/{id}/hide")
	public String hideComment(@PathVariable Long id, HttpServletRequest req, RedirectAttributes ra) {
		commentService.hideComment(id);
		ra.addFlashAttribute("successMessage", "Đã ẩn bình luận.");
		String ref = req.getHeader("Referer");
		return "redirect:" + (ref != null ? ref : "/admin/comments");
	}

	@GetMapping("/{id}/show")
	public String showComment(@PathVariable Long id, HttpServletRequest req, RedirectAttributes ra) {
		commentService.showComment(id);
		ra.addFlashAttribute("successMessage", "Đã hiển thị bình luận.");
		String ref = req.getHeader("Referer");
		return "redirect:" + (ref != null ? ref : "/admin/comments");
	}

	@GetMapping("/{id}/delete")
	public String deleteComment(@PathVariable Long id, HttpServletRequest req, RedirectAttributes ra) {
		commentService.deleteComment(id);
		ra.addFlashAttribute("successMessage", "Đã xóa bình luận.");
		String ref = req.getHeader("Referer");
		return "redirect:" + (ref != null ? ref : "/admin/comments");
	}
}
