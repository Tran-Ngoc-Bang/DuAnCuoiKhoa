package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserActivityRepository;
import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.UserActivity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final TransactionRepository transactionRepository;
    private final UserActivityRepository userActivityRepository;

    @Autowired
    public DashboardService(UserRepository userRepository,
            DocumentRepository documentRepository,
            TransactionRepository transactionRepository,
            UserActivityRepository userActivityRepository) {
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.transactionRepository = transactionRepository;
        this.userActivityRepository = userActivityRepository;
    }

    public Map<String, Object> getBasicStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Tổng số người dùng
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);

            // Tổng số tài liệu
            long totalDocuments = documentRepository.count();
            stats.put("totalDocuments", totalDocuments);

            // Tổng lượt tải
            long totalDownloads = getTotalDownloads();
            stats.put("totalDownloads", totalDownloads);

            // Tổng doanh thu
            long totalRevenue = getTotalRevenue();
            stats.put("totalRevenue", totalRevenue);

            // Phân loại tài liệu theo giá
            Map<String, Object> documentStats = getDocumentStats();
            stats.put("documentStats", documentStats);

        } catch (Exception e) {
            // Fallback nếu có lỗi
            stats.put("totalUsers", 0L);
            stats.put("totalDocuments", 0L);
            stats.put("totalDownloads", 0L);
            stats.put("totalRevenue", 0L);
            stats.put("documentStats", getEmptyDocumentStats());

            System.err.println("Error getting basic stats: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Object> getDocumentStats() {
        Map<String, Object> docStats = new HashMap<>();

        try {
            // Đếm tài liệu miễn phí
            long freeDocuments = documentRepository.countFreeDocuments();
            docStats.put("freeDocuments", freeDocuments);

            // Đếm tài liệu premium
            long premiumDocuments = documentRepository.countPremiumDocuments();
            docStats.put("premiumDocuments", premiumDocuments);

            // Tổng tài liệu (active)
            long totalActiveDocuments = freeDocuments + premiumDocuments;
            docStats.put("totalActiveDocuments", totalActiveDocuments);

            System.out.println("Document Stats - Free: " + freeDocuments +
                    ", Premium: " + premiumDocuments +
                    ", Total Active: " + totalActiveDocuments);

        } catch (Exception e) {
            System.err.println("Error getting document stats: " + e.getMessage());
            return getEmptyDocumentStats();
        }

        return docStats;
    }

    public Map<String, Object> getEmptyDocumentStats() {
        Map<String, Object> emptyStats = new HashMap<>();
        emptyStats.put("freeDocuments", 0L);
        emptyStats.put("premiumDocuments", 0L);
        emptyStats.put("totalActiveDocuments", 0L);
        return emptyStats;
    }

    public List<Map<String, Object>> getLatestDocuments(int limit) {
        try {
            System.out.println("=== DEBUG: Getting latest documents with limit: " + limit + " ===");
            
            // Sử dụng query với JOIN để lấy category thực tế
            List<Map<String, Object>> documents = documentRepository.findLatestDocumentsWithDetails(limit);
            System.out.println("Raw documents from JOIN query: " + documents.size());
            
            // Debug: In ra dữ liệu thô
            for (int i = 0; i < Math.min(documents.size(), 3); i++) {
                Map<String, Object> doc = documents.get(i);
                System.out.println("Document " + i + ": " + doc);
                System.out.println("  - Category from query: '" + doc.get("categoryName") + "'");
                System.out.println("  - Uploader from query: '" + doc.get("uploaderName") + "'");
            }
            
            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> doc : documents) {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.get("id"));
                docInfo.put("title", doc.get("title"));
                
                // Debug status
                String rawStatus = (String) doc.get("status");
                System.out.println("Raw status for doc " + doc.get("id") + ": '" + rawStatus + "'");
                
                // Xử lý status - đảm bảo có giá trị hợp lệ
                String status = rawStatus;
                if (status == null || status.trim().isEmpty()) {
                    status = "pending";
                } else {
                    status = status.toLowerCase().trim();
                }
                
                // Đảm bảo status hợp lệ
                if (!status.matches("draft|pending|approved|rejected|published|active")) {
                    status = "pending";
                }
                
                docInfo.put("status", status);
                System.out.println("Final processed status: '" + status + "'");
                
                docInfo.put("uploadDate", doc.get("uploadDate"));
                
                // Lấy uploader name từ query result
                String uploaderName = (String) doc.get("uploaderName");
                if (uploaderName == null || uploaderName.trim().isEmpty()) {
                    uploaderName = "Người dùng";
                }
                docInfo.put("uploaderName", uploaderName);
                
                // Lấy category name từ query result
                String categoryName = (String) doc.get("categoryName");
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    categoryName = "Chưa phân loại";
                }
                docInfo.put("categoryName", categoryName);
                
                System.out.println("Final category: '" + categoryName + "', uploader: '" + uploaderName + "'");

                // Tính toán thời gian "time ago"
                if (doc.get("uploadDate") != null) {
                    LocalDateTime uploadDate = (LocalDateTime) doc.get("uploadDate");
                    docInfo.put("timeAgo", calculateTimeAgo(uploadDate));
                } else {
                    docInfo.put("timeAgo", "Không rõ");
                }

                result.add(docInfo);
                System.out.println("Added document: " + docInfo.get("title") + " with category: " + docInfo.get("categoryName"));
            }

            System.out.println("Final result size: " + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("Error getting latest documents with JOIN: " + e.getMessage());
            e.printStackTrace();

            // Fallback: sử dụng query đơn giản
            System.out.println("Using fallback method...");
            return getLatestDocumentsSimple(limit);
        }
    }

    private List<Map<String, Object>> getLatestDocumentsSimple(int limit) {
        try {
            List<Document> documents = documentRepository.findRecentDocuments(PageRequest.of(0, limit));
            List<Map<String, Object>> result = new ArrayList<>();

            for (Document doc : documents) {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("id", doc.getId());
                docInfo.put("title", doc.getTitle());
                
                // Xử lý status đúng cách
                String status = doc.getStatus();
                if (status == null || status.trim().isEmpty()) {
                    status = "pending"; // Mặc định là chờ duyệt
                }
                docInfo.put("status", status.toLowerCase());
                
                docInfo.put("uploadDate", doc.getCreatedAt());
                docInfo.put("timeAgo", calculateTimeAgo(doc.getCreatedAt()));
                
                // Fallback values vì không có relationship
                docInfo.put("uploaderName", "Người dùng");
                docInfo.put("categoryName", "Chưa phân loại");

                result.add(docInfo);
            }

            System.out.println("Fallback method returned " + result.size() + " documents");
            return result;
        } catch (Exception e) {
            System.err.println("Error in fallback method: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Không rõ";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (hours < 24) {
            return hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " tuần trước";
        } else if (days < 365) {
            long months = days / 30;
            return months + " tháng trước";
        } else {
            long years = days / 365;
            return years + " năm trước";
        }
    }

    private long getTotalDownloads() {
        try {
            // Tính tổng downloads từ tất cả documents
            return documentRepository.findAll()
                    .stream()
                    .mapToLong(doc -> doc.getDownloadsCount() != null ? doc.getDownloadsCount() : 0L)
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculating total downloads: " + e.getMessage());
            return 0L;
        }
    }

    private long getTotalRevenue() {
        try {
            System.out.println("=== REVENUE DEBUG START ===");

            // Kiểm tra xem có transactions không
            long totalTransactions = transactionRepository.count();
            System.out.println("Total transactions count: " + totalTransactions);

            if (totalTransactions == 0) {
                System.out.println("No transactions found in database!");
                return 0L;
            }

            // Lấy tất cả transactions để debug
            var allTransactions = transactionRepository.findAll();
            System.out.println("Found " + allTransactions.size() + " transactions:");

            long manualTotal = 0L;
            int completedCount = 0;

            for (var transaction : allTransactions) {
                System.out.println("Transaction ID: " + transaction.getId() +
                        ", Status: " + transaction.getStatus() +
                        ", Amount: " + transaction.getAmount() +
                        ", Type: " + transaction.getType());

                if ("completed".equals(transaction.getStatus())) {
                    completedCount++;
                    if (transaction.getAmount() != null) {
                        manualTotal += transaction.getAmount().longValue();
                    }
                }
            }

            System.out.println("Completed transactions: " + completedCount);
            System.out.println("Manual total calculation: " + manualTotal);

            // Thử query từ repository
            try {
                BigDecimal repoTotal = transactionRepository.getTotalRevenue();
                System.out.println("Repository query result: " + repoTotal);
            } catch (Exception queryEx) {
                System.err.println("Repository query failed: " + queryEx.getMessage());
            }

            System.out.println("=== REVENUE DEBUG END ===");

            return manualTotal;

        } catch (Exception e) {
            System.err.println("Error calculating total revenue: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public List<Map<String, Object>> getRecentActivities(int limit) {
        try {
            // Kiểm tra xem có hoạt động không
            long totalActivities = userActivityRepository.count();
            
            if (totalActivities == 0) {
                return new ArrayList<>();
            }

            // Lấy hoạt động với thông tin chi tiết
            List<Map<String, Object>> activities = userActivityRepository
                    .findRecentActivitiesWithDetails(PageRequest.of(0, limit));

            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> activity : activities) {
                Map<String, Object> activityInfo = new HashMap<>();

                activityInfo.put("id", activity.get("activity_id"));
                activityInfo.put("action", activity.get("action"));
                activityInfo.put("createdAt", activity.get("created_at"));
                activityInfo.put("ipAddress", activity.get("ip_address"));

                // User info
                activityInfo.put("userId", activity.get("user_id"));
                activityInfo.put("username", activity.get("username"));
                activityInfo.put("userFullName", activity.get("full_name"));

                // Document info (if exists)
                activityInfo.put("documentId", activity.get("document_id"));
                activityInfo.put("documentTitle", activity.get("document_title"));

                // Calculate time ago
                if (activity.get("created_at") != null) {
                    LocalDateTime createdAt = (LocalDateTime) activity.get("created_at");
                    activityInfo.put("timeAgo", calculateTimeAgo(createdAt));
                } else {
                    activityInfo.put("timeAgo", "Không rõ");
                }

                // Format activity description
                activityInfo.put("description", formatActivityDescription(activityInfo));
                activityInfo.put("iconClass", getActivityIconClass((String) activity.get("action")));

                result.add(activityInfo);
            }

            return result;

        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Map<String, Object> getActivityStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Thống kê theo loại hoạt động
            long uploadCount = userActivityRepository.countByAction("upload");
            long downloadCount = userActivityRepository.countByAction("download");
            long loginCount = userActivityRepository.countByAction("login");
            long registerCount = userActivityRepository.countByAction("register");
            long commentCount = userActivityRepository.countByAction("comment");
            long reportCount = userActivityRepository.countByAction("report");
            long purchaseCount = userActivityRepository.countByAction("purchase");
            long paymentCount = userActivityRepository.countByAction("payment");
            
            stats.put("uploadCount", uploadCount);
            stats.put("downloadCount", downloadCount);
            stats.put("loginCount", loginCount);
            stats.put("registerCount", registerCount);
            stats.put("commentCount", commentCount);
            stats.put("reportCount", reportCount);
            stats.put("purchaseCount", purchaseCount);
            stats.put("paymentCount", paymentCount);
            
            // Tổng số hoạt động
            long totalActivities = userActivityRepository.count();
            stats.put("totalActivities", totalActivities);
            
            // Hoạt động phổ biến nhất
            String mostPopularAction = "upload";
            long maxCount = uploadCount;
            
            if (downloadCount > maxCount) {
                mostPopularAction = "download";
                maxCount = downloadCount;
            }
            if (loginCount > maxCount) {
                mostPopularAction = "login";
                maxCount = loginCount;
            }
            
            stats.put("mostPopularAction", mostPopularAction);
            stats.put("mostPopularActionCount", maxCount);
            
        } catch (Exception e) {
            System.err.println("Error getting activity stats: " + e.getMessage());
            // Return empty stats on error
            stats.put("uploadCount", 0L);
            stats.put("downloadCount", 0L);
            stats.put("loginCount", 0L);
            stats.put("registerCount", 0L);
            stats.put("commentCount", 0L);
            stats.put("reportCount", 0L);
            stats.put("purchaseCount", 0L);
            stats.put("paymentCount", 0L);
            stats.put("totalActivities", 0L);
            stats.put("mostPopularAction", "none");
            stats.put("mostPopularActionCount", 0L);
        }
        
        return stats;
    }

    private String formatActivityDescription(Map<String, Object> activity) {
        String action = (String) activity.get("action");
        String userFullName = (String) activity.get("userFullName");
        String username = (String) activity.get("username");
        String documentTitle = (String) activity.get("documentTitle");

        String displayName = userFullName != null && !userFullName.trim().isEmpty() ? userFullName : username;
        if (displayName == null)
            displayName = "Người dùng";

        switch (action) {
            case "upload":
                return displayName + " đã đăng tải tài liệu: " + (documentTitle != null ? documentTitle : "Tài liệu");
            case "download":
                return displayName + " đã tải xuống tài liệu: " + (documentTitle != null ? documentTitle : "Tài liệu");
            case "register":
                return displayName + " đã đăng ký tài khoản mới";
            case "login":
                return displayName + " đã đăng nhập";
            case "comment":
                return displayName + " đã bình luận tài liệu: " + (documentTitle != null ? documentTitle : "Tài liệu");
            case "report":
                return displayName + " đã báo cáo tài liệu: " + (documentTitle != null ? documentTitle : "Tài liệu");
            case "purchase":
                return displayName + " đã mua tài liệu: " + (documentTitle != null ? documentTitle : "Tài liệu");
            case "payment":
                return displayName + " đã nạp tiền vào tài khoản";
            default:
                return displayName + " đã thực hiện hành động: " + action;
        }
    }

    private String getActivityIconClass(String action) {
        switch (action) {
            case "upload":
                return "fas fa-upload";
            case "download":
                return "fas fa-download";
            case "register":
                return "fas fa-user-plus";
            case "login":
                return "fas fa-sign-in-alt";
            case "comment":
                return "fas fa-comment";
            case "report":
                return "fas fa-flag";
            case "purchase":
                return "fas fa-shopping-cart";
            case "payment":
                return "fas fa-money-bill-wave";
            default:
                return "fas fa-circle";
        }
    }

    public Map<String, Object> debugActivities() {
        Map<String, Object> debug = new HashMap<>();
        
        try {
            // Kiểm tra tổng số activities
            long totalCount = userActivityRepository.count();
            debug.put("totalActivitiesCount", totalCount);
            
            // Lấy 5 activities đơn giản
            List<UserActivity> simpleActivities = userActivityRepository.findRecentActivities(PageRequest.of(0, 5));
            debug.put("simpleActivitiesCount", simpleActivities.size());
            
            List<Map<String, Object>> simpleList = new ArrayList<>();
            for (UserActivity activity : simpleActivities) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", activity.getId());
                item.put("action", activity.getAction());
                item.put("createdAt", activity.getCreatedAt());
                item.put("userId", activity.getUser() != null ? activity.getUser().getId() : null);
                item.put("documentId", activity.getDocument() != null ? activity.getDocument().getId() : null);
                simpleList.add(item);
            }
            debug.put("simpleActivities", simpleList);
            
            // Thử query phức tạp
            try {
                List<Map<String, Object>> complexActivities = userActivityRepository.findRecentActivitiesWithDetails(PageRequest.of(0, 5));
                debug.put("complexActivitiesCount", complexActivities.size());
                debug.put("complexActivities", complexActivities);
            } catch (Exception e) {
                debug.put("complexQueryError", e.getMessage());
            }
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return debug;
    }

}