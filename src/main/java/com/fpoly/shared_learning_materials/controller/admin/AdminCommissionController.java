package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.service.DocumentPurchaseStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/commission")
public class AdminCommissionController {

    @Autowired
    private DocumentPurchaseStatisticsService commissionService;

    @GetMapping
    public String commissionDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Model model) {

        // Mặc định là 30 ngày gần nhất nếu không có tham số
        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }

        Map<String, Object> stats = commissionService.getCommissionStatistics(fromDate, toDate);

        model.addAttribute("stats", stats);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("currentPage", "commission");

        return "admin/commission/dashboard";
    }

    @GetMapping("/monthly")
    public String monthlyStatistics(
            @RequestParam(defaultValue = "2024") int year,
            Model model) {

        Map<String, Object> monthlyStats = commissionService.getMonthlyCommissionStatistics(year);

        model.addAttribute("monthlyStats", monthlyStats);
        model.addAttribute("year", year);
        model.addAttribute("currentPage", "commission");

        return "admin/commission/monthly";
    }

    @GetMapping("/top-sellers")
    public String topSellers(
            @RequestParam(defaultValue = "10") int limit,
            Model model) {

        model.addAttribute("topSellers", commissionService.getTopSellers(limit));
        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", "commission");

        return "admin/commission/top-sellers";
    }

    @GetMapping("/top-documents")
    public String topDocuments(
            @RequestParam(defaultValue = "10") int limit,
            Model model) {

        model.addAttribute("topDocuments", commissionService.getTopSellingDocuments(limit));
        model.addAttribute("limit", limit);
        model.addAttribute("currentPage", "commission");

        return "admin/commission/top-documents";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getStatsApi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        if (fromDate == null) {
            fromDate = LocalDateTime.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDateTime.now();
        }

        return commissionService.getCommissionStatistics(fromDate, toDate);
    }
}