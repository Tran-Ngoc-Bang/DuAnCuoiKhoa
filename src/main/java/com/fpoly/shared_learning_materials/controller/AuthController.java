package com.fpoly.shared_learning_materials.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.shared_learning_materials.service.UserService;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "expired", required = false) String expired,
            @RequestParam(value = "registered", required = false) String registered,
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
        if(registered != null) {
            model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
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
    public String processRegisters(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
             @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "termsAgreement", required = false) String termsAgreement,
            Model model) {
// Kiểm tra xác nhận mật khẩu
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Mật khẩu và xác nhận mật khẩu không khớp!");
            return "client/register";
        }

        // Gọi service để xử lý đăng ký
        boolean isRegistered = userService.registerUser(firstName, lastName,username, email, password);
        if (!isRegistered) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc email đã được sử dụng. Vui lòng chọn tên khác.");
            return "client/register";
        }

        model.addAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("message", "Bạn không có quyền truy cập trang này!");
        return "error/access-denied";
    }
    @GetMapping("/confirm")
    public String confirmUser(@RequestParam("username") String username, Model model) {
        boolean isConfirmed = userService.confirmUser(username);
        if (isConfirmed) {
            model.addAttribute("success", "Tài khoản của bạn đã được kích hoạt! Vui lòng đăng nhập.");
        } else {
            model.addAttribute("error", "Liên kết xác nhận không hợp lệ hoặc tài khoản đã được kích hoạt!");
        }
        return "client/login";
    }
}