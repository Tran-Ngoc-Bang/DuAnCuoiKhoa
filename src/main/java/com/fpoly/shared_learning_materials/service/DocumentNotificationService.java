package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.controller.admin.SettingController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service để gửi email thông báo tài liệu mới cho users
 * Chỉ gửi khi có tài liệu mới, theo tần suất hàng ngày/tuần
 */
@Service
public class DocumentNotificationService {

    @Autowired
    private EmailConfigService emailConfigService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.fpoly.shared_learning_materials.repository.DocumentOwnerRepository documentOwnerRepository;

    // Chạy mỗi giờ để kiểm tra
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 ms - ENABLED since email is working
    public void checkAndSendNewDocumentNotifications() {
        try {
            System.out.println("=== CHECKING NEW DOCUMENT NOTIFICATIONS ===");
            
            // Kiểm tra xem có bật thông báo không
            boolean enableNotifications = SettingController.getBooleanSetting("enableDocumentNotifications", false);
            if (!enableNotifications) {
                System.out.println("Document notifications disabled, skipping...");
                return;
            }

            String frequency = SettingController.getStringSetting("documentNotificationFrequency", "never");
            System.out.println("Notification frequency: " + frequency);

            if ("never".equals(frequency)) {
                System.out.println("Frequency is 'never', skipping...");
                return;
            }

            // Kiểm tra xem có cần gửi email không
            if (shouldSendNotification(frequency)) {
                System.out.println("Time to check for new documents!");
                checkAndSendNotifications(frequency);
            } else {
                System.out.println("Not time to send notifications yet...");
            }

        } catch (Exception e) {
            System.err.println("Error in document notification check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean shouldSendNotification(String frequency) {
        try {
            String lastSentStr = SettingController.getStringSetting("lastDocumentNotificationSent", "Chưa gửi");
            
            if ("Chưa gửi".equals(lastSentStr)) {
                return true;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime lastSent = LocalDateTime.parse(lastSentStr, formatter);
            LocalDateTime now = LocalDateTime.now();

            switch (frequency) {
                case "daily":
                    long hoursSinceLastSent = ChronoUnit.HOURS.between(lastSent, now);
                    return hoursSinceLastSent >= 24;
                case "weekly":
                    long daysSinceLastSent = ChronoUnit.DAYS.between(lastSent, now);
                    return daysSinceLastSent >= 7;
                default:
                    return false;
            }

        } catch (Exception e) {
            System.err.println("Error checking notification timing: " + e.getMessage());
            return true;
        }
    }

    private void checkAndSendNotifications(String frequency) {
        try {
            // Tính thời gian để tìm tài liệu mới
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime fromDate;
            
            switch (frequency) {
                case "daily":
                    fromDate = now.minusDays(1);
                    break;
                case "weekly":
                    fromDate = now.minusWeeks(1);
                    break;
                default:
                    fromDate = now.minusDays(1);
            }

            // Tìm tài liệu mới trong khoảng thời gian
            List<Document> newDocuments = documentRepository.findByCreatedAtBetweenAndDeletedAtIsNull(fromDate, now);
            
            if (newDocuments.isEmpty()) {
                System.out.println("No new documents found, skipping email...");
                updateLastNotificationSentTime();
                return;
            }

            System.out.println("Found " + newDocuments.size() + " new documents, sending notifications...");

            // Lấy tất cả user active (không bị xóa)
            List<User> activeUsers = userRepository.findByDeletedAtIsNull();
            
            if (activeUsers.isEmpty()) {
                System.out.println("No active users found");
                return;
            }

            // Tạo nội dung email
            String emailContent = generateNewDocumentEmailContent(newDocuments, frequency);
            String subject = "📚 Tài liệu mới trên EduShare - " + 
                           (frequency.equals("daily") ? "Cập nhật hàng ngày" : "Cập nhật hàng tuần");

            // Gửi email cho tất cả users với rate limiting
            int successCount = 0;
            int failedCount = 0;
            int maxEmailsPerBatch = 50; // Giới hạn để tránh Gmail limit
            
            for (int i = 0; i < activeUsers.size() && successCount < maxEmailsPerBatch; i++) {
                User user = activeUsers.get(i);
                try {
                    if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                        boolean sent = emailConfigService.sendHtmlEmail(user.getEmail(), subject, emailContent);
                        if (sent) {
                            successCount++;
                        } else {
                            failedCount++;
                        }
                        
                        // Thêm delay nhỏ giữa các email để tránh spam
                        if (i % 10 == 0 && i > 0) {
                            Thread.sleep(1000); // 1 second delay mỗi 10 emails
                        }
                    }
                } catch (Exception e) {
                    failedCount++;
                    System.err.println("Failed to send email to " + user.getEmail() + ": " + e.getMessage());
                    
                    // Nếu gặp lỗi Gmail limit, dừng gửi
                    if (e.getMessage().contains("Daily user sending limit exceeded")) {
                        System.err.println("Gmail daily limit reached. Stopping email sending.");
                        break;
                    }
                }
            }

            System.out.println("Email sending completed: " + successCount + " successful, " + failedCount + " failed out of " + activeUsers.size() + " total users");
            
            if (successCount < activeUsers.size()) {
                System.out.println("⚠️ Not all users received emails due to rate limiting or Gmail daily limit");
            }
            
            // Cập nhật thời gian gửi cuối cùng
            updateLastNotificationSentTime();

        } catch (Exception e) {
            System.err.println("Error sending document notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String generateNewDocumentEmailContent(List<Document> newDocuments, String frequency) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String periodText = frequency.equals("daily") ? "hôm nay" : "tuần này";
        
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Header
        html.append("<div style='background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>📚 Tài liệu mới trên EduShare</h1>");
        html.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>Cập nhật ").append(periodText).append("</p>");
        html.append("<p style='margin: 5px 0 0 0; font-size: 14px; opacity: 0.8;'>").append(formatter.format(now)).append("</p>");
        html.append("</div>");
        
        html.append("<div style='background: white; padding: 30px; border: 1px solid #e0e0e0; border-top: none;'>");
        
        // Thông báo số lượng tài liệu mới
        html.append("<div style='background: #e8f5e8; border: 2px solid #4caf50; border-radius: 8px; padding: 20px; margin-bottom: 20px; text-align: center;'>");
        html.append("<h2 style='color: #2e7d32; margin: 0 0 10px 0;'>🎉 Có ").append(newDocuments.size()).append(" tài liệu mới!</h2>");
        html.append("<p style='color: #2e7d32; margin: 0;'>Được thêm ").append(periodText).append("</p>");
        html.append("</div>");
        
        // Danh sách tài liệu mới
        html.append("<h3 style='color: #333; margin: 20px 0 15px 0;'>📋 Danh sách tài liệu mới:</h3>");
        
        for (Document doc : newDocuments) {
            html.append("<div style='border: 1px solid #ddd; border-radius: 8px; padding: 15px; margin-bottom: 15px; background: #f9f9f9;'>");
            html.append("<h4 style='margin: 0 0 8px 0; color: #333;'>").append(escapeHtml(doc.getTitle())).append("</h4>");
            
            if (doc.getDescription() != null && !doc.getDescription().trim().isEmpty()) {
                String description = doc.getDescription().length() > 100 ? 
                    doc.getDescription().substring(0, 100) + "..." : doc.getDescription();
                html.append("<p style='margin: 0 0 8px 0; color: #666; font-size: 14px;'>").append(escapeHtml(description)).append("</p>");
            }
            
            html.append("<div style='font-size: 12px; color: #888;'>");
            html.append("<span>📅 ").append(doc.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</span>");
            String authorName = getDocumentAuthorName(doc);
            html.append(" | <span>👤 ").append(escapeHtml(authorName)).append("</span>");
            html.append("</div>");
            html.append("</div>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; color: #666; font-size: 12px;'>");
        html.append("<p style='margin: 0;'>📧 Email này được gửi tự động từ hệ thống EduShare</p>");
        html.append("<p style='margin: 5px 0 0 0;'>Truy cập <a href='#' style='color: #4CAF50;'>EduShare</a> để xem chi tiết</p>");
        html.append("</div>");
        
        html.append("</div>");
        
        return html.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    private String getDocumentAuthorName(Document document) {
        try {
            List<com.fpoly.shared_learning_materials.domain.DocumentOwner> owners = 
                documentOwnerRepository.findByDocumentId(document.getId());
            if (!owners.isEmpty() && owners.get(0).getUser() != null) {
                com.fpoly.shared_learning_materials.domain.User user = owners.get(0).getUser();
                return user.getFullName() != null ? user.getFullName() : user.getUsername();
            }
            return "Không xác định";
        } catch (Exception e) {
            return "Không xác định";
        }
    }

    private void updateLastNotificationSentTime() {
        try {
            String sentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            
            // Cập nhật thời gian gửi cuối cùng
            java.lang.reflect.Field settingsField = SettingController.class.getDeclaredField("settings");
            settingsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> settings = (java.util.Map<String, Object>) settingsField.get(null);
            settings.put("lastDocumentNotificationSent", sentTime);
            
            System.out.println("Updated lastDocumentNotificationSent to: " + sentTime);
        } catch (Exception e) {
            System.err.println("Error updating lastDocumentNotificationSent: " + e.getMessage());
        }
    }

    // Method để gửi thông báo ngay lập tức khi có tài liệu mới (được gọi từ DocumentController)
    public void sendImmediateNotification(Document newDocument) {
        try {
            boolean enableNotifications = SettingController.getBooleanSetting("enableDocumentNotifications", false);
            String frequency = SettingController.getStringSetting("documentNotificationFrequency", "never");
            
            if (!enableNotifications || !"immediate".equals(frequency)) {
                return;
            }

            List<User> activeUsers = userRepository.findByDeletedAtIsNull();
            if (activeUsers.isEmpty()) {
                return;
            }

            String emailContent = generateSingleDocumentEmailContent(newDocument);
            String subject = "📚 Tài liệu mới: " + newDocument.getTitle();

            for (User user : activeUsers) {
                try {
                    emailConfigService.sendHtmlEmail(user.getEmail(), subject, emailContent);
                } catch (Exception e) {
                    System.err.println("Failed to send immediate notification to " + user.getEmail());
                }
            }

        } catch (Exception e) {
            System.err.println("Error sending immediate notification: " + e.getMessage());
        }
    }

    private String generateSingleDocumentEmailContent(Document document) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        
        // Header
        html.append("<div style='background: linear-gradient(135deg, #2196F3 0%, #1976D2 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>📚 Tài liệu mới vừa được thêm!</h1>");
        html.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>EduShare</p>");
        html.append("</div>");
        
        html.append("<div style='background: white; padding: 30px; border: 1px solid #e0e0e0; border-top: none;'>");
        
        // Thông tin tài liệu
        html.append("<div style='border: 2px solid #2196F3; border-radius: 8px; padding: 20px; margin-bottom: 20px;'>");
        html.append("<h2 style='color: #1976D2; margin: 0 0 15px 0;'>").append(escapeHtml(document.getTitle())).append("</h2>");
        
        if (document.getDescription() != null && !document.getDescription().trim().isEmpty()) {
            html.append("<p style='margin: 0 0 15px 0; color: #666; line-height: 1.5;'>").append(escapeHtml(document.getDescription())).append("</p>");
        }
        
        html.append("<div style='font-size: 14px; color: #888;'>");
        html.append("<p style='margin: 5px 0;'>📅 Thời gian: ").append(document.getCreatedAt().format(formatter)).append("</p>");
        String authorName = getDocumentAuthorName(document);
        html.append("<p style='margin: 5px 0;'>👤 Người đăng: ").append(escapeHtml(authorName)).append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        html.append("</div>");
        
        // Footer
        html.append("<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; color: #666; font-size: 12px;'>");
        html.append("<p style='margin: 0;'>📧 Thông báo tự động từ EduShare</p>");
        html.append("<p style='margin: 5px 0 0 0;'>Truy cập website để xem chi tiết tài liệu</p>");
        html.append("</div>");
        
        html.append("</div>");
        
        return html.toString();
    }
}