package com.fpoly.shared_learning_materials.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.beans.factory.annotation.Autowired;


import com.fpoly.shared_learning_materials.service.CategoryService;
import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.service.FavoriteService;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.util.NumberFormatUtils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/")
    public String home(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }

        // Lấy danh sách root categories active để hiển thị trên trang chủ
        try {
            logger.info("Loading active root categories for home page");
            List<CategoryDTO> activeRootCategories = categoryService.getActiveRootCategories();
            logger.info("Successfully loaded {} categories", activeRootCategories.size());

            // Format document counts cho hiển thị đẹp hơn
            activeRootCategories.forEach(category -> {
                category.setDocumentCount(category.getDocuments()); // Đảm bảo documentCount được set
            });

            model.addAttribute("categories", activeRootCategories);
            model.addAttribute("totalCategories", activeRootCategories.size());

            // Load all active categories for search dropdown
            List<CategoryDTO> allActiveCategories = categoryService.getActiveCategories();
            model.addAttribute("searchCategories", allActiveCategories);

            // Tính tổng số documents
            long totalDocuments = activeRootCategories.stream()
                    .mapToLong(CategoryDTO::getDocuments)
                    .sum();
            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("formattedTotalDocuments",
                    NumberFormatUtils.formatDocumentCountWithPlus(totalDocuments));

            logger.info("Home page data loaded successfully. Total categories: {}, Total documents: {}",
                    activeRootCategories.size(), totalDocuments);

        } catch (Exception e) {
            // Log error và hiển thị danh sách rỗng nếu có lỗi
            logger.error("Error loading categories for home page: {}", e.getMessage(), e);
            model.addAttribute("categories", List.of());
            model.addAttribute("totalCategories", 0);
            model.addAttribute("totalDocuments", 0L);
            model.addAttribute("formattedTotalDocuments", "0");
            model.addAttribute("categoriesError", "Không thể tải danh mục. Vui lòng thử lại sau.");
        }

        // Lấy tài liệu nổi bật
        try {
            logger.info("Loading featured documents for home page");
            List<DocumentDTO> featuredDocuments = documentService.getFeaturedDocuments(8);
            logger.info("Successfully loaded {} featured documents", featuredDocuments.size());

            model.addAttribute("featuredDocuments", featuredDocuments);

        } catch (Exception e) {
            logger.error("Error loading featured documents for home page: {}", e.getMessage(), e);
            model.addAttribute("featuredDocuments", List.of());
            model.addAttribute("featuredDocumentsError", "Không thể tải tài liệu nổi bật. Vui lòng thử lại sau.");
        }

        return "client/index";
    }

    @GetMapping("/home")
    public String homeAlias(Model model) {
        return home(model);
    }

    @GetMapping("/test")
    public String testPages(Model model) {
        model.addAttribute("pageTitle", "Test All Client Pages");
        return "test/client-pages";
    }

    @GetMapping("/debug/categories")
    public String debugCategories(Model model) {
        try {
            List<CategoryDTO> categories = categoryService.getActiveRootCategories();

            // Debug information
            for (CategoryDTO category : categories) {
                System.out.println("Category: " + category.getName());
                System.out.println("  - Documents: " + category.getDocuments());
                System.out.println("  - Popular Tags: "
                        + (category.getPopularTags() != null ? category.getPopularTags().size() : 0));
                if (category.getPopularTags() != null && !category.getPopularTags().isEmpty()) {
                    System.out.println("  - Tags: " + category.getPopularTags());
                }
            }

            model.addAttribute("categories", categories);
            model.addAttribute("debugInfo", "Check console for detailed information");

        } catch (Exception e) {
            logger.error("Error in debug categories: {}", e.getMessage(), e);
            model.addAttribute("error", e.getMessage());
        }

        return "client/index"; // Reuse the same template
    }


    @GetMapping("/favicon.ico")
    public void favicon(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}