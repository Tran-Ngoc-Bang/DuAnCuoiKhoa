package com.fpoly.shared_learning_materials.util;

import java.text.Normalizer;
import java.util.UUID;

public class SlugUtils {

    public static String generateSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }

        // Handle special programming language names first
        if ("C#".equals(text)) {
            return "c-sharp";
        } else if ("C++".equals(text)) {
            return "cpp";
        } else if ("C".equals(text)) {
            return "c-language";
        }

        // Normalize Vietnamese characters
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);

        // Remove diacritics and convert to lowercase
        String slug = normalized
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .trim();

        if (slug.isEmpty()) {
            slug = UUID.randomUUID().toString().substring(0, 8);
        }

        return slug;
    }
}