package com.fpoly.shared_learning_materials.util;

import java.util.HashMap;
import java.util.Map;

public class CategoryIconMapper {

    private static final Map<String, String> CATEGORY_ICONS = new HashMap<>();

    // Mảng các icon classes để chọn ngẫu nhiên
    private static final String[] ICON_CLASSES = {
            "fas fa-laptop-code", "fas fa-code", "fas fa-desktop", "fas fa-mobile-alt",
            "fas fa-chart-line", "fas fa-bullhorn", "fas fa-coins", "fas fa-chart-bar",
            "fas fa-language", "fas fa-globe", "fas fa-book", "fas fa-graduation-cap",
            "fas fa-cogs", "fas fa-cog", "fas fa-bolt", "fas fa-wrench",
            "fas fa-users", "fas fa-brain", "fas fa-user-friends", "fas fa-handshake",
            "fas fa-heartbeat", "fas fa-stethoscope", "fas fa-pills", "fas fa-hospital",
            "fas fa-gavel", "fas fa-balance-scale", "fas fa-landmark", "fas fa-file-contract",
            "fas fa-palette", "fas fa-music", "fas fa-paint-brush", "fas fa-theater-masks",
            "fas fa-atom", "fas fa-flask", "fas fa-dna", "fas fa-microscope",
            "fas fa-seedling", "fas fa-tractor", "fas fa-leaf", "fas fa-tree",
            "fas fa-folder", "fas fa-archive", "fas fa-box", "fas fa-cube"
    };

    // Mảng các màu gradient để chọn ngẫu nhiên
    private static final String[] GRADIENT_COLORS = {
            "#4361ee,#3a0ca3", "#f72585,#b5179e", "#4cc9f0,#4361ee", "#fb8500,#ffb703",
            "#8338ec,#5e17eb", "#06d6a0,#1b9aaa", "#073b4c,#118ab2", "#ffbe0b,#fb8500",
            "#ff006e,#8338ec", "#8338ec,#3a0ca3", "#38b000,#06d6a0", "#31cb9e,#06d6a0",
            "#6b7280,#9ca3af", "#ef4444,#dc2626", "#10b981,#059669", "#8b5cf6,#7c3aed",
            "#f59e0b,#d97706", "#ec4899,#db2777", "#06b6d4,#0891b2", "#84cc16,#65a30d"
    };

    static {
        // Technology & IT
        CATEGORY_ICONS.put("công nghệ thông tin", "fas fa-laptop-code");
        CATEGORY_ICONS.put("it", "fas fa-laptop-code");
        CATEGORY_ICONS.put("technology", "fas fa-laptop-code");
        CATEGORY_ICONS.put("programming", "fas fa-code");
        CATEGORY_ICONS.put("software", "fas fa-desktop");

        // Business & Economics
        CATEGORY_ICONS.put("kinh tế", "fas fa-chart-line");
        CATEGORY_ICONS.put("quản trị", "fas fa-chart-line");
        CATEGORY_ICONS.put("business", "fas fa-chart-line");
        CATEGORY_ICONS.put("marketing", "fas fa-bullhorn");
        CATEGORY_ICONS.put("finance", "fas fa-coins");

        // Language
        CATEGORY_ICONS.put("ngoại ngữ", "fas fa-language");
        CATEGORY_ICONS.put("language", "fas fa-language");
        CATEGORY_ICONS.put("tiếng anh", "fas fa-language");
        CATEGORY_ICONS.put("english", "fas fa-language");

        // Engineering
        CATEGORY_ICONS.put("kỹ thuật", "fas fa-cogs");
        CATEGORY_ICONS.put("engineering", "fas fa-cogs");
        CATEGORY_ICONS.put("mechanical", "fas fa-cog");
        CATEGORY_ICONS.put("electrical", "fas fa-bolt");

        // Social Sciences
        CATEGORY_ICONS.put("khoa học xã hội", "fas fa-users");
        CATEGORY_ICONS.put("social", "fas fa-users");
        CATEGORY_ICONS.put("psychology", "fas fa-brain");
        CATEGORY_ICONS.put("sociology", "fas fa-users");

        // Health & Medicine
        CATEGORY_ICONS.put("y dược", "fas fa-heartbeat");
        CATEGORY_ICONS.put("health", "fas fa-heartbeat");
        CATEGORY_ICONS.put("medicine", "fas fa-stethoscope");
        CATEGORY_ICONS.put("medical", "fas fa-heartbeat");

        // Law
        CATEGORY_ICONS.put("luật", "fas fa-gavel");
        CATEGORY_ICONS.put("law", "fas fa-gavel");
        CATEGORY_ICONS.put("legal", "fas fa-balance-scale");

        // Education
        CATEGORY_ICONS.put("giáo dục", "fas fa-graduation-cap");
        CATEGORY_ICONS.put("education", "fas fa-graduation-cap");
        CATEGORY_ICONS.put("teaching", "fas fa-chalkboard-teacher");

        // Arts
        CATEGORY_ICONS.put("nghệ thuật", "fas fa-palette");
        CATEGORY_ICONS.put("art", "fas fa-palette");
        CATEGORY_ICONS.put("music", "fas fa-music");
        CATEGORY_ICONS.put("design", "fas fa-paint-brush");

        // Science
        CATEGORY_ICONS.put("khoa học", "fas fa-atom");
        CATEGORY_ICONS.put("science", "fas fa-atom");
        CATEGORY_ICONS.put("physics", "fas fa-atom");
        CATEGORY_ICONS.put("chemistry", "fas fa-flask");
        CATEGORY_ICONS.put("biology", "fas fa-dna");

        // Agriculture
        CATEGORY_ICONS.put("nông nghiệp", "fas fa-seedling");
        CATEGORY_ICONS.put("agriculture", "fas fa-seedling");
        CATEGORY_ICONS.put("farming", "fas fa-tractor");

        // Environment
        CATEGORY_ICONS.put("môi trường", "fas fa-leaf");
        CATEGORY_ICONS.put("environment", "fas fa-leaf");
        CATEGORY_ICONS.put("ecology", "fas fa-tree");

        // Default icon
        CATEGORY_ICONS.put("default", "fas fa-folder");
    }

