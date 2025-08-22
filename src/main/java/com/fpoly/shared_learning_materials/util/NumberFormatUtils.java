package com.fpoly.shared_learning_materials.util;

public class NumberFormatUtils {

    /**
     * Format số lượng documents để hiển thị đẹp hơn
     * Ví dụ: 1200 -> "1.2K", 1000000 -> "1M"
     */
    public static String formatDocumentCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1000000) {
            double k = count / 1000.0;
            if (k == (long) k) {
                return String.format("%.0fK", k);
            } else {
                return String.format("%.1fK", k);
            }
        } else {
            double m = count / 1000000.0;
            if (m == (long) m) {
                return String.format("%.0fM", m);
            } else {
                return String.format("%.1fM", m);
            }
        }
    }

    /**
     * Format số lượng documents với dấu "+" nếu có nhiều hơn
     */
    public static String formatDocumentCountWithPlus(long count) {
        return formatDocumentCount(count) + "+";
    }

    /**
     * Format số lượng views/downloads để hiển thị đẹp hơn
     * Ví dụ: 1200 -> "1.2K", 1000000 -> "1M"
     */
    public static String formatViewsDownloads(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        } else if (count < 1000000) {
            double k = count / 1000.0;
            if (k == (long) k) {
                return String.format("%.0fK", k);
            } else {
                return String.format("%.1fK", k);
            }
        } else {
            double m = count / 1000000.0;
            if (m == (long) m) {
                return String.format("%.0fM", m);
            } else {
                return String.format("%.1fM", m);
            }
        }
    }

    /**
     * Format số lượng views/downloads với null check
     * Overload method để xử lý Long có thể null
     */
    public static String formatViewsDownloads(Long count) {
        if (count == null) {
            return "0";
        }
        return formatViewsDownloads(count.longValue());
    }
}