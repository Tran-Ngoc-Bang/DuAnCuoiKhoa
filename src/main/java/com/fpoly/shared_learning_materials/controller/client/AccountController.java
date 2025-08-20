package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for user account pages
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Bảng điều khiển");
        return "client/account/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("pageTitle", "Thông tin cá nhân");
        return "client/account/profile";
    }

    @GetMapping("/documents")
    public String myDocuments(Model model) {
        model.addAttribute("pageTitle", "Tài liệu của tôi");
        return "client/account/documents";
    }

    @GetMapping("/favorites")
    public String favorites(Model model) {
        model.addAttribute("pageTitle", "Tài liệu yêu thích");
        return "client/account/favorites";
    }

    @GetMapping("/transactions")
    public String transactions(Model model) {
        model.addAttribute("pageTitle", "Lịch sử giao dịch");
        return "client/account/transactions";
    }

    @GetMapping("/recharge")
    public String recharge(Model model) {
        model.addAttribute("pageTitle", "Nạp tiền");
        return "client/account/recharge";
    }

    @GetMapping("/security")
    public String security(Model model) {
        model.addAttribute("pageTitle", "Bảo mật tài khoản");
        return "client/account/security";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("pageTitle", "Thông báo");
        return "client/account/notifications";
    }

    @GetMapping("/options")
    public String options(Model model) {
        model.addAttribute("pageTitle", "Tùy chọn");
        return "client/account/options";
    }

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("pageTitle", "Hỗ trợ");
        return "client/account/support";
    }
}