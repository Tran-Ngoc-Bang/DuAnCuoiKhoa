package com.fpoly.shared_learning_materials.controller.client;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
import org.springframework.data.domain.Sort;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.domain.Category;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.CategoryTreeDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.dto.TagDTO;
import com.fpoly.shared_learning_materials.repository.DocumentOwnerRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.CategoryService;
import com.fpoly.shared_learning_materials.service.DocumentNotificationService;
import com.fpoly.shared_learning_materials.service.TagService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for main client pages
 */
@Controller
@RequestMapping("/")
public class ClientPageController {
     @Autowired
    private CategoryService categoryService;
       @Autowired
    private TagService tagService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentOwnerRepository documentOwnerRepository;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private NotificationService  notificationService;


    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "Về chúng tôi");
        return "client/about";
    }

  @GetMapping("/categories")
    public String categories(Model model, @RequestParam(required = false) String category) {
        // Lấy danh sách danh mục gốc (active)
        List<CategoryDTO> rootCategories = categoryService.getActiveRootCategories();
        
        // Danh sách danh mục sẽ hiển thị trong category-grid
        List<CategoryTreeDTO> displayCategories = new ArrayList<>();
        String selectedCategoryName = null;

        // Lấy danh mục con cho từng danh mục gốc để hiển thị trong subcategories
        Map<Long, List<CategoryTreeDTO>> subcategoriesMap = new HashMap<>();
        List<Category> allCategories = categoryService.getAllCategories().stream().map(dto -> {
                Category cat = new Category();
                cat.setId(dto.getId());
                cat.setName(dto.getName());
                cat.setSlug(dto.getSlug());
                cat.setDescription(dto.getDescription());
                cat.setStatus(dto.getStatus());
                cat.setSortOrder(dto.getSortOrder());
                cat.setCreatedAt(dto.getCreatedAt());
                cat.setUpdatedAt(dto.getUpdatedAt());
                cat.setDeletedAt(dto.getDeletedAt());
                return cat;
            }).collect(Collectors.toList());

        for (CategoryDTO rootCategory : rootCategories) {
            subcategoriesMap.put(rootCategory.getId(), categoryService.getSubcategoriesTree(rootCategory.getId()));
        }

        // Lấy danh sách tag nổi bật
        Page<TagDTO> popularTagsPage = tagService.getAllTags(0, 20, "popular");
        List<TagDTO> popularTags = popularTagsPage.getContent();
        // Nếu có selectedCategory, tìm danh mục tương ứng và lấy danh mục con
        if (category != null && !category.isEmpty()) {
            // Tìm danh mục được chọn (có thể là root hoặc child)
            CategoryDTO selectedCategory = categoryService.getAllCategories().stream()
                .filter(cat -> category.equals(cat.getSlug()))
                .findFirst()
                .orElse(null);

            if (selectedCategory != null) {
                selectedCategoryName = selectedCategory.getName();
                // Lấy danh mục con của danh mục được chọn
                displayCategories = categoryService.getSubcategoriesTree(selectedCategory.getId());
                
                // Nếu không có danh mục con, hiển thị chính danh mục được chọn
                if (displayCategories.isEmpty()) {
                    CategoryTreeDTO treeDTO = new CategoryTreeDTO();
                    treeDTO.setId(selectedCategory.getId());
                    treeDTO.setName(selectedCategory.getName());
                    treeDTO.setSlug(selectedCategory.getSlug());
                    treeDTO.setDescription(selectedCategory.getDescription());
                    treeDTO.setDocuments(selectedCategory.getDocuments());
                    treeDTO.setStatus(selectedCategory.getStatus());
                    treeDTO.setCreatedAt(selectedCategory.getCreatedAt());
                    treeDTO.setUpdatedAt(selectedCategory.getUpdatedAt());
                    treeDTO.setDeletedAt(selectedCategory.getDeletedAt());
                    treeDTO.setSubcategories(selectedCategory.getSubcategories());
                    displayCategories.add(treeDTO);
                }

                // Thêm danh mục con của các danh mục con (nếu có)
                for (CategoryTreeDTO child : displayCategories) {
                    subcategoriesMap.put(child.getId(), categoryService.getSubcategoriesTree(child.getId()));
                }
            } else {
                // Nếu không tìm thấy danh mục, hiển thị tất cả danh mục cha
                displayCategories = rootCategories.stream()
                    .map(cat -> {
                        CategoryTreeDTO treeDTO = new CategoryTreeDTO();
                        treeDTO.setId(cat.getId());
                        treeDTO.setName(cat.getName());
                        treeDTO.setSlug(cat.getSlug());
                        treeDTO.setDescription(cat.getDescription());
                        treeDTO.setDocuments(cat.getDocuments());
                        treeDTO.setStatus(cat.getStatus());
                        treeDTO.setCreatedAt(cat.getCreatedAt());
                        treeDTO.setUpdatedAt(cat.getUpdatedAt());
                        treeDTO.setDeletedAt(cat.getDeletedAt());
                        treeDTO.setSubcategories(cat.getSubcategories());
                        return treeDTO;
                    })
                    .collect(Collectors.toList());
            }
        } else {
            // Nếu không có selectedCategory, hiển thị tất cả danh mục cha
            displayCategories = rootCategories.stream()
                .map(cat -> {
                    CategoryTreeDTO treeDTO = new CategoryTreeDTO();
                    treeDTO.setId(cat.getId());
                    treeDTO.setName(cat.getName());
                    treeDTO.setSlug(cat.getSlug());
                    treeDTO.setDescription(cat.getDescription());
                    treeDTO.setDocuments(cat.getDocuments());
                    treeDTO.setStatus(cat.getStatus());
                    treeDTO.setCreatedAt(cat.getCreatedAt());
                    treeDTO.setUpdatedAt(cat.getUpdatedAt());
                    treeDTO.setDeletedAt(cat.getDeletedAt());
                    treeDTO.setSubcategories(cat.getSubcategories());
                    return treeDTO;
                })
                .collect(Collectors.toList());
        }
        // Tính toán thống kê
        long numUsers = userRepository.countByDeletedAtIsNull();
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(1);
        long numNewDocuments = documentRepository.countByCreatedAtBetweenAndDeletedAtIsNull(startOfMonth, endOfMonth);
        Long totalDownloadsObj = documentRepository.getTotalDownloads();
        long numDownloads = totalDownloadsObj != null ? totalDownloadsObj : 0;

        long numContributors = documentOwnerRepository.countDistinctContributors();

        model.addAttribute("pageTitle", "Danh mục tài liệu");
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCategoryName", selectedCategoryName);
        model.addAttribute("rootCategories", rootCategories);
        model.addAttribute("subcategoriesMap", subcategoriesMap);
        model.addAttribute("displayCategories", displayCategories);
        model.addAttribute("popularTags", popularTags);
        model.addAttribute("numUsers", numUsers);
        model.addAttribute("numNewDocuments", numNewDocuments);
        model.addAttribute("numDownloads", numDownloads);
        model.addAttribute("numContributors", numContributors);
        return "client/categories";
    }

    @GetMapping("/search")
    public String search(Model model,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String category) {
        model.addAttribute("pageTitle", "Tìm kiếm tài liệu");
        model.addAttribute("query", q);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);
        model.addAttribute("category", category);
        return "client/search";
    }

    @GetMapping("/upload")
