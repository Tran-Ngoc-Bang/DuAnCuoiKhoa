package com.fpoly.shared_learning_materials.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        // Log để debug
        System.out.println("Login page accessed with params - error: " + error + ", logout: " + logout + ", expired: "
                + expired + ", registered: " + registered);

        // Only redirect authenticated users if they're not logging out
        if (logout == null && SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {

            // Check if user is admin
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            String redirectUrl = isAdmin ? "/admin" : "/";
            System.out.println("User already authenticated, redirecting to: " + redirectUrl);
            return "redirect:" + redirectUrl;
        }

        // Add error message if present
        if (error != null) {
            model.addAttribute("error", error);
        }

        if (logout != null) {
            model.addAttribute("success", "Đăng xuất thành công!");
        }

        if (expired != null) {
            model.addAttribute("warning", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
        }

        if (registered != null) {
            model.addAttribute("success", "Đăng ký thành công! Vui lòng xác thực bằng gmail.");
        }

        return "client/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        // Redirect authenticated users away from register page
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
                SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
                !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {

            // Check if user is admin
            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

            return isAdmin ? "redirect:/admin" : "redirect:/";
        }

        return "client/register";
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
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp!");
            return "client/register";
        }

        // Validate password strength
        if (!isPasswordStrong(password)) {
            model.addAttribute("error",
                    "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            return "client/register";
        }

        // Gọi service để xử lý đăng ký
        boolean isRegistered = userService.registerUser(firstName, lastName, username, email, password);
        if (!isRegistered) {
            model.addAttribute("error", "Tên đăng nhập hoặc email đã được sử dụng. Vui lòng chọn tên khác.");
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

    @PostMapping("/access-denied")
    public String accessDeniedPost(Model model) {
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

    // ===== FORGOT PASSWORD ENDPOINTS =====

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "client/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        boolean emailSent = userService.sendResetCode(email);

        if (emailSent) {
            model.addAttribute("success", "Mã xác nhận đã được gửi đến email của bạn! Vui lòng kiểm tra email.");
            return "client/forgot-password";
        } else {
            model.addAttribute("error", "Email không tồn tại trong hệ thống!");
            return "client/forgot-password";
        }
    }

    @GetMapping("/verify-reset-code")
    public String showVerifyCodeForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "client/verify-reset-code";
    }

    @PostMapping("/verify-reset-code")
    public String processVerifyCode(@RequestParam("email") String email, @RequestParam("code") String code,
            Model model) {
        if (userService.verifyResetCode(email, code)) {
            model.addAttribute("email", email);
            model.addAttribute("code", code);
            return "client/reset-password";
        } else {
            model.addAttribute("error", "Mã xác nhận không đúng hoặc đã hết hạn!");
            model.addAttribute("email", email);
            return "client/verify-reset-code";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("email") String email,
            @RequestParam("code") String code,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp!");
            model.addAttribute("email", email);
            model.addAttribute("code", code);
            return "client/reset-password";
        }

        if (!isPasswordStrong(password)) {
            model.addAttribute("error",
                    "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt!");
            model.addAttribute("email", email);
            model.addAttribute("code", code);
            return "client/reset-password";
        }

        boolean resetSuccess = userService.resetPassword(email, code, password);

        if (resetSuccess) {
            model.addAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập với mật khẩu mới.");
            return "client/login";
        } else {
            model.addAttribute("error", "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại!");
            model.addAttribute("email", email);
            model.addAttribute("code", code);
            return "client/reset-password";
        }
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");

        // Require ALL criteria for strong password
        return hasLowercase && hasUppercase && hasNumber && hasSpecial;
    }
}