package com.fpoly.shared_learning_materials.controller.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.repository.DocumentCategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Trang chủ admin dashboard
     */
    @GetMapping
    public String index(Model model) {
        // Thêm thông tin cần thiết cho dashboard
        model.addAttribute("pageTitle", "Admin Dashboard");
        return "admin/index";
    }

    /**
     * Trang dashboard (alias cho index)
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        return index(model);
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("documents", documentRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc());
        model.addAttribute("currentPage", "statistics");
        return "admin/statistics";
    }

    @GetMapping("/statistics/user-growth")
    public ResponseEntity<Map<String, Integer>> getUserGrowth(@RequestParam(defaultValue = "365") int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Object[]> results = userRepository.countUsersByDateRange(startDateTime, endDateTime);

        Map<String, Integer> monthlyCounts = new LinkedHashMap<>();

        for (Object[] row : results) {
            int month = (int) row[0];
            long count = (long) row[1];
            monthlyCounts.put("T" + month, (int) count);
        }

        return ResponseEntity.ok(monthlyCounts);
    }

    @GetMapping("/statistics/category-distribution")
    public ResponseEntity<Map<String, Long>> getCategoryDistribution() {
        List<Object[]> results = documentCategoryRepository.countDocumentsByCategory();

        Map<String, Long> data = new LinkedHashMap<>();
        for (Object[] row : results) {
            String categoryName = (String) row[0];
            Long documentCount = (Long) row[1];
            data.put(categoryName, documentCount);
        }

        return ResponseEntity.ok(data);
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<Map<String, Object>> getStatisticsSummary(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (start != null && end != null) {
            startDate = LocalDate.parse(start).atStartOfDay();
            endDate = LocalDate.parse(end).atTime(23, 59, 59);
        }

        long userCount;
        long documentCount;
        BigDecimal revenue;

        if (startDate != null && endDate != null) {
            userCount = userRepository.countByCreatedAtBetweenAndDeletedAtIsNull(startDate, endDate);
            documentCount = documentRepository.countByCreatedAtBetweenAndDeletedAtIsNull(startDate, endDate);
            revenue = transactionRepository.sumAmountByTypeAndStatusAndCreatedAtBetween(
                    Transaction.TransactionType.PURCHASE,
                    Transaction.TransactionStatus.COMPLETED,
                    startDate, endDate);
        } else {
            userCount = userRepository.countByDeletedAtIsNull();
            documentCount = documentRepository.countByDeletedAtIsNull();
            revenue = transactionRepository.sumAmountByTypeAndStatus(
                    Transaction.TransactionType.PURCHASE,
                    Transaction.TransactionStatus.COMPLETED);
        }

        if (revenue == null)
            revenue = BigDecimal.ZERO;

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userCount);
        result.put("documentCount", documentCount);
        result.put("revenueAmount", revenue);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/statistics/traffic")
    public ResponseEntity<List<Long>> getUserTrafficByDay(
            @RequestParam int year,
            @RequestParam int month) {

        List<Long> dailyCounts = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDateTime startOfDay = LocalDate.of(year, month, day).atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

            long count = userRepository.countByCreatedAtBetweenAndDeletedAtIsNull(startOfDay, endOfDay);
            dailyCounts.add(count);
        }

        return ResponseEntity.ok(dailyCounts);
    }

    @GetMapping("/statistics/revenue-monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenue(@RequestParam(defaultValue = "30") int days) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.minusDays(days);

        List<Object[]> revenueByMonth = transactionRepository.getMonthlyRevenueSummary(
                Transaction.TransactionType.PURCHASE,
                Transaction.TransactionStatus.COMPLETED,
                start.atStartOfDay(),
                now.atTime(23, 59, 59));

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        String maxLabel = "";

        for (Object[] row : revenueByMonth) {
            Integer year = (Integer) row[0];
            Integer month = (Integer) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            String label = "T" + month + "/" + year;
            labels.add(label);
            data.add(amount);

            total = total.add(amount);
            if (amount.compareTo(max) > 0) {
                max = amount;
                maxLabel = label;
            }
        }

        BigDecimal avg = data.isEmpty() ? BigDecimal.ZERO
                : total.divide(new BigDecimal(data.size()), 0, RoundingMode.HALF_UP);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("data", data);
        result.put("total", total);
        result.put("average", avg);
        result.put("max", max);
        result.put("maxLabel", maxLabel);

        return ResponseEntity.ok(result);
    }

}