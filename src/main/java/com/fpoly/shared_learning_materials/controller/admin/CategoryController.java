package com.fpoly.shared_learning_materials.controller.admin;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fpoly.shared_learning_materials.service.CategoryService;


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
//    @Autowired
//    private UserService userService;

    @GetMapping
    public String getCategories(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "none") String sort) {
        System.out.println("Request: page=" + page + ", size=" + size + ", filter=" + filter + ", status=" + status + ", sort=" + sort);
        
        List<CategoryDTO> allCategories = categoryService.getAllCategories();

        if (!filter.isEmpty()) {
            String searchTerm = filter.toLowerCase();
            allCategories = allCategories.stream()
                    .filter(cat -> cat.getName().toLowerCase().contains(searchTerm) ||
                            (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(searchTerm)))
                    .collect(Collectors.toList());
        }

        if (!status.equals("all")) {
            allCategories = allCategories.stream()
                    .filter(cat -> cat.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        switch (sort) {
            case "name_asc":
                allCategories.sort(Comparator.comparing(CategoryDTO::getName));
                break;
            case "name_desc":
                allCategories.sort(Comparator.comparing(CategoryDTO::getName).reversed());
                break;
            case "date_newest":
                allCategories.sort(Comparator.comparing(cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())));
                break;
            case "date_oldest":
                allCategories.sort(Comparator.comparing(cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder())));
                break;
            case "count_most":
                allCategories.sort(Comparator.comparingInt(CategoryDTO::getDocuments).reversed());
                break;
            case "count_least":
                allCategories.sort(Comparator.comparingInt(CategoryDTO::getDocuments));
                break;
            default:
                // Mặc định sắp xếp theo thời gian tạo mới nhất
                allCategories.sort(Comparator.comparing(cat -> cat.getHierarchyCreatedAt() != null ? cat.getHierarchyCreatedAt() : cat.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder())));
                break;
        }

        int totalItems = allCategories.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        page = Math.max(1, page);
        page = Math.min(page, totalPages == 0 ? 1 : totalPages);

        int start = (page - 1) * size;
        int end = Math.min(start + size, totalItems);
        List<CategoryDTO> pagedCategories = totalItems > 0 ? allCategories.subList(start, end) : List.of();

        System.out.println("Categories size: " + totalItems + ", totalPages: " + totalPages + ", page: " + page);

        model.addAttribute("categories", pagedCategories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        model.addAttribute("filter", filter);
        model.addAttribute("status", status);
        model.addAttribute("sort", sort);
        return "admin/categories/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/categories/create";
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("category") CategoryDTO categoryDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/create";
        }

        try {
        	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        Long userId = null;

	        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
	            Object principal = authentication.getPrincipal();
	            if (principal instanceof UserDetails) {
	                String username = ((UserDetails) principal).getUsername();
	                User user = userRepository.findByUsername(username)
	                        .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng với username: " + username));
	                userId = user.getId();
	            } else {
	                model.addAttribute("errorMessage", "Không thể xác định thông tin người dùng đã đăng nhập");
	                return "admin/tags/create";
	            }
	        } else {
	            model.addAttribute("errorMessage", "Bạn cần đăng nhập để tạo tag");
	            return "admin/tags/create";
	        }
            	   categoryDTO.setCreatedById(userId);
//            }
            categoryService.createCategory(categoryDTO);
            return "redirect:/admin/categories?success=true";
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
    public String updateCategory(@PathVariable Long id, @Valid @ModelAttribute("category") CategoryDTO categoryDTO, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/categories/edit";
        }

        try {
        	  System.out.println("Updating category ID: " + id + ", New parentId: " + categoryDTO.getParentId());
            categoryService.updateCategory(id, categoryDTO);
            
            return "redirect:/admin/categories?success=true";
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

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok().body("{\"message\": \"Xóa danh mục thành công.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping
    @ResponseBody
    public ResponseEntity<?> deleteCategories(@RequestBody List<Long> categoryIds) {
        try {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Chưa chọn danh mục nào.\"}");
            }
            categoryService.deleteCategories(categoryIds);
            return ResponseEntity.ok().body("{\"message\": \"Xóa các danh mục thành công.\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
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
}