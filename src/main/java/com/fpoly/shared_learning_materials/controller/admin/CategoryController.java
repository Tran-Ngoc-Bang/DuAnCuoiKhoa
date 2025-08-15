package com.fpoly.shared_learning_materials.controller.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fpoly.shared_learning_materials.service.CategoryService;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;

import com.fpoly.shared_learning_materials.domain.CategoryHierarchy;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.repository.CategoryHierarchyRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryHierarchyRepository categoryHierarchyRepository;
    // @Autowired
    // private UserService userService;

    @GetMapping
    public String getCategories(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "none") String sort,
            @RequestParam(defaultValue = "active") String tab) {
        System.out.println("Request: page=" + page + ", size=" + size + ", filter=" + filter + ", status=" + status
                + ", sort=" + sort + ", tab=" + tab);

        List<CategoryDTO> allCategories;

        // Lấy danh mục theo tab
        switch (tab) {
            case "deleted":
                allCategories = categoryService.getDeletedCategories();
                System.out.println("Controller: Retrieved " + allCategories.size() + " deleted categories");
                break;
            case "all":
                allCategories = categoryService.getAllCategories();
                break;
            default: // "active"
                allCategories = categoryService.getActiveCategories();
                break;
        }

        // Hiển thị tất cả danh mục với cấu trúc phân cấp cho active/all tab
        // Nhưng với deleted tab, hiển thị flat list để tránh missing items
        List<CategoryDTO> displayCategories;
        if ("deleted".equals(tab)) {
            // Deleted tab: hiển thị flat list, không hierarchy
            displayCategories = allCategories;
            System.out.println("Deleted tab: showing " + displayCategories.size() + " categories as flat list");
        } else {
            // Active/All tab: hiển thị với hierarchy
            displayCategories = buildHierarchicalStructure(allCategories);
        }

        if (!filter.isEmpty()) {
            String searchTerm = filter.toLowerCase();
            displayCategories = displayCategories.stream()
                    .filter(cat -> cat.getName().toLowerCase().contains(searchTerm) ||
                            (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(searchTerm)))
                    .collect(Collectors.toList());
        }

        // Chỉ filter theo status khi ở tab "all"
        if ("all".equals(tab) && !status.equals("all")) {
            displayCategories = displayCategories.stream()
                    .filter(cat -> cat.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        switch (sort) {
            case "name_asc":
                displayCategories.sort(Comparator.comparing(CategoryDTO::getName));
                break;
            case "name_desc":
                displayCategories.sort(Comparator.comparing(CategoryDTO::getName).reversed());
                break;
            case "date_newest":
                displayCategories.sort(Comparator.comparing(
                        cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "date_oldest":
                displayCategories.sort(Comparator.comparing(
                        cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(),
                        Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "count_most":
                displayCategories.sort(Comparator.comparingInt(CategoryDTO::getDocuments).reversed());
                break;
            case "count_least":
                displayCategories.sort(Comparator.comparingInt(CategoryDTO::getDocuments));
                break;
            default:
                // Mặc định sắp xếp theo thời gian tạo mới nhất
                displayCategories.sort(Comparator.comparing(
                        cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(),
                        Comparator.nullsLast(Comparator.reverseOrder())));
                break;
        }

        int totalItems = displayCategories.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        page = Math.max(1, page);
        page = Math.min(page, totalPages == 0 ? 1 : totalPages);

        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);
        List<CategoryDTO> pagedCategories = totalItems > 0 ? displayCategories.subList(start, end) : List.of();

        System.out.println("Categories size: " + totalItems + ", totalPages: " + totalPages + ", page: " + page);

        // Thống kê cho các tab
        int activeCount = categoryService.getActiveCategories().size();
        int deletedCount = categoryService.getDeletedCategories().size();
        int totalCount = categoryService.getAllCategories().size();

        model.addAttribute("categories", pagedCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("filter", filter);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        model.addAttribute("tab", tab);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("deletedCount", deletedCount);
        model.addAttribute("totalCount", totalCount);
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/create";
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/create";
        }

        try {
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
                } else {
                    model.addAttribute("errorMessage", "Không thể xác định thông tin người dùng đã đăng nhập");
                    return "admin/categories/create";
                }
            } else {
                model.addAttribute("errorMessage", "Bạn cần đăng nhập để tạo danh mục");
                return "admin/categories/create";
            }
            categoryDTO.setCreatedById(userId);
            categoryService.createCategory(categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được tạo thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Set<Long> descendantIds = getDescendantIds(id);

        List<CategoryDTO> availableCategories = categoryService.getAllCategories().stream()
                .filter(c -> !c.getId().equals(id) && !descendantIds.contains(c.getId()))
                .collect(Collectors.toList());
        model.addAttribute("category", category);
        model.addAttribute("categories", availableCategories);
        return "admin/categories/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id, @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/edit";
        }

        try {
            System.out.println("Updating category ID: " + id + ", New parentId: " + categoryDTO.getParentId());
            categoryService.updateCategory(id, categoryDTO);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được cập nhật thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            System.out.println("Error updating category ID: " + id + ", Error: " + e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/edit";
        }
    }

    @GetMapping("/details/{id}")
    public String showDetails(@PathVariable Long id, Model model) {
        CategoryDTO category = categoryService.getAllCategories().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        model.addAttribute("category", category);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("categoryHierarchyRepository", categoryHierarchyRepository);
        return "admin/categories/details";
    }

    // MVC Delete methods
    @GetMapping("/{id}/delete")
    public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO category = categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            model.addAttribute("category", category);
            return "admin/categories/delete";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO category = categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            // Log lý do xóa nếu có
            if (reason != null && !reason.trim().isEmpty()) {
                System.out.println("Deleting category: " + category.getName() + " - Reason: " + reason);
            }

            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success",
                    "Danh mục '" + category.getName() + "' đã được xóa thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO category = categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            // Toggle status
            String newStatus = "active".equals(category.getStatus()) ? "inactive" : "active";
            category.setStatus(newStatus);
            categoryService.updateCategory(id, category);

            String statusText = "active".equals(newStatus) ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("success",
                    "Đã " + statusText + " danh mục '" + category.getName() + "' thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    @PostMapping("/{id}/restore")
    public String restoreCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== CONTROLLER RESTORE DEBUG ===");
            System.out.println("Restore request for category ID: " + id);
            
            CategoryDTO category = categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            System.out.println("Found category: " + category.getName());
            
            categoryService.restoreCategory(id);
            redirectAttributes.addFlashAttribute("success",
                    "Danh mục '" + category.getName() + "' đã được khôi phục thành công");
            return "redirect:/admin/categories?tab=active";
        } catch (Exception e) {
            System.err.println("Error in restore controller: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi khôi phục danh mục: " + e.getMessage());
            return "redirect:/admin/categories?tab=deleted";
        }
    }

    @PostMapping("/{id}/permanent-delete")
    public String permanentDeleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            CategoryDTO category = categoryService.getAllCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

            categoryService.permanentDeleteCategory(id);
            redirectAttributes.addFlashAttribute("success",
                    "Danh mục '" + category.getName() + "' đã được xóa vĩnh viễn");
            return "redirect:/admin/categories?tab=deleted";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa vĩnh viễn danh mục: " + e.getMessage());
            return "redirect:/admin/categories?tab=deleted";
        }
    }

    @PostMapping("/delete-multiple")
    public String deleteMultipleCategories(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            String[] categoryIdStrings = request.getParameterValues("categoryIds");
            System.out.println("Raw parameters: " + java.util.Arrays.toString(categoryIdStrings));

            if (categoryIdStrings == null || categoryIdStrings.length == 0) {
                redirectAttributes.addFlashAttribute("error", "Chưa chọn danh mục nào để xóa");
                return "redirect:/admin/categories";
            }

            List<Long> categoryIds = new java.util.ArrayList<>();
            for (String idStr : categoryIdStrings) {
                try {
                    categoryIds.add(Long.parseLong(idStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid category ID: " + idStr);
                }
            }

            System.out.println("Parsed categoryIds: " + categoryIds);

            if (categoryIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không có danh mục hợp lệ để xóa");
                return "redirect:/admin/categories";
            }

            categoryService.deleteCategories(categoryIds);
            redirectAttributes.addFlashAttribute("success",
                    "Đã xóa thành công " + categoryIds.size() + " danh mục");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            System.err.println("Error deleting categories: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    private Set<Long> getDescendantIds(Long categoryId) {
        Set<Long> descendantIds = new HashSet<>();
        collectDescendantIds(categoryId, descendantIds);
        return descendantIds;
    }

    private void collectDescendantIds(Long categoryId, Set<Long> descendantIds) {
        List<CategoryHierarchy> children = categoryHierarchyRepository.findByIdParentId(categoryId);
        for (CategoryHierarchy child : children) {
            Long childId = child.getId().getChildId();
            if (!descendantIds.contains(childId)) {
                descendantIds.add(childId);
                collectDescendantIds(childId, descendantIds);
            }
        }
    }

    private List<CategoryDTO> buildHierarchicalStructure(List<CategoryDTO> allCategories) {
        List<CategoryDTO> result = new java.util.ArrayList<>();

        // Tìm các danh mục gốc (không có parent)
        List<CategoryDTO> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParentId() == null)
                .collect(Collectors.toList());

        // Thêm các danh mục gốc vào kết quả
        for (CategoryDTO root : rootCategories) {
            result.add(root);
            // Thêm các danh mục con với indent
            addChildrenWithIndent(root.getId(), allCategories, result, 1);
        }

        return result;
    }

    private void addChildrenWithIndent(Long parentId, List<CategoryDTO> allCategories,
            List<CategoryDTO> result, int level) {
        List<CategoryDTO> children = allCategories.stream()
                .filter(cat -> parentId.equals(cat.getParentId()))
                .collect(Collectors.toList());

        for (CategoryDTO child : children) {
            // Tạo prefix để hiển thị cấp độ
            String prefix = "├─ ".repeat(level);
            CategoryDTO childWithIndent = new CategoryDTO();
            // Copy tất cả thuộc tính
            childWithIndent.setId(child.getId());
            childWithIndent.setName(prefix + child.getName());
            childWithIndent.setDescription(child.getDescription());
            childWithIndent.setStatus(child.getStatus());
            childWithIndent.setParentId(child.getParentId());
            childWithIndent.setCreatedAt(child.getCreatedAt());
            childWithIndent.setUpdatedAt(child.getUpdatedAt());
            childWithIndent.setDeletedAt(child.getDeletedAt()); // Quan trọng: copy deletedAt
            childWithIndent.setCreatedById(child.getCreatedById());
            childWithIndent.setDocuments(child.getDocuments());
            childWithIndent.setSubcategories(child.getSubcategories());
            childWithIndent.setHierarchyCreatedAt(child.getHierarchyCreatedAt());
            childWithIndent.setLevel(level); // Thêm thuộc tính level nếu có
            childWithIndent.setParentName(child.getParentName()); // Copy parent name

            result.add(childWithIndent);
            // Đệ quy thêm các danh mục con của danh mục này
            addChildrenWithIndent(child.getId(), allCategories, result, level + 1);
        }
    }

    // Bulk restore deleted categories
    @PostMapping("/restore-multiple")
    public String restoreMultipleCategories(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        try {
            String[] categoryIdStrings = request.getParameterValues("categoryIds");
            System.out.println("=== BULK RESTORE CATEGORIES ===");
            System.out.println("Raw parameters: " + java.util.Arrays.toString(categoryIdStrings));

            if (categoryIdStrings == null || categoryIdStrings.length == 0) {
                redirectAttributes.addFlashAttribute("error", "Chưa chọn danh mục nào để khôi phục");
                return "redirect:/admin/categories?tab=deleted";
            }

            List<Long> categoryIds = new ArrayList<>();
            for (String idStr : categoryIdStrings) {
                try {
                    categoryIds.add(Long.parseLong(idStr));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid category ID: " + idStr);
                }
            }

            if (categoryIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không có danh mục hợp lệ để khôi phục");
                return "redirect:/admin/categories?tab=deleted";
            }

            System.out.println("Category IDs to restore: " + categoryIds);

            int restoredCount = 0;
            for (Long categoryId : categoryIds) {
                try {
                    categoryService.restoreCategory(categoryId);
                    restoredCount++;
                } catch (Exception e) {
                    System.err.println("Error restoring category " + categoryId + ": " + e.getMessage());
                }
            }

            if (restoredCount > 0) {
                redirectAttributes.addFlashAttribute("success",
                        "Đã khôi phục thành công " + restoredCount + " danh mục");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể khôi phục danh mục nào");
            }

            return "redirect:/admin/categories?tab=deleted";
        } catch (Exception e) {
            System.err.println("Error in bulk restore: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi khôi phục danh mục: " + e.getMessage());
            return "redirect:/admin/categories?tab=deleted";
        }
    }
}