public String upload(Model model, HttpSession session) {
    model.addAttribute("pageTitle", "Đăng tải tài liệu");
    List<CategoryDTO> rootCategories = categoryService.getActiveRootCategories();
    model.addAttribute("rootCategories", rootCategories);
    Page<TagDTO> popularTagsPage = tagService.getAllTags(0, 20, "popular");
    model.addAttribute("popularTags", popularTagsPage.getContent());
    System.out.println("Popular Tags loaded: " + popularTagsPage.getContent().size());
    
    DocumentDTO documentDTO = (DocumentDTO) session.getAttribute("uploadDocumentDTO");
    if (documentDTO == null) {
        documentDTO = new DocumentDTO();
    }
    if (documentDTO.getCategoryIds() == null) {
        documentDTO.setCategoryIds(new ArrayList<>());
    }
    model.addAttribute("documentDTO", documentDTO);
    model.addAttribute("currentStep", 1);
    return "client/upload";
}
   @PostMapping("/upload/step1")
    public String processStep1(
            @RequestParam("file") MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Kiểm tra đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đăng tải tài liệu");
            return "redirect:/upload";
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> currentUser = userRepository.findByUsername(userDetails.getUsername());
        if (!currentUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng");
            return "redirect:/upload";
        }

        // Kiểm tra file
        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file để tải lên");
            return "redirect:/upload";
        }

        // Kiểm tra kích thước file (100MB)
        if (file.getSize() > 100 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("error", "Kích thước file vượt quá 100MB");
            return "redirect:/upload";
        }

        // Kiểm tra định dạng file
        String fileName = file.getOriginalFilename();
        if (fileName != null && !fileName.matches(".*\\.(pdf|doc|docx|ppt|pptx|xls|xlsx)$")) {
            redirectAttributes.addFlashAttribute("error", "Định dạng file không được hỗ trợ. Hỗ trợ: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX");
            return "redirect:/upload";
        }

        // Lưu nội dung file dưới dạng byte array
        byte[] fileContent;
        try {
            fileContent = file.getInputStream().readAllBytes();
            session.setAttribute("uploadFileContent", fileContent);
            session.setAttribute("fileName", fileName);
            session.setAttribute("fileSize", file.getSize());
            session.setAttribute("fileType", fileName.substring(fileName.lastIndexOf(".") + 1).toUpperCase());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đọc file: " + e.getMessage());
            return "redirect:/upload";
        }

        // Lấy hoặc khởi tạo documentDTO
        DocumentDTO documentDTO = (DocumentDTO) session.getAttribute("uploadDocumentDTO");
        if (documentDTO == null) {
            documentDTO = new DocumentDTO();
            documentDTO.setCategoryIds(new ArrayList<>());
            session.setAttribute("uploadDocumentDTO", documentDTO);
        } else if (documentDTO.getCategoryIds() == null) {
            documentDTO.setCategoryIds(new ArrayList<>());
        }

        // Chuyển đến bước 2
        model.addAttribute("pageTitle", "Đăng tải tài liệu");
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
        model.addAttribute("documentDTO", documentDTO);
        model.addAttribute("currentStep", 2);
        return "client/upload";
    }

    @GetMapping("/upload/step2")
