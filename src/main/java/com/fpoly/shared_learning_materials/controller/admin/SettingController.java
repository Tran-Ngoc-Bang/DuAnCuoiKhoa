package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Setting;
import com.fpoly.shared_learning_materials.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class SettingController {

    private final SettingService settingService;

    @Autowired
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    // Hiển thị trang settings
    @GetMapping
    public String showSettings(Model model) {
        // Lấy tất cả settings theo category
        Map<String, List<Setting>> settingsByCategory = new HashMap<>();
        settingsByCategory.put("general", settingService.getSettingsByCategory("general"));
        settingsByCategory.put("users", settingService.getSettingsByCategory("users"));
        settingsByCategory.put("content", settingService.getSettingsByCategory("content"));
        settingsByCategory.put("notifications", settingService.getSettingsByCategory("notifications"));
        settingsByCategory.put("security", settingService.getSettingsByCategory("security"));
        settingsByCategory.put("integrations", settingService.getSettingsByCategory("integrations"));
        settingsByCategory.put("backup", settingService.getSettingsByCategory("backup"));
        settingsByCategory.put("advanced", settingService.getSettingsByCategory("advanced"));

        model.addAttribute("settingsByCategory", settingsByCategory);

        // Lấy settings dạng Map để dễ sử dụng trong template
        model.addAttribute("settings", settingService.getAllSettingsAsMap());

        return "admin/settings";
    }

    // API để lấy settings theo category
    @GetMapping("/api/{category}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSettingsByCategory(@PathVariable String category) {
        Map<String, String> settings = settingService.getSettingsByCategoryAsMap(category);
        return ResponseEntity.ok(settings);
    }

    // API để lấy tất cả settings
    @GetMapping("/api/all")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getAllSettings() {
        Map<String, String> settings = settingService.getAllSettingsAsMap();
        return ResponseEntity.ok(settings);
    }

    // Cập nhật settings chung
    @PostMapping("/general")
    public String updateGeneralSettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            // Lọc ra các settings thuộc category general
            Map<String, String> generalSettings = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("site.")) {
                    generalSettings.put(key, entry.getValue());
                }
            }

            settingService.updateSettings(generalSettings, "general");
            redirectAttributes.addFlashAttribute("success", "Cài đặt chung đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật cài đặt: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    // Cập nhật settings người dùng
    @PostMapping("/users")
    public String updateUserSettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> userSettings = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("user.")) {
                    userSettings.put(key, entry.getValue());
                }
            }

            settingService.updateSettings(userSettings, "users");
            redirectAttributes.addFlashAttribute("success", "Cài đặt người dùng đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật cài đặt: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    // Cập nhật settings nội dung
    @PostMapping("/content")
    public String updateContentSettings(@RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes) {
        try {
            Map<String, String> contentSettings = new HashMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("content.")) {
                    contentSettings.put(key, entry.getValue());
                }
            }

            settingService.updateSettings(contentSettings, "content");
            redirectAttributes.addFlashAttribute("success", "Cài đặt nội dung đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật cài đặt: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    // API để cập nhật một setting cụ thể
    @PostMapping("/api/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateSetting(@RequestParam String key,
            @RequestParam String value,
            @RequestParam(required = false) String category) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (category == null) {
                category = "general";
            }

            String type = determineType(value);
            settingService.setSetting(key, value, type, category, null, false);

            response.put("success", true);
            response.put("message", "Cài đặt đã được cập nhật thành công");
            response.put("key", key);
            response.put("value", value);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để reset settings về mặc định
    @PostMapping("/api/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetSettings(@RequestParam(required = false) String category) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (category != null) {
                // Reset settings của category cụ thể
                List<Setting> settings = settingService.getSettingsByCategory(category);
                for (Setting setting : settings) {
                    settingService.deleteSetting(setting.getKey());
                }
            }

            // Khởi tạo lại settings mặc định
            settingService.initializeDefaultSettings();

            response.put("success", true);
            response.put("message", "Cài đặt đã được reset về mặc định");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API để export settings
    @GetMapping("/api/export")
    @ResponseBody
    public ResponseEntity<Map<String, String>> exportSettings() {
        Map<String, String> settings = settingService.getAllSettingsAsMap();
        return ResponseEntity.ok(settings);
    }

    // API để import settings
    @PostMapping("/api/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importSettings(@RequestBody Map<String, String> settings) {
        Map<String, Object> response = new HashMap<>();

        try {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String category = extractCategoryFromKey(key);
                String type = determineType(value);

                settingService.setSetting(key, value, type, category, null, false);
            }

            response.put("success", true);
            response.put("message", "Import cài đặt thành công");
            response.put("imported", settings.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Helper methods
    private String determineType(String value) {
        if (value == null)
            return "string";

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

    private String extractCategoryFromKey(String key) {
        if (key.startsWith("site."))
            return "general";
        if (key.startsWith("user."))
            return "users";
        if (key.startsWith("content."))
            return "content";
        if (key.startsWith("notification."))
            return "notifications";
        if (key.startsWith("security."))
            return "security";
        if (key.startsWith("integration."))
            return "integrations";
        if (key.startsWith("backup."))
            return "backup";
        return "advanced";
    }
}