package com.fpoly.shared_learning_materials.controller.client;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.domain.Comment;
import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.DocumentOwner;
import com.fpoly.shared_learning_materials.domain.DocumentOwnerId;
import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.dto.CommentDTO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.repository.DocumentOwnerRepository;
import com.fpoly.shared_learning_materials.repository.ReportRepository;
import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.service.CommentService;
import com.fpoly.shared_learning_materials.service.DocumentAnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Files;
import java.io.IOException;

import com.fpoly.shared_learning_materials.service.DocumentPurchaseService;

/**
 * Controller for client document-related pages
 */
@Controller
@RequestMapping("/documents")
public class ClientDocumentController {

    private static final Logger logger = LoggerFactory.getLogger(ClientDocumentController.class);

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DocumentOwnerRepository documentOwnerRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private DocumentAnalyticsService documentAnalyticsService;

    @Autowired
    private DocumentPurchaseService documentPurchaseService;

    @Value("${app.uploads.base-path:src/main/resources/static/uploads/documents}")
    private String uploadsBasePath;

    @GetMapping("/{id}")
    public String documentDetails(@PathVariable Long id,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model,
            HttpServletRequest request) {
        logger.info("Accessing document details for ID: {} with filters - rating: {}, sortBy: {}, page: {}, size: {}",
                id, rating, sortBy, page, size);

        // Validate input parameters
        if (id == null || id <= 0) {
            logger.warn("Invalid document ID provided: {}", id);
            model.addAttribute("errorMessage", "ID tài liệu không hợp lệ");
            return "error/404";
        }

        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                model.addAttribute("errorMessage", "Không tìm thấy tài liệu");
                return "error/404";
            }

