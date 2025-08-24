package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.service.EmailConfigService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class SettingController extends BaseAdminController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private EmailConfigService emailConfigService;

    public SettingController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }

    // Static storage để giả lập database
    private static final Map<String, Object> settings = new HashMap<>();

    static {
        // Initialize default settings
        initializeDefaultSettings();
    }

    // Initialize default settings
    private static void initializeDefaultSettings() {
        // Site settings
        settings.put("siteName", "EduShare");
        settings.put("siteDescription", "Hệ thống chia sẻ tài liệu học tập");
        settings.put("siteLogo", "/images/logo.png");
        settings.put("siteFavicon", "/images/favicon.ico");
        settings.put("contactEmail", "admin@edushare.vn");
        settings.put("contactPhone", "0123456789");
        settings.put("address", "123 Đường ABC, Quận XYZ, TP.HCM");

        // System settings
        settings.put("language", "vi");
        settings.put("timezone", "Asia/Ho_Chi_Minh");
        settings.put("dateFormat", "dd/MM/yyyy");
        settings.put("darkMode", false);
        settings.put("maintenanceMode", false);
        settings.put("maintenanceMessage", "Hệ thống đang bảo trì, vui lòng quay lại sau.");

        // User settings
        settings.put("allowRegistration", true);
        settings.put("requireEmailVerification", true);
        settings.put("passwordMinLength", 8);
        settings.put("sessionTimeout", 3600);

        // Document settings
        settings.put("documentsPerPage", 20);
        settings.put("maxFileSize", 50);
        settings.put("allowedFileTypes", "pdf,doc,docx,ppt,pptx,xls,xlsx");
        settings.put("showViewCount", true);
        settings.put("enableComments", true);
        settings.put("enableRatings", true);

        // Notification settings
        settings.put("notifyNewDocuments", true);
        settings.put("notifyNewComments", true);
        settings.put("notifySystemReports", true);
        settings.put("documentNotificationFrequency", "daily");
        settings.put("enableDocumentNotifications", true);
        settings.put("lastDocumentNotificationSent", "Chưa gửi");

        // Email settings
        settings.put("smtpServer", "");
        settings.put("smtpPort", "587");
        settings.put("emailSender", "");
        settings.put("emailDisplayName", "EduShare System");
        settings.put("smtpPassword", "");

        // Backup settings
        settings.put("lastBackup", "Chưa có backup");
        settings.put("backupSize", "0 MB");
        settings.put("totalBackups", "0");
    }

    @GetMapping
    public String index(Model model) {
        // Load current email config from application.properties
        Map<String, String> emailConfig = emailConfigService.getCurrentEmailConfig();
        settings.put("smtpServer", emailConfig.get("host"));
        settings.put("smtpPort", emailConfig.get("port"));
        settings.put("emailSender", emailConfig.get("username"));

        model.addAttribute("settings", settings);
        model.addAttribute("currentPage", "settings");
        return "admin/settings";
    }

    @PostMapping("/save")
    public String saveAllSettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate settings first
            if (!validateSettings(params, redirectAttributes)) {
                return "redirect:/admin/settings";
            }

            // Update all settings from form parameters
            settings.putAll(params);

            // Cập nhật cấu hình email nếu có thay đổi
            if (params.containsKey("smtpServer") && params.containsKey("smtpPort") &&
                    params.containsKey("emailSender") && params.containsKey("smtpPassword")) {

                String smtpHost = params.get("smtpServer");
                String smtpPort = params.get("smtpPort");
                String username = params.get("emailSender");
                String password = params.get("smtpPassword");
                boolean saveToFile = params.containsKey("saveToPropertiesFile");

                // Cập nhật cấu hình email
                if (saveToFile) {
                    emailConfigService.updateEmailConfiguration(smtpHost, smtpPort, username, password);
                } else {
                    emailConfigService.updateEmailConfigurationRuntimeOnly(smtpHost, smtpPort, username, password);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Tất cả cài đặt đã được lưu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi lưu cài đặt: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    @PostMapping("/email/test")
    public String testEmailConnection(@RequestParam String smtpServer,
            @RequestParam String smtpPort,
            @RequestParam String emailSender,
            @RequestParam String smtpPassword,
            RedirectAttributes redirectAttributes) {
        try {
            boolean success = emailConfigService.testEmailConnection(smtpServer, smtpPort, emailSender, smtpPassword);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Kết nối email thành công! Cấu hình đã được kiểm tra.");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Không thể kết nối email. Vui lòng kiểm tra lại cấu hình.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi test email: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    // Export documents as backup
    // @GetMapping("/backup/export")
    // public ResponseEntity<InputStreamResource> exportDocuments() {
    //     try {
    //         String documentsData = documentService.exportAllDocuments();

    //         LocalDateTime now = LocalDateTime.now();
    //         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    //         String filename = "documents_backup_" + now.format(formatter) + ".json";

    //         ByteArrayInputStream bis = new ByteArrayInputStream(documentsData.getBytes());

    //         // Update backup info
    //         DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    //         settings.put("lastBackup", now.format(displayFormatter));
    //         settings.put("backupSize", String.format("%.2f MB", documentsData.length() / 1024.0 / 1024.0));

    //         HttpHeaders headers = new HttpHeaders();
    //         headers.add("Content-Disposition", "attachment; filename=" + filename);

    //         return ResponseEntity.ok()
    //                 .headers(headers)
    //                 .contentType(MediaType.APPLICATION_JSON)
    //                 .body(new InputStreamResource(bis));

    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().build();
    //     }
    // // Export documents as backup
    // @GetMapping("/backup/export")
    // public ResponseEntity<InputStreamResource> exportDocuments() {
    // try {
    // String documentsData = documentService.exportAllDocuments();

    // LocalDateTime now = LocalDateTime.now();
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    // String filename = "documents_backup_" + now.format(formatter) + ".json";

    // ByteArrayInputStream bis = new
    // ByteArrayInputStream(documentsData.getBytes());

    // // Update backup info
    // DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy
    // HH:mm");
    // settings.put("lastBackup", now.format(displayFormatter));
    // settings.put("backupSize", String.format("%.2f MB", documentsData.length() /
    // 1024.0 / 1024.0));

    // HttpHeaders headers = new HttpHeaders();
    // headers.add("Content-Disposition", "attachment; filename=" + filename);

    // return ResponseEntity.ok()
    // .headers(headers)
    // .contentType(MediaType.APPLICATION_JSON)
    // .body(new InputStreamResource(bis));

    // } catch (Exception e) {
    // return ResponseEntity.badRequest().build();
    // }
    // }

    // // Import documents from backup
    // @PostMapping("/backup/import")
    // public String importDocuments(@RequestParam("backupFile") MultipartFile file,
    //         RedirectAttributes redirectAttributes) {
    //     try {
    //         if (file.isEmpty()) {
    //             redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file backup!");
    //             return "redirect:/admin/settings";
    //         }

    //         String result = documentService.importDocuments(file);
    //         redirectAttributes.addFlashAttribute("success", "Đã import thành công: " + result);
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi import: " + e.getMessage());
    //     }

    //     return "redirect:/admin/settings";
    // RedirectAttributes redirectAttributes) {
    // try {
    // if (file.isEmpty()) {
    // redirectAttributes.addFlashAttribute("error", "Vui lòng chọn file backup!");
    // return "redirect:/admin/settings";
    // }

    // String result = documentService.importDocuments(file);
    // redirectAttributes.addFlashAttribute("success", "Đã import thành công: " +
    // result);
    // } catch (Exception e) {
    // redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi import: " +
    // e.getMessage());
    // }

    // return "redirect:/admin/settings";
    // }

    @PostMapping("/maintenance/toggle")
    public String toggleMaintenance(@RequestParam(defaultValue = "false") boolean maintenanceMode,
            @RequestParam(required = false) String maintenanceMessage,
            RedirectAttributes redirectAttributes) {
        try {
            settings.put("maintenanceMode", maintenanceMode);
            if (maintenanceMessage != null) {
                settings.put("maintenanceMessage", maintenanceMessage);
            }

            String status = maintenanceMode ? "bật" : "tắt";
            redirectAttributes.addFlashAttribute("success", "Đã " + status + " chế độ bảo trì!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    @PostMapping("/reset")
    public String resetSettings(RedirectAttributes redirectAttributes) {
        try {
            initializeDefaultSettings();
            redirectAttributes.addFlashAttribute("success", "Đã khôi phục cài đặt mặc định!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi khôi phục: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    @GetMapping("/document-notifications/preview")
    public String previewDocumentNotification(Model model) {
        try {
            // Lấy một số tài liệu gần đây để preview
            java.util.List<com.fpoly.shared_learning_materials.domain.Document> recentDocs = documentService
                    .getRecentDocuments(3);

            String emailContent = generateTestNotificationContent(recentDocs);
            model.addAttribute("emailContent", emailContent);
            model.addAttribute("currentPage", "settings");
            return "admin/email-digest-preview";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi tạo preview: " + e.getMessage());
            return "redirect:/admin/settings";
        }
    }

    // Validate settings before saving
    private boolean validateSettings(Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            // Validate email settings
            if (params.containsKey("smtpPort")) {
                int port = Integer.parseInt(params.get("smtpPort"));
                if (port < 1 || port > 65535) {
                    redirectAttributes.addFlashAttribute("error", "SMTP Port phải từ 1-65535");
                    return false;
                }
            }

            // Validate session timeout
            if (params.containsKey("sessionTimeout")) {
                int timeout = Integer.parseInt(params.get("sessionTimeout"));
                if (timeout < 300 || timeout > 86400) {
                    redirectAttributes.addFlashAttribute("error", "Session timeout phải từ 300-86400 giây");
                    return false;
                }
            }

            // Validate max file size
            if (params.containsKey("maxFileSize")) {
                int size = Integer.parseInt(params.get("maxFileSize"));
                if (size < 1 || size > 1000) {
                    redirectAttributes.addFlashAttribute("error", "Kích thước file tối đa phải từ 1-1000 MB");
                    return false;
                }
            }

            // Validate password length
            if (params.containsKey("passwordMinLength")) {
                int length = Integer.parseInt(params.get("passwordMinLength"));
                if (length < 6 || length > 50) {
                    redirectAttributes.addFlashAttribute("error", "Độ dài mật khẩu phải từ 6-50 ký tự");
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("error", "Giá trị số không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    // Helper method to get document author name
    private String getDocumentAuthorName(com.fpoly.shared_learning_materials.domain.Document document) {
        try {
            return documentService.getDocumentAuthorName(document.getId());
        } catch (Exception e) {
            return "Không xác định";
        }
    }

    // Tạo nội dung email test cho document notifications
    private String generateTestNotificationContent(
            java.util.List<com.fpoly.shared_learning_materials.domain.Document> documents) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");

            // Header
            html.append(
                    "<div style='background: linear-gradient(135deg, #4CAF50 0%, #45a049 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0;'>");
            html.append("<h1 style='margin: 0; font-size: 24px;'>📚 [PREVIEW] Tài liệu mới trên EduShare</h1>");
            html.append("<p style='margin: 10px 0 0 0; opacity: 0.9;'>Email thông báo tài liệu mới</p>");
            html.append("<p style='margin: 5px 0 0 0; font-size: 14px; opacity: 0.8;'>").append(formatter.format(now))
                    .append("</p>");
            html.append("</div>");

            html.append("<div style='background: white; padding: 30px; border: 1px solid #e0e0e0; border-top: none;'>");

            if (documents == null || documents.isEmpty()) {
                html.append("<div style='text-align: center; padding: 40px; color: #666;'>");
                html.append("<h3>Không có tài liệu nào để hiển thị</h3>");
                html.append("<p>Đây là email preview. Khi có tài liệu mới, chúng sẽ được hiển thị ở đây.</p>");
                html.append("</div>");
            } else {
                // Thông báo số lượng tài liệu
                html.append(
                        "<div style='background: #e8f5e8; border: 2px solid #4caf50; border-radius: 8px; padding: 20px; margin-bottom: 20px; text-align: center;'>");
                html.append("<h2 style='color: #2e7d32; margin: 0 0 10px 0;'>🎉 Có ").append(documents.size())
                        .append(" tài liệu mới!</h2>");
                html.append("<p style='color: #2e7d32; margin: 0;'>Được thêm gần đây</p>");
                html.append("</div>");

                // Danh sách tài liệu
                html.append("<h3 style='color: #333; margin: 20px 0 15px 0;'>📋 Danh sách tài liệu:</h3>");

                for (com.fpoly.shared_learning_materials.domain.Document doc : documents) {
                    html.append(
                            "<div style='border: 1px solid #ddd; border-radius: 8px; padding: 15px; margin-bottom: 15px; background: #f9f9f9;'>");
                    html.append("<h4 style='margin: 0 0 8px 0; color: #333;'>").append(escapeHtml(doc.getTitle()))
                            .append("</h4>");

                    if (doc.getDescription() != null && !doc.getDescription().trim().isEmpty()) {
                        String description = doc.getDescription().length() > 100
                                ? doc.getDescription().substring(0, 100) + "..."
                                : doc.getDescription();
                        html.append("<p style='margin: 0 0 8px 0; color: #666; font-size: 14px;'>")
                                .append(escapeHtml(description)).append("</p>");
                    }

                    html.append("<div style='font-size: 12px; color: #888;'>");
                    html.append("<span>📅 ")
                            .append(doc.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                            .append("</span>");
                    String authorName = getDocumentAuthorName(doc);
                    html.append(" | <span>👤 ").append(escapeHtml(authorName)).append("</span>");
                    html.append("</div>");
                    html.append("</div>");
                }
            }

            html.append("</div>");

            // Footer
            html.append(
                    "<div style='background: #f8f9fa; padding: 20px; text-align: center; border-radius: 0 0 8px 8px; color: #666; font-size: 12px;'>");
            html.append("<p style='margin: 0;'>📧 Email preview từ hệ thống EduShare</p>");
            html.append("<p style='margin: 5px 0 0 0;'>Truy cập website để xem chi tiết tài liệu</p>");
            html.append("</div>");

            html.append("</div>");

            return html.toString();

        } catch (Exception e) {
            return "<div style='color: red; padding: 20px;'><h3>Lỗi tạo nội dung email</h3><p>" + e.getMessage()
                    + "</p></div>";
        }
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Get setting by key with default value
    public static Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    // Get setting as string
    public static String getStringSetting(String key, String defaultValue) {
        Object value = settings.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    // Get setting as boolean
    public static boolean getBooleanSetting(String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    // Get setting as integer
    public static int getIntegerSetting(String key, int defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @GetMapping("/export")
    @ResponseBody
    public Map<String, Object> exportSettings() {
        return new HashMap<>(settings);
    }

    // API endpoint để lấy settings công khai cho frontend
    @GetMapping("/public")
    @ResponseBody
    public Map<String, Object> getPublicSettings() {
        Map<String, Object> publicSettings = new HashMap<>();

        // Chỉ expose những settings an toàn cho public
        publicSettings.put("siteName", settings.get("siteName"));
        publicSettings.put("siteDescription", settings.get("siteDescription"));
        publicSettings.put("siteLogo", settings.get("siteLogo"));
        publicSettings.put("siteFavicon", settings.get("siteFavicon"));
        publicSettings.put("language", settings.get("language"));
        publicSettings.put("timezone", settings.get("timezone"));
        publicSettings.put("dateFormat", settings.get("dateFormat"));
        publicSettings.put("darkMode", settings.get("darkMode"));
        publicSettings.put("showViewCount", settings.get("showViewCount"));
        publicSettings.put("allowRegistration", settings.get("allowRegistration"));
        publicSettings.put("documentsPerPage", settings.get("documentsPerPage"));
        publicSettings.put("maxFileSize", settings.get("maxFileSize"));
        publicSettings.put("allowedFileTypes", settings.get("allowedFileTypes"));
        publicSettings.put("maintenanceMode", settings.get("maintenanceMode"));
        publicSettings.put("maintenanceMessage", settings.get("maintenanceMessage"));

        return publicSettings;
    }

    // System health check
    @GetMapping("/health")
    @ResponseBody
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Check database connection
            long documentCount = documentService.getTotalDocuments();
            health.put("database", "OK");
            health.put("documentCount", documentCount);

            // Check email configuration
            Map<String, String> emailConfig = emailConfigService.getCurrentEmailConfig();
            boolean emailConfigured = emailConfig.get("host") != null && !emailConfig.get("host").isEmpty();
            health.put("email", emailConfigured ? "OK" : "NOT_CONFIGURED");

            // Check maintenance mode
            health.put("maintenanceMode", settings.get("maintenanceMode"));

            // System info
            health.put("timestamp", LocalDateTime.now());
            health.put("uptime", "N/A");

            health.put("status", "HEALTHY");

        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
        }

        return health;
    }

    @PostMapping("/cache/clear")
    public String clearCache(RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("success", "Đã xóa cache thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa cache: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    // System statistics
    @GetMapping("/stats")
    @ResponseBody
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Document statistics
            stats.put("totalDocuments", documentService.getTotalDocuments());
            stats.put("totalUsers", "N/A");
            stats.put("totalDownloads", "N/A");

            // System resources
            Runtime runtime = Runtime.getRuntime();
            stats.put("memoryUsed", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
            stats.put("memoryTotal", runtime.totalMemory() / 1024 / 1024);
            stats.put("memoryFree", runtime.freeMemory() / 1024 / 1024);

            // Settings info
            stats.put("settingsCount", settings.size());
            stats.put("lastBackup", settings.get("lastBackup"));

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }

        return stats;
    }

}