public String showStep2(Model model, HttpSession session) {
    DocumentDTO documentDTO = (DocumentDTO) session.getAttribute("uploadDocumentDTO"); // Sửa key session
    if (documentDTO == null) {
        documentDTO = new DocumentDTO();
        documentDTO.setCategoryIds(new ArrayList<>()); // Khởi tạo danh sách rỗng
        session.setAttribute("uploadDocumentDTO", documentDTO);
    } else if (documentDTO.getCategoryIds() == null) {
        documentDTO.setCategoryIds(new ArrayList<>()); // Khởi tạo danh sách rỗng nếu null
    }
    model.addAttribute("documentDTO", documentDTO);
    model.addAttribute("rootCategories", categoryService.getActiveRootCategories()); // Sử dụng getActiveRootCategories cho nhất quán
    model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
    model.addAttribute("currentStep", 2);
    return "client/upload";
}

    @PostMapping("/upload/step2")
public String processStep2(
        @ModelAttribute("documentDTO") DocumentDTO documentDTO,
        @RequestParam(value = "subcategoryId", required = false) Long subcategoryId,
        @RequestParam(value = "tags", required = false) String tags,
        @RequestParam(value = "parentId", required = false) Long parentId,
        @RequestParam(value = "categoryIds", required = false) String categoryIds, // Thêm tham số này
        HttpSession session,
        RedirectAttributes redirectAttributes,
        Model model) {
    // Đảm bảo categoryIds không null
    if (documentDTO.getCategoryIds() == null) {
        documentDTO.setCategoryIds(new ArrayList<>());
    }

    // Log để kiểm tra
    System.out.println("Received parentId: " + parentId);
    System.out.println("Received categoryIds: " + categoryIds);

    // Lưu parentId vào documentDTO
    if (parentId != null && parentId != 0) {
        documentDTO.setParentId(parentId);
    }

    // Xử lý categoryIds (nếu có)
    if (categoryIds != null && !categoryIds.isEmpty()) {
        Arrays.stream(categoryIds.split(","))
              .map(Long::valueOf)
              .forEach(documentDTO.getCategoryIds()::add);
    }

    // Kiểm tra dữ liệu bắt buộc
    if (documentDTO.getTitle() == null || documentDTO.getTitle().trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "Tiêu đề tài liệu là bắt buộc");
        model.addAttribute("currentStep", 2);
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
        model.addAttribute("documentDTO", documentDTO);
        return "client/upload";
    }
    if (documentDTO.getCategoryIds().isEmpty() && (documentDTO.getParentId() == null || documentDTO.getParentId() == 0)) {
        redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một danh mục hoặc danh mục cha");
        model.addAttribute("currentStep", 2);
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
        model.addAttribute("documentDTO", documentDTO);
        return "client/upload";
    }
    if (documentDTO.getDescription() == null || documentDTO.getDescription().trim().isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "Mô tả tài liệu là bắt buộc");
        model.addAttribute("currentStep", 2);
        model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
        model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
        model.addAttribute("documentDTO", documentDTO);
        return "client/upload";
    }

    // Cập nhật dữ liệu
    if (subcategoryId != null) {
        documentDTO.getCategoryIds().add(subcategoryId);
    }
    if (tags != null && !tags.trim().isEmpty()) {
        documentDTO.setTagNames(Arrays.asList(tags.split(",")));
    }

    // Lưu vào session
    session.setAttribute("uploadDocumentDTO", documentDTO);
    System.out.println("Saved documentDTO with parentId: " + documentDTO.getParentId());
    System.out.println("Saved documentDTO with categoryIds: " + documentDTO.getCategoryIds());

    // Chuyển đến bước 3
    model.addAttribute("pageTitle", "Đăng tải tài liệu");
    model.addAttribute("documentDTO", documentDTO);
    model.addAttribute("fileName", session.getAttribute("fileName"));
    model.addAttribute("fileSize", session.getAttribute("fileSize"));
    model.addAttribute("fileType", session.getAttribute("fileType"));
    model.addAttribute("currentStep", 3);
    return "client/upload";
}

    @PostMapping("/upload/step3")
    public String processStep3(
            @RequestParam("accessType") String accessType,
            @RequestParam(value = "price", required = false) Integer price,
            @RequestParam(value = "previewPages", required = false) Integer previewPages,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Kiểm tra user đăng nhập
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails)) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập để đăng tải tài liệu");
            return "redirect:/upload";
        }

        org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        Optional<User> currentUser = userRepository.findByUsername(userDetails.getUsername());
        if (!currentUser.isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng");
            return "redirect:/upload";
        }

        // Lấy dữ liệu từ session
        DocumentDTO documentDTO = (DocumentDTO) session.getAttribute("uploadDocumentDTO");
         byte[] fileContent = (byte[]) session.getAttribute("uploadFileContent");
      String fileName = (String) session.getAttribute("fileName");
      if (documentDTO == null || fileContent == null || fileName == null) {
          redirectAttributes.addFlashAttribute("error", "Dữ liệu tài liệu hoặc file không tồn tại");
          return "redirect:/upload";
      }
        //  MultipartFile file = new MockMultipartFile(fileName, fileName, null, fileContent);
        // MultipartFile file = (MultipartFile) session.getAttribute("uploadFile");
        // if (documentDTO == null || file == null) {
        //     redirectAttributes.addFlashAttribute("error", "Dữ liệu tài liệu hoặc file không tồn tại");
        //     return "redirect:/upload";
        // }
        
  System.out.println("DocumentDTO: " + documentDTO);
      System.out.println("File: " + fileName);
      System.out.println("CategoryIds: " + documentDTO.getCategoryIds());
      System.out.println("TagNames: " + documentDTO.getTagNames());
      System.out.println("UserId: " + currentUser.get().getId());
      System.out.println("File size: " + fileContent.length);
      documentDTO.setVisibility(accessType.equals("premium") ? "premium" : "public");
      if (accessType.equals("premium") && price != null) {
          documentDTO.setPrice(new BigDecimal(price));
      } else {
          documentDTO.setPrice(BigDecimal.ZERO);
      }
      documentDTO.setStatus("PENDING");

        try {
          // Gọi service để lưu tài liệu
          DocumentDTO createdDocument = documentService.createDocument(
              documentDTO, fileContent, fileName, documentDTO.getCategoryIds(), documentDTO.getTagNames(), currentUser.get().getId());
             // Tạo notification cho admin khi có document mới
            try {
                String uploaderName = "Admin"; // Default name
                if (currentUser.get().getId() != null) {
                    User uploader = userRepository.findById(currentUser.get().getId()).orElse(null);
                    if (uploader != null) {
                        uploaderName = uploader.getFullName() != null ? uploader.getFullName() : uploader.getUsername();
                        notificationService.createNotification(
                    uploader, // null for system notification
                    "Tài liệu mới được đăng tải",
                    String.format("%s đã đăng tải tài liệu: %s", uploaderName, createdDocument.getTitle()),
                    "document"
                );
                    }
                }
                
                System.out.println("Created notification for new document: " + documentDTO.getTitle());
            } catch (Exception e) {
                System.err.println("Error creating notification for new document: " + e.getMessage());
            }
          // Lưu ID tài liệu để hiển thị ở bước 4
          session.setAttribute("documentId", createdDocument.getId());

          // Xóa dữ liệu tạm
          session.removeAttribute("uploadDocumentDTO");
          session.removeAttribute("uploadFileContent");
          session.removeAttribute("fileName");
          session.removeAttribute("fileSize");
          session.removeAttribute("fileType");

          // Chuyển đến bước 4
          model.addAttribute("pageTitle", "Đăng tải tài liệu");
          model.addAttribute("documentId", session.getAttribute("documentId"));
          model.addAttribute("currentStep", 4);
          model.addAttribute("documentDTO", documentDTO);
          return "client/upload";
      } catch (Exception e) {
          redirectAttributes.addFlashAttribute("error", "Lỗi khi xuất bản tài liệu: " + e.getMessage());
          return "redirect:/upload";
      }
    }

    @PostMapping("/upload/reset")
    public String resetUpload(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("uploadDocumentDTO");
        session.removeAttribute("uploadFile");
        session.removeAttribute("fileName");
        session.removeAttribute("fileSize");
        session.removeAttribute("fileType");
        session.removeAttribute("documentId");
        redirectAttributes.addFlashAttribute("success", "Đã reset, bạn có thể tải lên tài liệu mới");
        return "redirect:/upload";
    }
    @GetMapping("/upload/step/{step}/prev")