    /**
     * Get icon class for a category name
     */
    public static String getIconForCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return CATEGORY_ICONS.get("default");
        }

        String normalizedName = categoryName.toLowerCase().trim();

        // Try exact match first
        if (CATEGORY_ICONS.containsKey(normalizedName)) {
            return CATEGORY_ICONS.get(normalizedName);
        }

        // Try partial match
        for (Map.Entry<String, String> entry : CATEGORY_ICONS.entrySet()) {
            if (normalizedName.contains(entry.getKey()) || entry.getKey().contains(normalizedName)) {
                return entry.getValue();
            }
        }

        // Return dynamic icon based on category name hash
        return getDynamicIcon(normalizedName);
    }

    /**
     * Get dynamic icon based on category name hash
     */
    private static String getDynamicIcon(String categoryName) {
        int hash = Math.abs(categoryName.hashCode());
        int index = hash % ICON_CLASSES.length;
        return ICON_CLASSES[index];
    }

    /**
     * Get CSS class for category icon styling (dynamic)
     */
    public static String getIconClassForCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "dynamic-icon";
        }

        String normalizedName = categoryName.toLowerCase().trim();

        // Map to CSS classes for known categories
        if (normalizedName.contains("công nghệ") || normalizedName.contains("it")
                || normalizedName.contains("technology")) {
            return "it-icon";
        } else if (normalizedName.contains("kinh tế") || normalizedName.contains("business")
                || normalizedName.contains("quản trị")) {
            return "business-icon";
        } else if (normalizedName.contains("ngoại ngữ") || normalizedName.contains("language")) {
            return "language-icon";
        } else if (normalizedName.contains("kỹ thuật") || normalizedName.contains("engineering")) {
            return "engineering-icon";
        } else if (normalizedName.contains("xã hội") || normalizedName.contains("social")) {
            return "social-icon";
        } else if (normalizedName.contains("y dược") || normalizedName.contains("health")) {
            return "health-icon";
        } else if (normalizedName.contains("luật") || normalizedName.contains("law")) {
            return "law-icon";
        } else if (normalizedName.contains("giáo dục") || normalizedName.contains("education")) {
            return "education-icon";
        } else if (normalizedName.contains("nghệ thuật") || normalizedName.contains("art")) {
            return "art-icon";
        } else if (normalizedName.contains("khoa học") || normalizedName.contains("science")) {
            return "science-icon";
        } else if (normalizedName.contains("nông nghiệp") || normalizedName.contains("agriculture")) {
            return "agriculture-icon";
        } else if (normalizedName.contains("môi trường") || normalizedName.contains("environment")) {
            return "environment-icon";
        }

        // Return dynamic class for unknown categories
        return "dynamic-icon";
    }

    /**
     * Get dynamic gradient colors for category
     */
    public static String getDynamicGradient(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return GRADIENT_COLORS[0];
        }

        int hash = Math.abs(categoryName.hashCode());
        int index = hash % GRADIENT_COLORS.length;
        return GRADIENT_COLORS[index];
    }

    /**
     * Get dynamic icon style attributes for inline CSS
     */
    public static String getDynamicIconStyle(String categoryName) {
        String gradient = getDynamicGradient(categoryName);
        String[] colors = gradient.split(",");

        return String.format(
                "background: linear-gradient(135deg, %s, %s); " +
                        "color: white; " +
                        "box-shadow: 0 10px 20px %s25;",
                colors[0], colors[1], colors[0]);
    }
}