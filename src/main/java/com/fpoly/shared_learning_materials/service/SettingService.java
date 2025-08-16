package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Setting;
import com.fpoly.shared_learning_materials.repository.SettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SettingService {

    private final SettingRepository settingRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public SettingService(SettingRepository settingRepository, ObjectMapper objectMapper) {
        this.settingRepository = settingRepository;
        this.objectMapper = objectMapper;
    }

    // Lấy giá trị setting theo key
    public String getSetting(String key) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .orElse(null);
    }

    // Lấy giá trị setting với default value
    public String getSetting(String key, String defaultValue) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .orElse(defaultValue);
    }

    // Lấy setting dạng boolean
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSetting(key);
        if (value == null) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    // Lấy setting dạng integer
    public int getIntSetting(String key, int defaultValue) {
        String value = getSetting(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Lấy setting dạng JSON object
    public <T> T getJsonSetting(String key, Class<T> clazz, T defaultValue) {
        String value = getSetting(key);
        if (value == null) return defaultValue;
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JsonProcessingException e) {
            return defaultValue;
        }
    }

    // Lưu setting
    @Transactional
    public void setSetting(String key, String value, String type, String category, String description, boolean isPublic) {
        Optional<Setting> existingSetting = settingRepository.findByKey(key);
        
        Setting setting;
        if (existingSetting.isPresent()) {
            setting = existingSetting.get();
            setting.setValue(value);
            setting.setType(type);
            setting.setCategory(category);
            setting.setDescription(description);
            setting.setIsPublic(isPublic);
        } else {
            setting = new Setting();
            setting.setKey(key);
            setting.setValue(value);
            setting.setType(type);
            setting.setCategory(category);
            setting.setDescription(description);
            setting.setIsPublic(isPublic);
        }
        
        settingRepository.save(setting);
    }

    // Lưu setting đơn giản
    @Transactional
    public void setSetting(String key, String value) {
        setSetting(key, value, "string", "general", null, false);
    }

    // Lưu setting boolean
    @Transactional
    public void setBooleanSetting(String key, boolean value) {
        setSetting(key, String.valueOf(value), "boolean", "general", null, false);
    }

    // Lưu setting JSON
    @Transactional
    public void setJsonSetting(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            setSetting(key, jsonValue, "json", "general", null, false);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing setting value", e);
        }
    }

    // Lấy tất cả settings theo category
    public List<Setting> getSettingsByCategory(String category) {
        return settingRepository.findByCategoryOrderByKey(category);
    }

    // Lấy tất cả settings dạng Map
    public Map<String, String> getAllSettingsAsMap() {
        List<Setting> settings = settingRepository.findAll();
        Map<String, String> settingsMap = new HashMap<>();
        for (Setting setting : settings) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return settingsMap;
    }

    // Lấy settings theo category dạng Map
    public Map<String, String> getSettingsByCategoryAsMap(String category) {
        List<Setting> settings = settingRepository.findByCategory(category);
        Map<String, String> settingsMap = new HashMap<>();
        for (Setting setting : settings) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return settingsMap;
    }

    // Lấy tất cả settings public
    public Map<String, String> getPublicSettings() {
        List<Setting> settings = settingRepository.findByIsPublicTrue();
        Map<String, String> settingsMap = new HashMap<>();
        for (Setting setting : settings) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return settingsMap;
    }

    // Cập nhật nhiều settings cùng lúc
    @Transactional
    public void updateSettings(Map<String, String> settingsMap, String category) {
        for (Map.Entry<String, String> entry : settingsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Xác định type dựa trên value
            String type = determineType(value);
            
            setSetting(key, value, type, category, null, false);
        }
    }

    // Xóa setting
    @Transactional
    public void deleteSetting(String key) {
        settingRepository.deleteByKey(key);
    }

    // Khởi tạo settings mặc định
    @Transactional
    public void initializeDefaultSettings() {
        // General settings
        setDefaultSetting("site.name", "EduShare - Chia sẻ tài liệu giáo dục", "string", "general", "Tên website", true);
        setDefaultSetting("site.description", "Nền tảng chia sẻ tài liệu giáo dục hàng đầu Việt Nam", "string", "general", "Mô tả website", true);
        setDefaultSetting("site.email", "admin@edushare.vn", "string", "general", "Email liên hệ", true);
        setDefaultSetting("site.phone", "(+84) 123 456 789", "string", "general", "Số điện thoại", true);
        setDefaultSetting("site.address", "123 Đường ABC, Quận 1, TP. Hồ Chí Minh", "string", "general", "Địa chỉ", true);
        setDefaultSetting("site.language", "vi", "string", "general", "Ngôn ngữ mặc định", true);
        setDefaultSetting("site.timezone", "Asia/Ho_Chi_Minh", "string", "general", "Múi giờ", false);
        setDefaultSetting("site.items_per_page", "20", "number", "general", "Số tài liệu mỗi trang", false);
        setDefaultSetting("site.date_format", "dd/MM/yyyy", "string", "general", "Định dạng ngày", false);
        setDefaultSetting("site.dark_mode_enabled", "true", "boolean", "general", "Cho phép dark mode", true);
        setDefaultSetting("site.show_view_count", "true", "boolean", "general", "Hiển thị số lượt xem", true);

        // User settings
        setDefaultSetting("user.registration_enabled", "true", "boolean", "users", "Cho phép đăng ký mới", false);
        setDefaultSetting("user.email_verification_required", "true", "boolean", "users", "Yêu cầu xác thực email", false);
        setDefaultSetting("user.phone_verification_required", "false", "boolean", "users", "Yêu cầu xác thực số điện thoại", false);
        setDefaultSetting("user.manual_approval_required", "true", "boolean", "users", "Duyệt tài khoản thủ công", false);
        setDefaultSetting("user.min_password_length", "8", "number", "users", "Mật khẩu tối thiểu", false);
        setDefaultSetting("user.default_download_permission", "true", "boolean", "users", "Quyền tải xuống mặc định", false);
        setDefaultSetting("user.default_upload_permission", "true", "boolean", "users", "Quyền đăng tài liệu mặc định", false);
        setDefaultSetting("user.default_comment_permission", "true", "boolean", "users", "Quyền bình luận mặc định", false);

        // Content settings
        setDefaultSetting("content.max_file_size", "50", "number", "content", "Kích thước file tối đa (MB)", false);
        setDefaultSetting("content.max_concurrent_uploads", "5", "number", "content", "Số file upload đồng thời", false);
        setDefaultSetting("content.allowed_file_types", "pdf,doc,docx,ppt,pptx,xls,xlsx", "string", "content", "Định dạng file cho phép", false);
        setDefaultSetting("content.virus_scan_enabled", "true", "boolean", "content", "Quét virus tự động", false);
        setDefaultSetting("content.thumbnail_generation_enabled", "true", "boolean", "content", "Tạo thumbnail preview", false);
        setDefaultSetting("content.moderation_mode", "manual", "string", "content", "Chế độ kiểm duyệt", false);
        setDefaultSetting("content.sensitive_word_check", "true", "boolean", "content", "Kiểm tra từ khóa nhạy cảm", false);
        setDefaultSetting("content.plagiarism_detection", "true", "boolean", "content", "Phát hiện plagiarism", false);
        setDefaultSetting("content.notify_author_on_approval", "false", "boolean", "content", "Thông báo tác giả khi duyệt", false);

        System.out.println("Default settings initialized successfully!");
    }

    // Helper method để set default setting nếu chưa tồn tại
    private void setDefaultSetting(String key, String value, String type, String category, String description, boolean isPublic) {
        if (!settingRepository.existsByKey(key)) {
            setSetting(key, value, type, category, description, isPublic);
        }
    }

    // Helper method để xác định type của value
    private String determineType(String value) {
        if (value == null) return "string";
        
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return "boolean";
        }
        
        try {
            Integer.parseInt(value);
            return "number";
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        if (value.startsWith("{") || value.startsWith("[")) {
            return "json";
        }
        
        return "string";
    }
}