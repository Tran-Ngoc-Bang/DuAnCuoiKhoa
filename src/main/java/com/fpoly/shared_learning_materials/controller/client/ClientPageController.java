package com.fpoly.shared_learning_materials.controller.client;

import java.util.List;
import java.util.Map;
import java.util.Locale.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.dto.CategoryDTO;
import com.fpoly.shared_learning_materials.service.CategoryService;
import com.fpoly.shared_learning_materials.service.DocumentService;

/**
 * Controller for main client pages
 */
@Controller
@RequestMapping("/")
public class ClientPageController {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private CategoryService categoryService;

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
    public String search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) List<String> format,
            @RequestParam(required = false) List<String> price,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String time,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Document> searchResults = documentService.searchDocuments(q, category, format, price, rating, time,
                pageable);

        model.addAttribute("searchResults", searchResults);
        model.addAttribute("query", q);
        model.addAttribute("category", category);
        model.addAttribute("format", format);
        model.addAttribute("price", price);
        model.addAttribute("rating", rating);
        model.addAttribute("time", time);

        // Theo định dạng
        Map<String, Long> formatCounts = documentService.countFormats(q);
        model.addAttribute("formatCounts", formatCounts);

        // Theo danh mục
        List<CategoryDTO> allCategories = categoryService.getAllCategories();
        Map<String, Long> categoryCounts = documentService.countDocumentsPerCategorySlug();
        model.addAttribute("categories", allCategories);
        model.addAttribute("categoryCounts", categoryCounts);

        // Theo giá
        Map<String, Long> priceCounts = documentService.countByPrice(q);
        model.addAttribute("price", price);
        model.addAttribute("priceCounts", priceCounts);

        // Theo đánh giá
        Map<String, Long> ratingCounts = documentService.countByRating(q);
        model.addAttribute("rating", rating);
        model.addAttribute("ratingCounts", ratingCounts);


        Map<String, Long> timeCounts = documentService.countByTime(q);
        model.addAttribute("time", time);
        model.addAttribute("timeCounts", timeCounts);

        return "client/search";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        model.addAttribute("pageTitle", "Đăng tải tài liệu");
        return "client/upload";
    }
}