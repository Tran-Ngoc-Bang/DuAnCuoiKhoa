package com.fpoly.shared_learning_materials.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("success", "Đăng xuất thành công!");
        }

        if (expired != null) {
            model.addAttribute("warning", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
        }

        return "client/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        return "client/register";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @PostMapping("/register")
    public String processRegister(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            Model model) {

        // TODO: Implement user registration logic
        // For now, just redirect to login with success message
        model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Bạn không có quyền truy cập trang này!");
        return "error/access-denied";
    }
}