package com.fpoly.shared_learning_materials.controller.admin;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import com.fpoly.shared_learning_materials.service.DashboardService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashBoardController {

    private final DashboardService dashboardService;

    public DashBoardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/index")
    public String showDashboard(Model model) {
        try {
            // Sử dụng DashboardService để lấy tất cả thống kê
            Map<String, Object> stats = dashboardService.getBasicStats();

            // Lấy dữ liệu từ stats map
            long totalUsers = (Long) stats.get("totalUsers");
            long totalDocuments = (Long) stats.get("totalDocuments");
            long totalDownloads = (Long) stats.get("totalDownloads");
            long totalRevenue = (Long) stats.get("totalRevenue");

            // Lấy thống kê phân loại tài liệu
            @SuppressWarnings("unchecked")
            Map<String, Object> documentStats = (Map<String, Object>) stats.get("documentStats");

            // Lấy tài liệu gần đây (10 tài liệu mới nhất)
            List<Map<String, Object>> recentDocuments = dashboardService.getLatestDocuments(10);

            // Lấy hoạt động gần đây (5 hoạt động mới nhất)
            List<Map<String, Object>> recentActivities = dashboardService.getRecentActivities(5);

            System.out.println("Dashboard stats loaded - Users: " + totalUsers +
                    ", Documents: " + totalDocuments +
                    ", Downloads: " + totalDownloads +
                    ", Revenue: " + totalRevenue);

            if (documentStats != null) {
                System.out.println("Document classification - Free: " + documentStats.get("freeDocuments") +
                        ", Premium: " + documentStats.get("premiumDocuments") +
                        ", Total Active: " + documentStats.get("totalActiveDocuments"));
            }

            // Thêm vào model
            model.addAttribute("recentDocuments", recentDocuments);
            model.addAttribute("recentActivities", recentActivities);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalDocuments", totalDocuments);
            model.addAttribute("totalDownloads", totalDownloads);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("documentStats", documentStats);

        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();

            // Fallback values
            model.addAttribute("totalUsers", 0L);
            model.addAttribute("totalDocuments", 0L);
            model.addAttribute("totalDownloads", 0L);
            model.addAttribute("totalRevenue", 0L);
            model.addAttribute("recentDocuments", List.of());
            model.addAttribute("recentActivities", List.of());
            model.addAttribute("documentStats", dashboardService.getEmptyDocumentStats());
        }

        return "admin/index";
    }

    @GetMapping("/admin/api/activity-chart")
    @ResponseBody
    public Map<String, Object> getActivityChartData() {
        try {
            Map<String, Object> chartData = new HashMap<>();

            // Mock data cho activity chart (7 ngày qua)
            List<String> labels = List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN");
            List<Integer> data = List.of(30, 50, 70, 60, 75, 85, 60);

            chartData.put("labels", labels);
            chartData.put("data", data);

            return chartData;

        } catch (Exception e) {
            System.err.println("Error getting activity chart data: " + e.getMessage());

            // Fallback data
            Map<String, Object> fallbackData = new HashMap<>();
            fallbackData.put("labels", List.of("T2", "T3", "T4", "T5", "T6", "T7", "CN"));
            fallbackData.put("data", List.of(0, 0, 0, 0, 0, 0, 0));

            return fallbackData;
        }
    }

    @GetMapping("/admin/api/category-chart")
    @ResponseBody
    public Map<String, Object> getCategoryChartData() {
        try {
            Map<String, Object> documentStats = dashboardService.getDocumentStats();

            long freeDocuments = (Long) documentStats.get("freeDocuments");
            long premiumDocuments = (Long) documentStats.get("premiumDocuments");

            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", List.of("Miễn phí", "Premium"));
            chartData.put("data", List.of(freeDocuments, premiumDocuments));
            chartData.put("colors", List.of("#4facfe", "#f093fb"));

            return chartData;

        } catch (Exception e) {
            System.err.println("Error getting category chart data: " + e.getMessage());

            // Fallback data
            Map<String, Object> fallbackData = new HashMap<>();
            fallbackData.put("labels", List.of("Miễn phí", "Premium"));
            fallbackData.put("data", List.of(0, 0));
            fallbackData.put("colors", List.of("#4facfe", "#f093fb"));

            return fallbackData;
        }
    }

    @GetMapping("/admin/api/recent-activities")
    @ResponseBody
    public Map<String, Object> getRecentActivities() {
        try {
            List<Map<String, Object>> activities = dashboardService.getRecentActivities(10);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activities", activities);
            response.put("count", activities.size());

            return response;

        } catch (Exception e) {
            System.err.println("Error getting recent activities: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("activities", List.of());
            errorResponse.put("count", 0);
            errorResponse.put("error", e.getMessage());

            return errorResponse;
        }
    }

    @GetMapping("/admin/api/activity-stats")
    @ResponseBody
    public Map<String, Object> getActivityStats() {
        try {
            Map<String, Object> stats = dashboardService.getActivityStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);

            return response;

        } catch (Exception e) {
            System.err.println("Error getting activity stats: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("stats", new HashMap<>());
            errorResponse.put("error", e.getMessage());

            return errorResponse;
        }
    }

    @GetMapping("/admin/api/debug-activities")
    @ResponseBody
    public Map<String, Object> debugActivities() {
        try {
            Map<String, Object> debug = dashboardService.debugActivities();
            return debug;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return error;
        }
    }
}
