package com.fpoly.shared_learning_materials.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class EmailConfigService {

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Cập nhật cấu hình email từ admin settings (bao gồm lưu vào file)
     */
    public void updateEmailConfiguration(String smtpHost, String smtpPort,
            String username, String password) {
        try {
            // 1. Cập nhật runtime
            updateEmailConfigurationRuntimeOnly(smtpHost, smtpPort, username, password);

            // 2. Lưu vĩnh viễn vào application.properties
            updateApplicationProperties(smtpHost, smtpPort, username, password);

        } catch (Exception e) {
            System.err.println("Error updating email configuration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật cấu hình email: " + e.getMessage());
        }
    }

    /**
     * Cập nhật cấu hình email chỉ trong runtime (không lưu vào file)
     */
    public void updateEmailConfigurationRuntimeOnly(String smtpHost, String smtpPort,
            String username, String password) {
        try {
            // 1. Cập nhật properties trong runtime
            Map<String, Object> emailProps = new HashMap<>();
            emailProps.put("spring.mail.host", smtpHost);
            emailProps.put("spring.mail.port", smtpPort);
            emailProps.put("spring.mail.username", username);
            emailProps.put("spring.mail.password", password);

            // Thêm property source mới
            MapPropertySource emailPropertySource = new MapPropertySource("emailConfig", emailProps);
            environment.getPropertySources().addFirst(emailPropertySource);

            // 2. Cập nhật JavaMailSender
            if (mailSender instanceof JavaMailSenderImpl) {
                JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;

                mailSenderImpl.setHost(smtpHost);
                mailSenderImpl.setPort(Integer.parseInt(smtpPort));
                mailSenderImpl.setUsername(username);
                mailSenderImpl.setPassword(password);

                // Cập nhật properties
                Properties props = mailSenderImpl.getJavaMailProperties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");

                System.out.println("✅ Email configuration updated (runtime only):");
                System.out.println("📧 Host: " + smtpHost + ", Port: " + smtpPort + ", User: " + username);
            }

        } catch (Exception e) {
            System.err.println("Error updating email configuration (runtime): " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Không thể cập nhật cấu hình email: " + e.getMessage());
        }
    }

    /**
     * Cập nhật file application.properties
     */
    private void updateApplicationProperties(String smtpHost, String smtpPort,
            String username, String password) {
        try {
            String propertiesPath = "src/main/resources/application.properties";
            java.io.File file = new java.io.File(propertiesPath);

            if (!file.exists()) {
                System.err.println("application.properties file not found at: " + propertiesPath);
                return;
            }

            // Backup file trước khi thay đổi
            backupPropertiesFile(file);

            // Đọc nội dung file hiện tại (giữ nguyên format và comments)
            java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
            java.util.List<String> updatedLines = new java.util.ArrayList<>();

            boolean hostUpdated = false, portUpdated = false, usernameUpdated = false, passwordUpdated = false;

            for (String line : lines) {
                if (line.startsWith("spring.mail.host=")) {
                    updatedLines.add("spring.mail.host=" + smtpHost);
                    hostUpdated = true;
                } else if (line.startsWith("spring.mail.port=")) {
                    updatedLines.add("spring.mail.port=" + smtpPort);
                    portUpdated = true;
                } else if (line.startsWith("spring.mail.username=")) {
                    updatedLines.add("spring.mail.username=" + username);
                    usernameUpdated = true;
                } else if (line.startsWith("spring.mail.password=")) {
                    updatedLines.add("spring.mail.password=" + password);
                    passwordUpdated = true;
                } else {
                    updatedLines.add(line);
                }
            }

            // Thêm các properties chưa có
            if (!hostUpdated)
                updatedLines.add("spring.mail.host=" + smtpHost);
            if (!portUpdated)
                updatedLines.add("spring.mail.port=" + smtpPort);
            if (!usernameUpdated)
                updatedLines.add("spring.mail.username=" + username);
            if (!passwordUpdated)
                updatedLines.add("spring.mail.password=" + password);

            // Ghi lại file
            java.nio.file.Files.write(file.toPath(), updatedLines);

            System.out.println("✅ Email configuration saved to application.properties");
            System.out.println("📧 Host: " + smtpHost + ", Port: " + smtpPort + ", User: " + username);

        } catch (Exception e) {
            System.err.println("❌ Error updating application.properties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Backup file application.properties
     */
    private void backupPropertiesFile(java.io.File originalFile) {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupPath = originalFile.getParent() + "/application.properties.backup." + timestamp;

            java.nio.file.Files.copy(originalFile.toPath(),
                    java.nio.file.Paths.get(backupPath));

            System.out.println("📋 Backup created: " + backupPath);

        } catch (Exception e) {
            System.err.println("⚠️ Could not create backup: " + e.getMessage());
        }
    }

    /**
     * Test kết nối email với cấu hình mới
     */
    public boolean testEmailConnection(String smtpHost, String smtpPort,
            String username, String password) {
        try {
            JavaMailSenderImpl testSender = new JavaMailSenderImpl();
            testSender.setHost(smtpHost);
            testSender.setPort(Integer.parseInt(smtpPort));
            testSender.setUsername(username);
            testSender.setPassword(password);

            Properties props = testSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");

            // Test connection
            testSender.testConnection();

            System.out.println("Email connection test successful");
            return true;

        } catch (Exception e) {
            System.err.println("Email connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gửi email với nội dung HTML
     */
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        System.out.println("=== SENDING EMAIL ===");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        
        try {
            // Kiểm tra cấu hình email
            Map<String, String> config = getCurrentEmailConfig();
            System.out.println("Email config - Host: " + config.get("host"));
            System.out.println("Email config - Port: " + config.get("port"));
            System.out.println("Email config - Username: " + config.get("username"));
            System.out.println("Email config - Password length: " + (config.get("password") != null ? config.get("password").length() : 0));
            
            if (config.get("host") == null || config.get("host").isEmpty()) {
                System.err.println("❌ Email configuration not found");
                return false;
            }

            if (config.get("username") == null || config.get("username").isEmpty()) {
                System.err.println("❌ Email username not configured");
                return false;
            }

            if (config.get("password") == null || config.get("password").isEmpty()) {
                System.err.println("❌ Email password not configured");
                return false;
            }

            System.out.println("Creating MimeMessage...");
            // Tạo message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            System.out.println("Setting email properties...");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content
            helper.setFrom(config.get("username"));

            System.out.println("Sending email...");
            // Gửi email
            mailSender.send(message);

            System.out.println("✅ Email sent successfully to: " + to);
            System.out.println("📧 Subject: " + subject);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            System.err.println("❌ Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        } finally {
            System.out.println("=== EMAIL SENDING COMPLETE ===");
        }
    }

    /**
     * Lấy cấu hình email hiện tại
     */
    public Map<String, String> getCurrentEmailConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("host", environment.getProperty("spring.mail.host", "smtp.gmail.com"));
        config.put("port", environment.getProperty("spring.mail.port", "587"));
        config.put("username", environment.getProperty("spring.mail.username", ""));
        config.put("password", environment.getProperty("spring.mail.password", ""));
        return config;
    }
}