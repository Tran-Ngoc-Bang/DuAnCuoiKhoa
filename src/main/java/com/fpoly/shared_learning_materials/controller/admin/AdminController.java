package com.fpoly.shared_learning_materials.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    /**
     * Trang chủ admin dashboard
     */
    @GetMapping
    public String index(Model model) {
        // Thêm thông tin cần thiết cho dashboard
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/index";
    }

    /**
     * Trang dashboard (alias cho index)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return index(model);
    }
}