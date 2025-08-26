package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.TransactionDetail;
import com.fpoly.shared_learning_materials.repository.TransactionDetailRepository;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentPurchaseStatisticsService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.15"); // 15%

    /**
     * Thống kê tổng hoa hồng từ việc bán tài liệu
     */
    public Map<String, Object> getCommissionStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        Map<String, Object> stats = new HashMap<>();

        // Tổng doanh thu từ bán tài liệu
        BigDecimal totalRevenue = getTotalDocumentRevenue(fromDate, toDate);

        // Tổng hoa hồng (15%)
        BigDecimal totalCommission = totalRevenue.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);

        // Tổng số xu chia cho người bán (85%)
        BigDecimal totalSellerAmount = totalRevenue.multiply(new BigDecimal("0.85")).setScale(2, RoundingMode.HALF_UP);

        // Số giao dịch bán tài liệu
        long totalTransactions = getTotalDocumentTransactions(fromDate, toDate);

        stats.put("totalRevenue", totalRevenue);
        stats.put("totalCommission", totalCommission);
        stats.put("totalSellerAmount", totalSellerAmount);
        stats.put("totalTransactions", totalTransactions);
        stats.put("commissionRate", COMMISSION_RATE);
        stats.put("sellerRate", new BigDecimal("0.85"));
        stats.put("fromDate", fromDate);
        stats.put("toDate", toDate);

        return stats;
    }

    /**
     * Thống kê hoa hồng theo tháng
     */
    public Map<String, Object> getMonthlyCommissionStatistics(int year) {
        Map<String, Object> monthlyStats = new HashMap<>();

        for (int month = 1; month <= 12; month++) {
            LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);

            Map<String, Object> monthStats = getCommissionStatistics(startOfMonth, endOfMonth);
            monthlyStats.put("month_" + month, monthStats);
        }

        monthlyStats.put("year", year);
        return monthlyStats;
    }

    /**
     * Thống kê top người bán tài liệu
     */
    public List<Map<String, Object>> getTopSellers(int limit) {
        return transactionDetailRepository.findTopSellers(limit);
    }

    /**
     * Thống kê top tài liệu bán chạy
     */
    public List<Map<String, Object>> getTopSellingDocuments(int limit) {
        return transactionDetailRepository.findTopSellingDocuments(limit);
    }

    /**
     * Tổng doanh thu từ bán tài liệu trong khoảng thời gian
     */
    private BigDecimal getTotalDocumentRevenue(LocalDateTime fromDate, LocalDateTime toDate) {
        return transactionDetailRepository.sumDocumentRevenueByDateRange(fromDate, toDate);
    }

    /**
     * Tổng số giao dịch bán tài liệu trong khoảng thời gian
     */
    private long getTotalDocumentTransactions(LocalDateTime fromDate, LocalDateTime toDate) {
        return transactionDetailRepository.countDocumentTransactionsByDateRange(fromDate, toDate);
    }

    /**
     * Thống kê hoa hồng theo ngày
     */
    public Map<String, Object> getDailyCommissionStatistics(LocalDateTime fromDate, LocalDateTime toDate) {
        return transactionDetailRepository.getDailyCommissionStats(fromDate, toDate);
    }
}