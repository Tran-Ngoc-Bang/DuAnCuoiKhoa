package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Notification;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
@RequestMapping("/admin/notifications")

public class NotificationController extends BaseAdminController {
@Autowired
    NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }
    // Đánh dấu notification đã đọc - GET method (redirect)
    @GetMapping("/{id}/mark-read")
    public String markAsRead(@PathVariable Long id,
            @RequestParam(defaultValue = "/admin") String redirect,
            RedirectAttributes redirectAttributes) {
        try {
            notificationService.markAsRead(id);
            redirectAttributes.addFlashAttribute("success", "Đã đánh dấu thông báo là đã đọc");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đánh dấu đã đọc: " + e.getMessage());
        }

        return "redirect:" + redirect;
    }

    // Đánh dấu notification đã đọc - POST method (AJAX)
    @PostMapping("/{id}/mark-read")
    @ResponseBody
    public String markAsReadAjax(@PathVariable Long id) {
        try {
            notificationService.markAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error";
        }
    }

    // Đánh dấu tất cả đã đọc - GET method (redirect)
    @GetMapping("/mark-all-read")
    public String markAllAsRead(@RequestParam(defaultValue = "/admin") String redirect,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                username = "admin"; // Fallback for testing
            }

            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElse(null);

            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Admin đánh dấu tất cả system notifications đã đọc
                List<Notification> notifications = notificationService.getRecentNotificationsForAdmin();
                for (Notification notification : notifications) {
                    if (!notification.getIsRead()) {
                        notificationService.markAsRead(notification.getId());
                    }
                }
            } else if (currentUser != null) {
                // User thường đánh dấu notifications của mình
                notificationService.markAllAsRead(currentUser);
            }

            redirectAttributes.addFlashAttribute("success", "Đã đánh dấu tất cả thông báo là đã đọc");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi đánh dấu tất cả đã đọc: " + e.getMessage());
        }

        return "redirect:" + redirect;
    }

    // Đánh dấu tất cả đã đọc - POST method (AJAX)
    @PostMapping("/mark-all-read")
    @ResponseBody
    public String markAllAsReadAjax(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                username = "admin"; // Fallback for testing
            }

            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElse(null);

            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Admin đánh dấu tất cả system notifications đã đọc
                List<Notification> notifications = notificationService.getRecentNotificationsForAdmin();
                for (Notification notification : notifications) {
                    if (!notification.getIsRead()) {
                        notificationService.markAsRead(notification.getId());
                    }
                }
            } else if (currentUser != null) {
                // User thường đánh dấu notifications của mình
                notificationService.markAllAsRead(currentUser);
            }

            return "success";
        } catch (Exception e) {
            return "error";
        }
    }
}