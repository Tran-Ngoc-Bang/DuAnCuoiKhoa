package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for main client pages
 */
@Controller
@RequestMapping("/")
public class ClientPageController {

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
            @RequestParam(required = false) String category) {
        model.addAttribute("pageTitle", "Tìm kiếm tài liệu");
        model.addAttribute("query", q);
        model.addAttribute("sort", sort);
        model.addAttribute("filter", filter);
        model.addAttribute("category", category);
        return "client/search";
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        model.addAttribute("pageTitle", "Đăng tải tài liệu");
        return "client/upload";
    }
}