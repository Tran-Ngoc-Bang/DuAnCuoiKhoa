package com.fpoly.shared_learning_materials.controller.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.service.CategoryService;
import com.fpoly.shared_learning_materials.service.DocumentNotificationService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.service.TagService;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.domain.Tag;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.dto.TagDTO;
import com.fpoly.shared_learning_materials.repository.TagRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/documents")
public class DocumentController extends BaseAdminController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DocumentNotificationService documentNotificationService;
    @Autowired
    private TagService tagService;
    @Autowired
    private TagRepository tagRepository;
    
    public DocumentController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }

    @PostConstruct
    public void checkUploadDir() {
        String projectDir = System.getProperty("user.dir");
        String uploadDir = projectDir + "/src/main/resources/static/uploads/documents";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists())
            dir.mkdirs();
        System.out.println("Upload dir: " + uploadDir + ", Writable: " + dir.canWrite());
    }

    @GetMapping
    public String listDocuments(
            Model model,
            HttpServletRequest request,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo,
            @RequestParam(value = "fileSize", required = false) String fileSize,
            @RequestParam(value = "views", required = false) String views,
            @RequestParam(value = "sortBy", required = false, defaultValue = "created_at") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "desc") String sortDir,
            @RequestParam(value = "tab", required = false, defaultValue = "active") String tab) {

        // Create custom Pageable with sort parameters
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Set default sort field based on tab
        String effectiveSortBy = sortBy;
        if ("deleted".equals(tab) && "created_at".equals(sortBy)) {
            // For deleted tab, default sort by deletedAt instead of createdAt
            effectiveSortBy = "deleted_at";
            System.out.println("Deleted tab: Using deletedAt sort instead of createdAt");
        }

        // For author sort, we need to use the original sortBy to trigger in-memory
        // filtering
        String sortFieldForPageable = "author".equalsIgnoreCase(effectiveSortBy) ? "author" : mapSortField(effectiveSortBy);
        Sort sort = Sort.by(direction, sortFieldForPageable);
        Pageable customPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<DocumentDTO> documentPage;
        DocumentService.FilteredStatistics stats;

        if ("deleted".equals(tab)) {
            // Hiển thị documents đã xóa với tất cả filters
            documentPage = documentService.getFilteredDeletedDocuments(
                    customPageable, search, status, categoryId, type, price, author, tags, dateFrom, dateTo, fileSize,
                    views);
            System.out.println("DELETED TAB - Found " + documentPage.getTotalElements() + " deleted documents");
            System.out.println("DELETED TAB - Current page content: " + documentPage.getContent().size());
            // Thống kê TỔNG tất cả documents (giống tab active)
            stats = documentService.getFilteredStatistics(
                    search, status, categoryId, type, price, author, tags, dateFrom, dateTo, fileSize, views);
        } else {
            // Hiển thị documents active (chưa xóa)
            documentPage = documentService.getFilteredDocuments(
                    customPageable, search, status, categoryId, type, price, author, tags, dateFrom, dateTo,fileSize,
                    views);

            // Lấy thống kê từ kết quả đã lọc
            stats = documentService.getFilteredStatistics(
                    search, status, categoryId, type, price, author, tags, dateFrom, dateTo, fileSize, views);
        }

        System.out.println("=== DOCUMENT LIST DEBUG ===");
        System.out.println("Tab: " + tab);
        System.out.println("CurrentTab will be set to: " + tab);
        System.out.println("Page: " + pageable.getPageNumber() + ", Size: " + pageable.getPageSize() + ", Total: "
                + documentPage.getTotalElements());

        // Debug: Count total documents in database
        long totalInDb = documentService.countAllDocuments();
        long activeInDb = documentService.countActiveDocuments();
        long deletedInDb = documentService.countDeletedDocuments();
        System.out.println("DB Stats - Total: " + totalInDb + ", Active: " + activeInDb + ", Deleted: " + deletedInDb);
        System.out.println("===========================");

        model.addAttribute("documents", documentPage.getContent());
        model.addAttribute("page", documentPage);
        model.addAttribute("totalDocuments", stats.getTotalDocuments());
        model.addAttribute("totalViews", stats.getTotalViews());
        model.addAttribute("totalDownloads", stats.getTotalDownloads());
        model.addAttribute("pendingDocuments", stats.getPendingDocuments());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("currentTab", tab);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
      

        // Handle export request
        if ("true".equals(request.getParameter("export"))) {
            List<DocumentDTO> allDocuments = documentService.getAllDocumentsForExport();
            model.addAttribute("documents", allDocuments);
        }

        // For sidebar active menu
        model.addAttribute("currentPage", "documents");

        return "admin/documents/list";
    }

    @GetMapping("/export-data")
    @ResponseBody
    public List<DocumentDTO> exportDocumentsData() {
        return documentService.getAllDocumentsForExport();
    }
    @GetMapping("/tags/search")
    @ResponseBody
    public List<TagDTO> searchTags(@RequestParam("query") String query) {
        // Tìm tag dựa trên query (ví dụ: tìm theo tên tag)
        Page<TagDTO> tagsPage = tagService.getAllTags(0, 10, "popular"); // Giới hạn 10 kết quả
        return tagsPage.getContent().stream()
                .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
    

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("document")) {
            model.addAttribute("document", new DocumentDTO());
           
        }
         DocumentDTO documentDTO = new DocumentDTO();
          documentDTO.setParentId(0L);
          model.addAttribute("documentDTO", documentDTO);
             model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        if (!model.containsAttribute("categories")) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("currentPage", "documents");
        }
        return "admin/documents/create";
    }