            // Determine ownership for current user
            boolean isOwned = false;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !("anonymousUser".equals(auth.getPrincipal()))) {
                String username = auth.getName();
                User user = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
                if (user != null) {
                    Document entity = documentService.findById(id);
                    if (entity != null) {
                        isOwned = documentOwnerRepository.existsByUserAndDocument(user, entity);
                    }
                }
            }

            // TODO: Implement user authentication and comment permission logic
            // For now, allow all users to comment
            Map<String, Object> commentPermission = new java.util.HashMap<>();
            commentPermission.put("canComment", true);
            commentPermission.put("message", "Bạn có thể bình luận cho tài liệu này");
            commentPermission.put("action", "allowed");

            // Load comments with pagination and filters
            Page<CommentDTO> commentsPage = commentService.getFilteredComments(id, rating, sortBy, page, size);

            // Get related documents
            List<DocumentDTO> relatedDocuments = documentService.getRelatedDocuments(id, 4);

            // Pass DocumentDTO to template
            model.addAttribute("document", document);
            model.addAttribute("pageTitle", document.getTitle());
            model.addAttribute("comments", commentsPage.getContent());
            model.addAttribute("relatedDocuments", relatedDocuments);
            model.addAttribute("commentPermission", commentPermission);
            model.addAttribute("isOwned", isOwned);

            // Set pagination values from comments page
            model.addAttribute("currentPage", commentsPage.getNumber());
            model.addAttribute("totalPages", commentsPage.getTotalPages());
            model.addAttribute("totalElements", commentsPage.getTotalElements());
            model.addAttribute("hasNext", commentsPage.hasNext());
            model.addAttribute("hasPrevious", commentsPage.hasPrevious());
            model.addAttribute("currentRating", rating);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSize", size);

            return "client/document-details";

        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument when accessing document ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Tham số không hợp lệ");
            return "error/404";
        } catch (SecurityException e) {
            logger.error("Security exception when accessing document ID {}: {}", id, e.getMessage());
            model.addAttribute("errorMessage", "Không có quyền truy cập tài liệu này");
            return "error/404";
        } catch (Exception e) {
            logger.error("Unexpected error when accessing document ID {}: {}", id, e.getMessage(), e);
            model.addAttribute("errorMessage",
                    "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau hoặc liên hệ quản trị viên");
            return "error/500";
        }
    }

    @GetMapping("/{id}/pdf-view")
    public String viewDocument(@PathVariable Long id, Model model) {
        Document document = documentService.findById(id);

        List<Document> relatedDocuments = documentService.getRelatedDocuments(document, 3);

        model.addAttribute("pageTitle", "Xem tài liệu");
        model.addAttribute("documentId", id);
        model.addAttribute("document", document);
        model.addAttribute("relatedDocuments", relatedDocuments);

        boolean isOwned = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            User user = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (user != null) {
                isOwned = documentOwnerRepository.existsByUserAndDocument(user, document);
            }
        }

        model.addAttribute("isOwned", isOwned);

        return "client/pdf-viewer";
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để tải tài liệu");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }

        Document document = documentService.findById(id);
        if (document == null || document.getFile() == null) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy tài liệu");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }

        try {
            // Ủy quyền xử lý giao dịch tải tài liệu cho service
            documentPurchaseService.processDocumentDownload(currentUser, id);
            redirectAttributes.addFlashAttribute("toastMessage", "Bắt đầu tải xuống...");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("toastMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("toastMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }

        Path filePath = Paths.get(uploadsBasePath)
                .resolve(document.getFile().getFileName());
        File file = filePath.toFile();

        if (!file.exists()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Tập tin không tồn tại trên hệ thống");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            String contentType;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException ioEx) {
                contentType = null;
            }
            if (contentType == null)
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không thể mở file để tải về");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "/documents/" + id)
                    .build();
        }
    }

    /**
     * Tăng lượt xem và redirect về trang chi tiết
     */
    @PostMapping("/{id}/view")
    public String incrementViewCount(@PathVariable Long id, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Get session ID and IP address
            String sessionId = request.getSession().getId();
            String ipAddress = getClientIpAddress(request);

            // Increment view count using analytics service
            boolean success = documentAnalyticsService.incrementViewCount(id, sessionId, ipAddress);

            if (success) {
                redirectAttributes.addFlashAttribute("viewCountUpdated", true);
            }

        } catch (Exception e) {
            logger.error("Error incrementing view count for document ID {}: {}", id, e.getMessage());
        }

        // Redirect về trang chi tiết document
        return "redirect:/documents/" + id;
    }

    /**
     * Tăng lượt tải và redirect về trang chi tiết
     */
    @PostMapping("/{id}/download")
    public String incrementDownloadCount(@PathVariable Long id, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {
        try {
            // Get session ID and IP address
            String sessionId = request.getSession().getId();
            String ipAddress = getClientIpAddress(request);

            // Increment download count using analytics service
            boolean success = documentAnalyticsService.incrementDownloadCount(id, sessionId, ipAddress);

            if (success) {
                redirectAttributes.addFlashAttribute("downloadCountUpdated", true);
            }

        } catch (Exception e) {
            logger.error("Error incrementing download count for document ID {}: {}", id, e.getMessage());
        }

        // Redirect về trang chi tiết document
        return "redirect:/documents/" + id;
    }

    /**
     * Lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    @GetMapping("/uploads/documents/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            // Decode the filename
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            logger.info("Serving file: {}", decodedFileName);

            // Construct the file path
            Path filePath = Paths.get(uploadsBasePath).resolve(decodedFileName);
            Resource resource = new UrlResource(filePath.toUri());

            // Check if file exists
            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = determineContentType(decodedFileName);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + decodedFileName + "\"")
                        .body(resource);
            } else {
                logger.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error serving file: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/file/{fileName:.+}")
    public ResponseEntity<Resource> serveDocumentFile(@PathVariable String fileName) {
        try {
            String decodedFileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8.toString());
            Path filePath = Paths.get(uploadsBasePath).resolve(decodedFileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = determineContentType(decodedFileName);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + decodedFileName + "\"")
                        .body(resource);
            } else {
                logger.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error serving file: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            // PDF
            case "pdf":
                return "application/pdf";

            // Images
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            case "bmp":
                return "image/bmp";
            case "svg":
                return "image/svg+xml";
            case "ico":
                return "image/x-icon";

            // Text files
            case "txt":
                return "text/plain";
            case "md":
                return "text/markdown";
            case "csv":
                return "text/csv";
            case "json":
                return "application/json";
            case "xml":
                return "application/xml";
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "application/javascript";
            case "py":
                return "text/x-python";
            case "java":
                return "text/x-java-source";
            case "cpp":
            case "c":
                return "text/x-c++src";
            case "php":
                return "text/x-php";
            case "sql":
                return "text/x-sql";
            case "log":
                return "text/plain";

            // Office files
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "doc":
                return "application/msword";
            case "xls":
                return "application/vnd.ms-excel";
            case "ppt":
                return "application/vnd.ms-powerpoint";
            case "odt":
                return "application/vnd.oasis.opendocument.text";
            case "ods":
                return "application/vnd.oasis.opendocument.spreadsheet";
            case "odp":
                return "application/vnd.oasis.opendocument.presentation";

            // Archives
            case "zip":
                return "application/zip";
            case "rar":
                return "application/vnd.rar";
            case "7z":
                return "application/x-7z-compressed";
            case "tar":
                return "application/x-tar";
            case "gz":
                return "application/gzip";
            case "bz2":
                return "application/x-bzip2";

            // Video
            case "mp4":
                return "video/mp4";
            case "avi":
                return "video/x-msvideo";
            case "mov":
                return "video/quicktime";
            case "wmv":
                return "video/x-ms-wmv";
            case "flv":
                return "video/x-flv";
            case "webm":
                return "video/webm";
            case "mkv":
                return "video/x-matroska";

            // Audio
            case "mp3":
                return "audio/mpeg";
            case "wav":
                return "audio/wav";
            case "flac":
                return "audio/flac";
            case "aac":
                return "audio/aac";
            case "ogg":
                return "audio/ogg";
            case "wma":
                return "audio/x-ms-wma";

            default:
                return "application/octet-stream";
        }
    }

    @PostMapping("/{id}/review")
    public String submitReview(@PathVariable Long id,
            @RequestParam("rating") Integer rating,
            @RequestParam("review") String review,
            RedirectAttributes redirectAttributes) {
        logger.info("Submitting review for document ID: {}, rating: {}, review: {}", id, rating, review);

        try {
            // Validate input
            if (rating == null || rating < 1 || rating > 5) {
                redirectAttributes.addFlashAttribute("errorMessage", "Đánh giá sao phải từ 1-5");
                return "redirect:/documents/" + id;
            }

            if (review == null || review.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Nội dung đánh giá không được để trống");
                return "redirect:/documents/" + id;
            }

            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để gửi đánh giá");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + id;
            }

            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + id;
            }

            // Submit review through service
            try {
                Comment savedComment = documentService.submitReview(id, currentUser.getId(), rating, review.trim());
                redirectAttributes.addFlashAttribute("toastMessage", "Đánh giá của bạn đã được gửi thành công!");
                redirectAttributes.addFlashAttribute("toastType", "success");
                redirectAttributes.addFlashAttribute("newCommentId", savedComment.getId());
                logger.info("Review submitted successfully for document ID: {} with comment ID: {}", id,
                        savedComment.getId());
            } catch (Exception serviceException) {
                logger.error("Service error: {}", serviceException.getMessage(), serviceException);
                redirectAttributes.addFlashAttribute("toastMessage", "Lỗi service: " + serviceException.getMessage());
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + id;
            }

        } catch (Exception e) {
            logger.error("Error submitting review for document ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại.");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/documents/" + id;
    }

    @PostMapping("/{documentId}/comments/{commentId}/like")
    public String likeComment(
            @PathVariable Long documentId,
            @PathVariable Long commentId,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            Map<String, Object> result = commentService.likeComment(commentId, currentUser.getId());

            if ((Boolean) result.get("success")) {
                String action = (String) result.get("action");
                Long likesCount = (Long) result.get("likesCount");
                Long dislikesCount = (Long) result.get("dislikesCount");

                // Lưu thông tin cập nhật để JavaScript có thể cập nhật UI
                redirectAttributes.addFlashAttribute("updatedLikesCount", likesCount);
                redirectAttributes.addFlashAttribute("updatedDislikesCount", dislikesCount);
                redirectAttributes.addFlashAttribute("updatedCommentId", commentId);
                redirectAttributes.addFlashAttribute("updatedAction", action);

                // Lưu thông báo để JavaScript hiển thị toast
                if ("liked".equals(action)) {
                    redirectAttributes.addFlashAttribute("toastMessage", "Đã thích bình luận!");
                    redirectAttributes.addFlashAttribute("toastType", "success");
                } else if ("removed".equals(action)) {
                    redirectAttributes.addFlashAttribute("toastMessage", "Đã bỏ thích bình luận");
                    redirectAttributes.addFlashAttribute("toastType", "info");
                }
            } else {
                redirectAttributes.addFlashAttribute("toastMessage", (String) result.get("error"));
                redirectAttributes.addFlashAttribute("toastType", "error");
            }

        } catch (Exception e) {
            logger.error("Error liking comment {}: {}", commentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra khi like comment");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/documents/" + documentId + "#comment-" + commentId;
    }

    @PostMapping("/{documentId}/comments/{commentId}/dislike")
    public String dislikeComment(
            @PathVariable Long documentId,
            @PathVariable Long commentId,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            Map<String, Object> result = commentService.dislikeComment(commentId, currentUser.getId());

            if ((Boolean) result.get("success")) {
                String action = (String) result.get("action");
                Long likesCount = (Long) result.get("likesCount");
                Long dislikesCount = (Long) result.get("dislikesCount");

                // Lưu thông tin cập nhật để JavaScript có thể cập nhật UI
                redirectAttributes.addFlashAttribute("updatedLikesCount", likesCount);
                redirectAttributes.addFlashAttribute("updatedDislikesCount", dislikesCount);
                redirectAttributes.addFlashAttribute("updatedCommentId", commentId);
                redirectAttributes.addFlashAttribute("updatedAction", action);

                // Lưu thông báo để JavaScript hiển thị toast
                if ("disliked".equals(action)) {
                    redirectAttributes.addFlashAttribute("toastMessage", "Đã không thích bình luận");
                    redirectAttributes.addFlashAttribute("toastType", "warning");
                } else if ("removed".equals(action)) {
                    redirectAttributes.addFlashAttribute("toastMessage", "Đã bỏ không thích bình luận");
                    redirectAttributes.addFlashAttribute("toastType", "info");
                }
            } else {
                redirectAttributes.addFlashAttribute("toastMessage", (String) result.get("error"));
                redirectAttributes.addFlashAttribute("toastType", "error");
            }

        } catch (Exception e) {
            logger.error("Error disliking comment {}: {}", commentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra khi dislike comment");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/documents/" + documentId + "#comment-" + commentId;
    }

    @PostMapping("/{documentId}/comments/{commentId}/report")
    public String reportComment(
            @PathVariable Long documentId,
            @PathVariable Long commentId,
            @RequestParam String reason,
            @RequestParam(required = false) String note,
            RedirectAttributes redirectAttributes) {

        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để báo cáo");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/documents/" + documentId;
            }

            // Create report
            Report report = new Report();
            report.setReporter(currentUser);
            Comment comment = new Comment();
            comment.setId(commentId);
            report.setComment(comment);
            report.setType("comment");
            report.setReason(reason);
            report.setNote(note);
            report.setStatus("pending");
            report.setCreatedAt(LocalDateTime.now());

            // Save report
            reportRepository.save(report);

            redirectAttributes.addFlashAttribute("toastMessage",
                    "Cảm ơn bạn đã báo cáo. Chúng tôi sẽ xem xét đánh giá này.");
            redirectAttributes.addFlashAttribute("toastType", "info");
            logger.info("Comment {} reported by user {} for reason: {}", commentId, currentUser.getId(), reason);

        } catch (Exception e) {
            logger.error("Error reporting comment {}: {}", commentId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra khi gửi báo cáo");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }

        return "redirect:/documents/" + documentId + "#comment-" + commentId;
    }

    /**
     * Filter and paginate comments (MVC approach)
     */
    @GetMapping("/{documentId}/filter")
    public String filterComments(
            @PathVariable Long documentId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "2") Integer size,
            Model model) {

        try {
            // Validate parameters
            if (documentId == null || documentId <= 0) {
                return "redirect:/documents/" + documentId;
            }

            if (page < 0)
                page = 0;
            if (size <= 0 || size > 50)
                size = 10; // Limit max size to 50

            // Get filtered comments
            Page<CommentDTO> commentPage = commentService.getFilteredComments(documentId, rating, sortBy, page, size);

            // Get document info
            DocumentDTO document = documentService.getDocumentById(documentId);
            if (document == null) {
                return "redirect:/documents/" + documentId;
            }

            // Add data to model
            model.addAttribute("document", document);
            model.addAttribute("comments", commentPage.getContent());
            model.addAttribute("currentPage", commentPage.getNumber());
            model.addAttribute("totalPages", commentPage.getTotalPages());
            model.addAttribute("totalElements", commentPage.getTotalElements());
            model.addAttribute("hasNext", commentPage.hasNext());
            model.addAttribute("hasPrevious", commentPage.hasPrevious());

            // Add filter parameters
            model.addAttribute("currentRating", rating);
            model.addAttribute("currentSortBy", sortBy);
            model.addAttribute("currentSize", size);

            // Add success message
            String ratingText = rating == null ? "Tất cả" : rating + " sao";
            String sortText = sortBy.equals("recent") ? "Mới nhất"
                    : sortBy.equals("helpful") ? "Hữu ích nhất"
                            : sortBy.equals("high") ? "Đánh giá cao nhất" : "Đánh giá thấp nhất";
            model.addAttribute("toastMessage", "Đã lọc đánh giá: " + ratingText + " - " + sortText);
            model.addAttribute("toastType", "info");

            // Redirect to document details with reviews anchor to scroll to reviews section
            return "redirect:/documents/" + documentId + "?rating=" + (rating != null ? rating : "") +
                    "&sortBy=" + sortBy + "&page=" + page + "&size=" + size + "#reviews";

        } catch (Exception e) {
            logger.error("Error filtering comments for document {}: {}", documentId, e.getMessage(), e);
            return "redirect:/documents/" + documentId;
        }
    }
}