public String prevStep(@PathVariable int step, Model model, HttpSession session) {
    int prevStep = Math.max(1, step - 1);
    DocumentDTO documentDTO = (DocumentDTO) session.getAttribute("uploadDocumentDTO");
    if (documentDTO == null) {
        documentDTO = new DocumentDTO();
        documentDTO.setCategoryIds(new ArrayList<>());
        session.setAttribute("uploadDocumentDTO", documentDTO);
    } else if (documentDTO.getCategoryIds() == null) {
        documentDTO.setCategoryIds(new ArrayList<>());
    }
    model.addAttribute("documentDTO", documentDTO);
    model.addAttribute("rootCategories", categoryService.getActiveRootCategories());
    model.addAttribute("popularTags", tagService.getAllTags(0, 20, "popular").getContent());
    model.addAttribute("fileName", session.getAttribute("fileName"));
    model.addAttribute("fileSize", session.getAttribute("fileSize"));
    model.addAttribute("fileType", session.getAttribute("fileType"));
    model.addAttribute("currentStep", prevStep);
    return "client/upload";
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
@GetMapping("/tags/search")
    @ResponseBody
    public List<TagDTO> searchTags(@RequestParam("query") String query) {
        // Tìm tag dựa trên query (ví dụ: tìm theo tên tag)
        Page<TagDTO> tagsPage = tagService.getAllTags(0, 10, "popular"); // Giới hạn 10 kết quả
        return tagsPage.getContent().stream()
                .filter(tag -> tag.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}