@PostMapping
public String createDocument(
        @ModelAttribute("document") @Valid DocumentDTO documentDTO,
        BindingResult result,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "categoryIdsString", required = false) String categoryIdsString,
        @RequestParam(value = "tagNames", required = false) String tagNames,
        RedirectAttributes redirectAttributes,
        Model model) {

    // Log basic info for debugging
    System.out.println("Creating document: " + documentDTO.getTitle() + ", categories: " + categoryIdsString);
    System.out.println("==============================");

    if (result.hasErrors()) {
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        return "admin/documents/create";
    }

    // Parse categoryIds from string
    List<Long> categoryIds = new ArrayList<>();
    if (categoryIdsString != null && !categoryIdsString.trim().isEmpty()) {
        try {
            String[] idStrings = categoryIdsString.split(",");
            for (String idString : idStrings) {
                if (!idString.trim().isEmpty()) {
                    categoryIds.add(Long.parseLong(idString.trim()));
                }
            }
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Danh mục không hợp lệ");
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/create";
        }
    }

    // Bắt buộc chọn ít nhất 1 danh mục để phân loại tài liệu
    if (categoryIds.isEmpty() && (documentDTO.getParentId() == null || documentDTO.getParentId() == 0)) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một danh mục để phân loại tài liệu");
        redirectAttributes.addFlashAttribute("document", documentDTO);
        redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/documents/create";
    }

    if (categoryIds.size() > 15) {
        redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể chọn tối đa 15 danh mục");
        redirectAttributes.addFlashAttribute("document", documentDTO);
        redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/documents/create";
    }

    // Validate categoryIds are valid numbers
    for (Long categoryId : categoryIds) {
        if (categoryId == null || categoryId <= 0) {
            redirectAttributes.addFlashAttribute("error", "Danh mục không hợp lệ");
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/create";
        }
    }

    if (file == null || file.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file tài liệu");
        redirectAttributes.addFlashAttribute("document", documentDTO);
        redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/documents/create";
    }

    try {
        List<String> tagList = tagNames != null && !tagNames.trim().isEmpty()
                ? Arrays.asList(tagNames.split("\\s*,\\s*"))
                : new ArrayList<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new IllegalStateException(
                                "Không tìm thấy người dùng với username: " + username));
                userId = user.getId();
            }
        }
        documentDTO.setUserId(userId);

        // Gọi service để tạo document
        DocumentDTO createdDocument = documentService.createDocument(documentDTO, file, categoryIds, tagList, userId);

        // Tạo notification cho admin khi có document mới
        try {
            String uploaderName = "Admin"; // Default name
            if (userId != null) {
                User uploader = userRepository.findById(userId).orElse(null);
                if (uploader != null) {
                    uploaderName = uploader.getFullName() != null ? uploader.getFullName() : uploader.getUsername();
                }
            }
            notificationService.notifyAdminsNewDocument(documentDTO.getTitle(), uploaderName);
            System.out.println("Created notification for new document: " + documentDTO.getTitle());
        } catch (Exception e) {
            System.err.println("Error creating notification for new document: " + e.getMessage());
        }

        // Gửi email thông báo tài liệu mới cho users (nếu bật chế độ immediate)
        try {
            com.fpoly.shared_learning_materials.domain.Document newDocument = 
                documentService.findById(createdDocument.getId());
            if (newDocument != null) {
                documentNotificationService.sendImmediateNotification(newDocument);
                System.out.println("Sent immediate document notification for: " + documentDTO.getTitle());
            }
        } catch (Exception e) {
            System.err.println("Error sending immediate document notification: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("successMessage", "Thêm tài liệu thành công");
        return "redirect:/admin/documents";
    } catch (Exception e) {
        System.err.println("Error creating document: " + e.getMessage());
        e.printStackTrace();
        model.addAttribute("error", "Lỗi: " + e.getMessage());
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("allCategories", categoryService.getAllCategories());
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        redirectAttributes.addFlashAttribute("document", documentDTO);
        redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
        return "redirect:/admin/documents/create";
    }
}
   @GetMapping("/categories/{parentId}/subcategories")
    @ResponseBody
    public List<CategoryDTO> getSubcategories(@PathVariable Long parentId) {
        if (parentId == 0) {
            return categoryService.getActiveRootCategories();
        }
        // Lấy danh mục con dựa trên parentId
        return categoryService.getAllCategories().stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parentId))
                .collect(Collectors.toList());
    }
    @GetMapping("/categories/{id}/path")
    @ResponseBody
    public List<CategoryDTO> getCategoryPath(@PathVariable Long id) {
        List<CategoryDTO> path = new ArrayList<>();
        CategoryDTO current = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        while (current != null) {
            path.add(0, current);
            Long parentId = current.getParentId();
            current = parentId != null 
                    ? categoryService.getAllCategories().stream()
                        .filter(c -> c.getId().equals(parentId))
                        .findFirst()
                        .orElse(null)
                    : null;
        }
        
        return path;
    }

    @GetMapping("/tags")
    public ResponseEntity<List<TagDTO>> getTags(@RequestParam(required = false) String q) {
        List<Tag> tags;
        if (q != null && !q.trim().isEmpty()) {
            tags = tagRepository.findByNameContainingIgnoreCase(q);
        } else {
            tags = tagRepository.findAll();
        }

        List<TagDTO> tagDTOs = tags.stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(tagDTOs);
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        DocumentDTO document = documentService.getDocumentById(id);
        if (document == null) {
            System.out.println("Document not found for id: " + id);
            return "redirect:/admin/documents";
        }
        System.out.println("Document: id=" + document.getId() + ", title=" + document.getTitle() + ", tagNames="
                + document.getTagNames() + ", fileName=" + document.getFileName() + ", fileSize="
                + document.getFileSize());
        System.out.println("Document categoryIds: " + document.getCategoryIds());
        System.out.println("Document categoryNames: " + document.getCategoryNames());
        model.addAttribute("document", document);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("rootCategories", categoryService.getRootCategories());
        model.addAttribute("currentPage", "documents");
        return "admin/documents/edit";
    }

    @PostMapping("/{id}")
    public String updateDocument(
            @PathVariable Long id,
            @ModelAttribute("document") @Valid DocumentDTO documentDTO,
            BindingResult result,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "categoryIdsString", required = false) String categoryIdsString,
            @RequestParam(value = "tagNames", required = false) String tagNames,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== UPDATE DOCUMENT REQUEST ===");
        System.out.println("ID: " + id);
        System.out.println("Title: " + documentDTO.getTitle());
        System.out.println("File: "
                + (file != null && !file.isEmpty() ? file.getOriginalFilename() + " (" + file.getSize() / 1024 + " KB)"
                        : "null"));
        System.out.println("CategoryIdsString: " + categoryIdsString);
        System.out.println("TagNames: " + tagNames);
        System.out.println("==============================");

        // Parse categoryIds from string
        List<Long> categoryIds = new ArrayList<>();
        if (categoryIdsString != null && !categoryIdsString.trim().isEmpty()) {
            try {
                String[] idStrings = categoryIdsString.split(",");
                for (String idString : idStrings) {
                    if (!idString.trim().isEmpty()) {
                        categoryIds.add(Long.parseLong(idString.trim()));
                    }
                }
            } catch (NumberFormatException e) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không hợp lệ");
                redirectAttributes.addFlashAttribute("document", documentDTO);
                redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
                return "redirect:/admin/documents/" + id + "/edit";
            }
        }

        System.out.println("Parsed CategoryIds: " + categoryIds + " (size: " + categoryIds.size() + ")");

        // Bắt buộc chọn ít nhất 1 danh mục để phân loại tài liệu
        if (categoryIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một danh mục để phân loại tài liệu");
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/" + id + "/edit";
        }

        if (categoryIds.size() > 15) {
            redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể chọn tối đa 15 danh mục");
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/" + id + "/edit";
        }

        // Validate categoryIds are valid numbers
        for (Long categoryId : categoryIds) {
            if (categoryId == null || categoryId <= 0) {
                redirectAttributes.addFlashAttribute("error", "Danh mục không hợp lệ");
                redirectAttributes.addFlashAttribute("document", documentDTO);
                redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
                return "redirect:/admin/documents/" + id + "/edit";
            }
        }

        if (result.hasErrors()) {
            String errors = result.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            System.out.println("Validation errors: " + errors);
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại: " + errors);
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/" + id + "/edit";
        }

        try {
            List<String> tagList = tagNames != null && !tagNames.trim().isEmpty()
                    ? Arrays.asList(tagNames.split("\\s*,\\s*"))
                    : new ArrayList<>();
            documentDTO.setId(id);
            documentDTO.setCategoryIds(categoryIds); // Gán categoryIds vào DTO

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long userId = null;

            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    String username = ((UserDetails) principal).getUsername();
                    User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Không tìm thấy người dùng với username: " + username));
                    userId = user.getId();
                }
            }

            documentDTO.setUserId(userId);
            DocumentDTO updatedDocument = documentService.updateDocument(id, documentDTO, file, categoryIds, tagList);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài liệu thành công");
            return "redirect:/admin/documents";
        } catch (Exception e) {
            System.err.println("Error updating document: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("document", documentDTO);
            redirectAttributes.addFlashAttribute("categories", categoryService.getAllCategories());
            return "redirect:/admin/documents/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/details")
    public String showDetails(@PathVariable Long id, Model model) {
        DocumentDTO document = documentService.getDocumentById(id);
        if (document == null) {
            System.out.println("Document not found for id: " + id);
            model.addAttribute("error", "Tài liệu không tồn tại");
            return "redirect:/admin/documents";
        }
        System.out.println("Showing details for document: id=" + document.getId());
        System.out.println(
                "Document has " + (document.getComments() != null ? document.getComments().size() : 0) + " comments");
        model.addAttribute("document", document);
        model.addAttribute("currentPage", "documents");
        return "admin/documents/details";
    }

    @GetMapping("/{id}/delete")
    public String showDeleteForm(@PathVariable Long id, Model model) {
        DocumentDTO document = documentService.getDocumentById(id);
        if (document == null) {
            System.out.println("Document not found for id: " + id);
            model.addAttribute("error", "Tài liệu không tồn tại");
            return "redirect:/admin/documents";
        }
        System.out.println("Showing delete form for document: id=" + document.getId());
        model.addAttribute("document", document);
        model.addAttribute("currentPage", "documents");
        return "admin/documents/delete";
    }

    @PostMapping("/{id}/delete")
    public String deleteDocument(
            @PathVariable Long id,
            @RequestParam("reason") String reason,
            @RequestParam(value = "notifyAuthor", required = false) boolean notifyAuthor,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== DELETE DOCUMENT REQUEST ===");
        System.out.println("ID: " + id);
        System.out.println("Reason: " + reason);
        System.out.println("Notify Author: " + notifyAuthor);
        System.out.println("==============================");

        if (reason == null || reason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng nhập lý do xóa tài liệu");
            return "redirect:/admin/documents/" + id + "/delete";
        }

        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
                return "redirect:/admin/documents";
            }

            // Get current user info for logging
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String deletedBy = "System";
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    deletedBy = ((UserDetails) principal).getUsername();
                }
            }

            // Log deletion activity
            System.out
                    .println("Document '" + document.getTitle() + "' deleted by: " + deletedBy + ", reason: " + reason);

            // Delete the document
            documentService.deleteDocument(id);

            // TODO: If notifyAuthor is true, send notification to document author
            if (notifyAuthor) {
                System.out.println("TODO: Send notification to author: " + document.getAuthorName());
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã chuyển tài liệu '" + document.getTitle() + "' vào thùng rác");
            return "redirect:/admin/documents";
        } catch (Exception e) {
            System.err.println("Error deleting document: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa tài liệu: " + e.getMessage());
            return "redirect:/admin/documents/" + id + "/delete";
        }
    }

    @PostMapping("/{id}/restore")
    public String restoreDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
                return "redirect:/admin/documents?tab=deleted";
            }

            documentService.restoreDocument(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã khôi phục tài liệu '" + document.getTitle() + "' thành công");
            return "redirect:/admin/documents?tab=deleted";
        } catch (Exception e) {
            System.err.println("Error restoring document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi khôi phục tài liệu: " + e.getMessage());
            return "redirect:/admin/documents?tab=deleted";
        }
    }

    @PostMapping("/{id}/permanent-delete")
    public String permanentDeleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
                return "redirect:/admin/documents?tab=deleted";
            }

            // Kiểm tra xem tài liệu đã bị xóa mềm chưa
            if (document.getDeletedAt() == null) {
                redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa vĩnh viễn tài liệu đã bị xóa mềm");
                return "redirect:/admin/documents";
            }

            String documentTitle = document.getTitle();
            documentService.permanentDeleteDocument(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã xóa vĩnh viễn tài liệu '" + documentTitle + "' thành công");
            return "redirect:/admin/documents?tab=deleted";
        } catch (Exception e) {
            System.err.println("Error permanently deleting document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa vĩnh viễn tài liệu: " + e.getMessage());
            return "redirect:/admin/documents?tab=deleted";
        }
    }

    @GetMapping("/{id}/archive")
    public String archiveDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
                return "redirect:/admin/documents";
            }

            // Set status to ARCHIVED
            documentService.updateDocumentStatus(id, "ARCHIVED");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã lưu trữ tài liệu '" + document.getTitle() + "' thành công");
            return "redirect:/admin/documents";
        } catch (Exception e) {
            System.err.println("Error archiving document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu trữ tài liệu: " + e.getMessage());
            return "redirect:/admin/documents";
        }
    }

    @GetMapping("/{id}/hide")
    public String hideDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            DocumentDTO document = documentService.getDocumentById(id);
            if (document == null) {
                redirectAttributes.addFlashAttribute("error", "Tài liệu không tồn tại");
                return "redirect:/admin/documents";
            }

            // Set status to SUSPENDED
            documentService.updateDocumentStatus(id, "SUSPENDED");

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã tạm ngưng tài liệu '" + document.getTitle() + "' thành công");
            return "redirect:/admin/documents";
        } catch (Exception e) {
            System.err.println("Error hiding document: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi ẩn tài liệu: " + e.getMessage());
            return "redirect:/admin/documents";
        }
    }

    // Helper method to map sort field names from URL to entity field names
    private String mapSortField(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "views":
                return "viewsCount";
            case "downloads":
                return "downloadsCount";
            case "created_at":
                return "createdAt";
            case "deleted_at":
                return "deletedAt";
            case "author":
                // Author sorting cần xử lý đặc biệt vì không có direct field
                // Sẽ fallback về createdAt và xử lý trong service
                return "createdAt";
            case "title":
            case "status":
            case "price":
            default:
                return sortBy;
        }
    }
}