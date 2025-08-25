package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fpoly.shared_learning_materials.service.CoinPackageService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.domain.Notification;

import java.util.List;

/**
 * Controller for user account pages
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    @Autowired
    private CoinPackageService coinPackageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        model.addAttribute("coinPackages", coinPackageService.getActivePackagesForClient());
        return "client/account/recharge";
    }

    @GetMapping("/security")
    public String security(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("pageTitle", "Bảo mật tài khoản");
            model.addAttribute("currentUser", currentUser);
            
            // Check contact info availability (not verification)
            boolean hasEmail = currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty();
            boolean hasPhone = currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty();
            
            // Security level based on contact info
            String securityLevel;
            String securityIcon;
            if (hasEmail && hasPhone) {
                securityLevel = "Cao";
                securityIcon = "success";
            } else if (hasEmail) {
                securityLevel = "Trung bình";
                securityIcon = "warning";
            } else {
                securityLevel = "Thấp";
                securityIcon = "danger";
            }
            
            // Account status based on status field
            String accountStatus;
            String statusIcon;
            switch (currentUser.getStatus().toLowerCase()) {
                case "active":
                    accountStatus = "Hoạt động";
                    statusIcon = "success";
                    break;
                case "inactive":
                    accountStatus = "Khóa";
                    statusIcon = "danger";
                    break;
                case "pending":
                    accountStatus = "Đang chờ";
                    statusIcon = "warning";
                    break;
                default:
                    accountStatus = "Không xác định";
                    statusIcon = "warning";
            }
            
            model.addAttribute("hasEmail", hasEmail);
            model.addAttribute("hasPhone", hasPhone);
            model.addAttribute("securityLevel", securityLevel);
            model.addAttribute("securityIcon", securityIcon);
            model.addAttribute("accountStatus", accountStatus);
            model.addAttribute("statusIcon", statusIcon);
            
            // Thêm thông tin liên hệ
            model.addAttribute("userEmail", currentUser.getEmail());
            model.addAttribute("userPhone", currentUser.getPhoneNumber());
            
            // Thêm thông tin hoạt động đăng nhập
            model.addAttribute("lastLoginAt", currentUser.getLastLoginAt());
            model.addAttribute("lastLoginIp", currentUser.getLastLoginIp());
            model.addAttribute("failedLoginAttempts", currentUser.getFailedLoginAttempts());
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin bảo mật: " + e.getMessage());
        }
        
        return "client/account/security";
    }
    
    @PostMapping("/security/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại");
                return "redirect:/account/security";
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu mới");
                return "redirect:/account/security";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "redirect:/account/security";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
                return "redirect:/account/security";
            }
            
            // Get current user
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/account/security";
            }
            
            // Check if new password is different from current password
            if (passwordEncoder.matches(newPassword, currentUser.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác với mật khẩu hiện tại");
                return "redirect:/account/security";
            }
            
            // Validate password strength
            if (!isPasswordStrong(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
                return "redirect:/account/security";
            }
            
            // Update password
            currentUser.setPasswordHash(passwordEncoder.encode(newPassword));
            currentUser.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(currentUser);
            
            // Create notification
            notificationService.createNotification(currentUser, 
                "Mật khẩu đã được thay đổi", 
                "Mật khẩu tài khoản của bạn đã được thay đổi thành công vào lúc " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
                "system");
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/account/security";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/account/security";
        }
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Authentication authentication) {
        try {
            // Lấy user hiện tại
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Tạo thông báo mẫu nếu chưa có (chỉ để test)
            List<Notification> existingNotifications = notificationService.getUserNotifications(currentUser);
            if (existingNotifications.isEmpty()) {
                notificationService.createNotification(currentUser, 
                    "Chào mừng bạn đến với EduShare", 
                    "Cảm ơn bạn đã đăng ký tài khoản. Hãy khám phá các tài liệu học tập phong phú trên hệ thống của chúng tôi.", 
                    "system");
                    
                // notificationService.createNotification(currentUser, 
                //     "Tài liệu mới được đăng tải", 
                //     "Có tài liệu mới về 'Lập trình Java Spring Boot' vừa được đăng tải. Hãy xem ngay!", 
                //     "document");
                    
                // notificationService.createNotification(currentUser, 
                //     "Giao dịch thành công", 
                //     "Bạn đã nạp thành công 100.000 VNĐ vào tài khoản. Số xu hiện tại: 500 xu.", 
                //     "transaction");
            }
            
            // Lấy danh sách thông báo
            List<Notification> notifications = notificationService.getUserNotifications(currentUser);
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
            long unreadCount = notificationService.getUnreadCount(currentUser);
            
            // Tính toán số lượng thông báo theo loại
            long systemCount = notifications.stream().filter(n -> "system".equals(n.getType())).count();
            long readCount = notifications.size() - unreadCount;
            
            model.addAttribute("pageTitle", "Thông báo");
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotifications", unreadNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("systemCount", systemCount);
            model.addAttribute("readCount", readCount);
            model.addAttribute("currentUser", currentUser);
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông báo: " + e.getMessage());
        }
        
        return "client/account/notifications";
    }
    
    @PostMapping("/notifications/{id}/mark-read")
    @ResponseBody
    public String markNotificationAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Kiểm tra notification có thuộc về user hiện tại không
            List<Notification> userNotifications = notificationService.getUserNotifications(currentUser);
            boolean hasPermission = userNotifications.stream()
                    .anyMatch(n -> n.getId().equals(id));
            
            if (!hasPermission) {
                return "error: Không có quyền truy cập thông báo này";
            }
            
            notificationService.markAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/notifications/mark-all-read")
    @ResponseBody
    public String markAllNotificationsAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.markAllAsRead(currentUser);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
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