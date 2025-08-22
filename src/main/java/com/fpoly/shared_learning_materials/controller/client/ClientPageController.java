package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.service.CategoryService;
import com.fpoly.shared_learning_materials.service.DocumentService;

import java.util.List;

/**
 * Controller for main client pages
 */
@Controller
@RequestMapping("/")
public class ClientPageController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "Về chúng tôi");
        return "client/about";
    }

    @GetMapping("/categories")
    public String categories(Model model, @RequestParam(required = false) String category) {
        model.addAttribute("pageTitle", "Danh mục tài liệu");
        model.addAttribute("selectedCategory", category);
        return "client/categories";
    }

    @GetMapping("/search")
    public String search(Model model,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        model.addAttribute("pageTitle", "Tìm kiếm tài liệu");
        model.addAttribute("query", q);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);
        model.addAttribute("category", category);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // Load categories for dropdown
        try {
            List<CategoryDTO> allCategories = categoryService.getActiveCategories();
            model.addAttribute("searchCategories", allCategories);
        } catch (Exception e) {
            model.addAttribute("searchCategories", List.of());
        }

        // Perform search if query or category is provided
        if ((q != null && !q.trim().isEmpty()) || (category != null && !category.trim().isEmpty())) {
            try {
                // Create pageable for search results
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

                // Convert category slug to category ID if needed
                Long categoryId = null;
                if (category != null && !category.trim().isEmpty() && !"all".equals(category)) {
                    categoryId = getCategoryIdBySlug(category);
                }

                // Get search results
                Page<DocumentDTO> searchResults = documentService.getFilteredDocuments(
                        pageable,
                        q,
                        "APPROVED", // Only show approved documents
                        categoryId,
                        null, // type
                        null, // price
                        null, // author
                        null, // tags
                        null, // dateFrom
                        null, // dateTo
                        null, // size
                        null // views
                );

                model.addAttribute("searchResults", searchResults);
                model.addAttribute("totalResults", searchResults.getTotalElements());
                model.addAttribute("totalPages", searchResults.getTotalPages());

            } catch (Exception e) {
                model.addAttribute("searchError", "Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.");
                Pageable pageable = PageRequest.of(page, size);
                model.addAttribute("searchResults", Page.empty(pageable));
                model.addAttribute("totalResults", 0L);
                model.addAttribute("totalPages", 0);
            }
        } else {
            // No search performed, show empty results
            Pageable pageable = PageRequest.of(page, size);
            model.addAttribute("searchResults", Page.empty(pageable));
            model.addAttribute("totalResults", 0L);
            model.addAttribute("totalPages", 0);
        }

        return "client/search";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        model.addAttribute("pageTitle", "Đăng tải tài liệu");
        return "client/upload";
    }

    /**
     * Helper method to get category ID by slug
     */
    private Long getCategoryIdBySlug(String slug) {
        try {
            List<CategoryDTO> categories = categoryService.getActiveCategories();
            return categories.stream()
                    .filter(cat -> slug.equals(cat.getSlug()))
                    .map(CategoryDTO::getId)
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}