package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Notification;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ModelAttribute;
import jakarta.servlet.http.HttpSession;

import java.util.List;

@RequiredArgsConstructor
public abstract class BaseAdminController {

    protected final NotificationService notificationService;
    protected final UserRepository userRepository;

    @ModelAttribute("notifications")
    public List<Notification> getNotifications(HttpSession session) {
        try {
            // Lấy user từ session
            String username = (String) session.getAttribute("username");
            if (username == null) {
                username = "admin"; // Fallback for testing
            }

            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElse(null);

            List<Notification> notifications;

            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Admin xem tất cả notifications
                notifications = notificationService.getRecentNotificationsForAdmin();
                System.out.println("DEBUG: Admin loaded " + notifications.size() + " notifications");
            } else if (currentUser != null) {
                // User thường xem notifications của mình
                notifications = notificationService.getUserNotifications(currentUser);
                System.out.println("DEBUG: User " + username + " loaded " + notifications.size() + " notifications");
            } else {
                notifications = List.of();
                System.out.println("DEBUG: No user found, returning empty notifications");
            }

            // Debug: in ra thông tin từng notification
            for (int i = 0; i < notifications.size(); i++) {
                Notification n = notifications.get(i);
                System.out.println(
                        "DEBUG: Notification " + (i + 1) + ": " + n.getMessage() + " (read: " + n.getIsRead() + ")");
            }

            return notifications;
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            return List.of();
        }
    }

    @ModelAttribute("unreadCount")
    public Long getUnreadCount(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                username = "admin"; // Fallback for testing
            }

            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElse(null);

            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Admin đếm system notifications chưa đọc
                List<Notification> notifications = notificationService.getRecentNotificationsForAdmin();
                return notifications.stream()
                        .filter(n -> !n.getIsRead())
                        .count();
            } else if (currentUser != null) {
                // User thường đếm notifications của mình
                return notificationService.getUnreadCount(currentUser);
            }

            return 0L;
        } catch (Exception e) {
            System.err.println("Error counting unread notifications: " + e.getMessage());
            return 0L;
        }
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                username = "admin"; // Fallback for testing
            }

            return userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // getCurrentPage method removed - now handled by JavaScript in sidebar
}