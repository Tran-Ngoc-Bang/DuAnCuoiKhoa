package com.fpoly.shared_learning_materials.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.domain.Comment;
import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.CommentRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.ReportRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @GetMapping("/reportcomment")
    public String showReportForm(
            @RequestParam(required = false) Long commentId,
            @RequestParam(required = false) Long documentId,
            @RequestParam(required = false) String returnUrl,
            Model model) {

        // Get comment info if reporting a comment
        if (commentId != null) {
            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            if (commentOpt.isPresent()) {
                Comment comment = commentOpt.get();
                model.addAttribute("comment", comment);
                model.addAttribute("commentId", commentId);
                
                // If documentId not provided, get from comment
                if (documentId == null && comment.getDocument() != null) {
                    documentId = comment.getDocument().getId();
                }
            }
        }

        model.addAttribute("documentId", documentId);
        model.addAttribute("returnUrl", returnUrl != null ? returnUrl : "/admin/documents");

        return "admin/reportcomment";
    }

    @PostMapping("/reportcomment")
    public String createReport(
            @RequestParam(required = false) Long commentId,
            @RequestParam(required = false) Long documentId,
            @RequestParam String type,
            @RequestParam String reason,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User reporter = null;

            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        reporter = userOpt.get();
                    }
                }
            }

            if (reporter == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để báo cáo vi phạm");
                return "redirect:" + (returnUrl != null ? returnUrl : "/admin/documents");
            }

            // Create report
            Report report = new Report();
            report.setReporter(reporter);
            report.setType(type);
            report.setReason(reason);
            report.setStatus("pending");
            report.setCreatedAt(LocalDateTime.now());

            // Set comment if provided
            if (commentId != null) {
                Optional<Comment> commentOpt = commentRepository.findById(commentId);
                if (commentOpt.isPresent()) {
                    report.setComment(commentOpt.get());
                    // Get document from comment if not provided
                    if (documentId == null) {
                        Comment comment = commentOpt.get();
                        if (comment.getDocument() != null) {
                            report.setDocument(comment.getDocument());
                        }
                    }
                }
            }

            // Set document if provided
            if (documentId != null) {
                Optional<Document> documentOpt = documentRepository.findById(documentId);
                if (documentOpt.isPresent()) {
                    report.setDocument(documentOpt.get());
                }
            }

            // Save report
            reportRepository.save(report);

            redirectAttributes.addFlashAttribute("success", "Báo cáo vi phạm đã được gửi thành công. Chúng tôi sẽ xem xét và xử lý trong thời gian sớm nhất.");
            
        } catch (Exception e) {
            System.err.println("Error creating report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi gửi báo cáo: " + e.getMessage());
        }

        return "redirect:" + (returnUrl != null ? returnUrl : "/admin/documents");
    }
}