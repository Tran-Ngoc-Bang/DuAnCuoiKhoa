package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Notification;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.NotificationRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Tạo thông báo mới
    public Notification createNotification(User user, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Tạo thông báo hệ thống (không có user cụ thể)
    public Notification createSystemNotification(String title, String message) {
        Notification notification = new Notification();
        notification.setUser(null); // System notification
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType("system");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    // Lấy thông báo của user
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // Lấy thông báo chưa đọc
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    // Đếm thông báo chưa đọc
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    // Lấy thông báo gần đây cho admin (limit 10) - bao gồm system notifications
    public List<Notification> getRecentNotificationsForAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        return notificationRepository.findRecentSystemNotifications(pageable);
    }

    // Lấy tất cả thông báo gần đây (cho debug)
    public List<Notification> getAllRecentNotifications() {
        Pageable pageable = PageRequest.of(0, 10);
        return notificationRepository.findAllRecentNotifications(pageable);
    }

    // Đánh dấu đã đọc
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Đánh dấu tất cả đã đọc
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    // Xóa thông báo
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    // Lấy thông báo theo ID
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    // Tạo thông báo khi có tài liệu mới
    public void notifyNewDocument(String documentTitle, String uploaderName) {
        String title = "Tài liệu mới được đăng tải";
        String message = String.format("%s đã đăng tải tài liệu: %s", uploaderName, documentTitle);
        createSystemNotification(title, message);
    }

    // Tạo thông báo khi có user mới đăng ký
    public void notifyNewUserRegistration(String userName) {
        String title = "Người dùng mới đăng ký";
        String message = String.format("%s đã đăng ký tài khoản mới", userName);
        createSystemNotification(title, message);
    }

    // Tạo thông báo khi có báo cáo vi phạm
    public void notifyNewReport(String reporterName, String reason) {
        String title = "Báo cáo vi phạm mới";
        String message = String.format("Có báo cáo vi phạm mới từ %s: %s", reporterName, reason);
        createSystemNotification(title, message);
    }

    // Tạo thông báo khi có giao dịch mới
    public void notifyNewTransaction(String userName, String amount) {
        String title = "Giao dịch mới";
        String message = String.format("%s đã nạp %s vào tài khoản", userName, amount);
        createSystemNotification(title, message);
    }

    // Tạo thông báo khi có bình luận mới
    public void notifyNewComment(String commenterName, String documentTitle) {
        String title = "Bình luận mới";
        String message = String.format("%s đã bình luận về tài liệu: %s", commenterName, documentTitle);
        createSystemNotification(title, message);
    }

    // Tạo thông báo cho tất cả admin khi có tài liệu mới
    public void notifyAdminsNewDocument(String documentTitle, String uploaderName) {
        String title = "Tài liệu mới được đăng tải";
        String message = String.format("%s đã đăng tải tài liệu: %s", uploaderName, documentTitle);

        try {
            // Tìm tất cả admin users và tạo notification riêng cho từng người
            List<User> adminUsers = userRepository.findByRoleAndDeletedAtIsNull("ADMIN");
            System.out.println("Found " + adminUsers.size() + " admin users to notify");

            for (User admin : adminUsers) {
                createNotification(admin, title, message, "document");
                System.out.println("Created notification for admin: " + admin.getUsername());
            }

            // Nếu không có admin user nào, tạo system notification
            if (adminUsers.isEmpty()) {
                System.out.println("No admin users found, creating system notification");
                createSystemNotification(title, message);
            }
        } catch (Exception e) {
            System.err.println("Error creating admin notifications: " + e.getMessage());
            // Fallback: tạo system notification
            createSystemNotification(title, message);
        }
    }
}