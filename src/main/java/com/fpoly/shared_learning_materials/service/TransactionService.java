package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.domain.TransactionDetail;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.repository.TransactionDetailRepository;
import com.fpoly.shared_learning_materials.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Lấy tất cả giao dịch với phân trang
     */
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findByDeletedAtIsNull(pageable);
    }

    /**
     * Tìm kiếm giao dịch theo keyword
     */
    public Page<Transaction> searchTransactions(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllTransactions(pageable);
        }
        return transactionRepository.findByKeywordAndDeletedAtIsNull(keyword.trim(), pageable);
    }

    /**
     * Lọc giao dịch theo trạng thái
     */
    public Page<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatusAndDeletedAtIsNull(status, pageable);
    }

    /**
     * Lọc giao dịch theo loại
     */
    public Page<Transaction> getTransactionsByType(Transaction.TransactionType type, Pageable pageable) {
        return transactionRepository.findByTypeAndDeletedAtIsNull(type, pageable);
    }

    /**
     * Tìm kiếm và lọc giao dịch
     */
    public Page<Transaction> searchAndFilterTransactions(String keyword, Transaction.TransactionStatus status,
            Transaction.TransactionType type, Pageable pageable) {
        return transactionRepository.findByFiltersAndDeletedAtIsNull(keyword, status, type, pageable);
    }

    /**
     * Lấy giao dịch theo ID
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Lấy giao dịch theo mã
     */
    public Optional<Transaction> getTransactionByCode(String code) {
        return transactionRepository.findByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Tạo giao dịch mới
     */
    public Transaction createTransaction(Transaction transaction) {
        // Tự động generate code nếu chưa có
        if (transaction.getCode() == null || transaction.getCode().trim().isEmpty()) {
            transaction.setCode(generateTransactionCode());
        }

        validateTransaction(transaction);

        // Set default status nếu chưa có
        if (transaction.getStatus() == null) {
            transaction.setStatus(Transaction.TransactionStatus.PENDING);
        }

        return transactionRepository.save(transaction);
    }

    /**
     * Cập nhật giao dịch
     */
    public Transaction updateTransaction(Transaction transaction) {
        validateTransaction(transaction);
        return transactionRepository.save(transaction);
    }

    /**
     * Xóa giao dịch (soft delete)
     */
    public void deleteTransaction(Long id) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(id);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setDeletedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
    }

    /**
     * Xóa nhiều giao dịch
     */
    public void deleteTransactions(List<Long> ids) {
        List<Transaction> transactions = transactionRepository.findAllById(ids);
        transactions.forEach(t -> t.setDeletedAt(LocalDateTime.now()));
        transactionRepository.saveAll(transactions);
    }

    /**
     * Cập nhật trạng thái nhiều giao dịch
     */
    public void updateTransactionsStatus(List<Long> ids, Transaction.TransactionStatus status) {
        List<Transaction> transactions = transactionRepository.findAllById(ids);
        transactions.forEach(t -> t.setStatus(status));
        transactionRepository.saveAll(transactions);
    }

    /**
     * Kiểm tra mã giao dịch có tồn tại không
     */
    public boolean existsByCode(String code) {
        return transactionRepository.existsByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Tạo mã giao dịch tự động
     */
    public String generateTransactionCode() {
        String prefix = "TXN";
        int counter = 1;
        String code;
        do {
            code = prefix + String.format("%06d", counter);
            counter++;
        } while (existsByCode(code));
        return code;
    }

    /**
     * Lấy giao dịch theo người dùng
     */
    public List<Transaction> getTransactionsByUser(User user) {
        return transactionRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
    }

    /**
     * Lọc giao dịch theo người dùng + keyword/status/type (phân trang)
     */
    public Page<Transaction> searchUserTransactions(User user, String keyword,
            Transaction.TransactionStatus status,
            Transaction.TransactionType type,
            java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate,
            java.math.BigDecimal minAmount,
            java.math.BigDecimal maxAmount,
            Pageable pageable) {
        return transactionRepository.findByUserWithFilters(user,
                (keyword == null || keyword.isBlank()) ? null : keyword.trim(), status, type,
                startDate, endDate, minAmount, maxAmount, pageable);
    }

    /**
     * Thống kê tổng số tiền theo người dùng cho từng loại giao dịch
     */
    public BigDecimal getUserTotalByType(User user, Transaction.TransactionType type,
            Transaction.TransactionStatus status) {
        return transactionRepository.sumAmountByUserAndTypeAndStatus(user, type, status);
    }

    /**
     * Lấy giao dịch theo người dùng (phân trang)
     */
    public Page<Transaction> getTransactionsByUser(User user, Pageable pageable) {
        return transactionRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Thống kê tổng số giao dịch
     */
    public long getTotalTransactions() {
        return transactionRepository.countActiveTransactions();
    }

    /**
     * Thống kê tổng doanh thu
     */
    public BigDecimal getTotalRevenue() {
        return transactionRepository.getTotalRevenue();
    }

    /**
     * Thống kê tỷ lệ thành công
     */
    public Double getSuccessRate() {
        return transactionRepository.getSuccessRate();
    }

    /**
     * Thống kê số giao dịch chờ xử lý
     */
    public long getPendingTransactions() {
        return transactionRepository.countPendingTransactions();
    }

    /**
     * Tính toán balance flow cho dashboard (bao gồm cả withdrawal và recharge)
     */
    public Map<String, Object> getBalanceFlowStats(User user) {
        Map<String, Object> stats = new HashMap<>();

        // Tổng nạp xu (PURCHASE - COMPLETED)
        BigDecimal totalRecharge = getUserTotalByType(user, Transaction.TransactionType.PURCHASE,
                Transaction.TransactionStatus.COMPLETED);
        if (totalRecharge == null)
            totalRecharge = BigDecimal.ZERO;

        // Tổng rút xu (WITHDRAWAL - COMPLETED)
        BigDecimal totalWithdrawal = getUserTotalByType(user, Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.COMPLETED);
        if (totalWithdrawal == null)
            totalWithdrawal = BigDecimal.ZERO;

        // Tổng rút xu đang chờ (WITHDRAWAL - PENDING)
        BigDecimal pendingWithdrawal = getUserTotalByType(user, Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.PENDING);
        if (pendingWithdrawal == null)
            pendingWithdrawal = BigDecimal.ZERO;

        // Tổng rút xu đang xử lý (WITHDRAWAL - PROCESSING)
        BigDecimal processingWithdrawal = getUserTotalByType(user, Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.PROCESSING);
        if (processingWithdrawal == null)
            processingWithdrawal = BigDecimal.ZERO;

        // Tổng rút xu bị từ chối (WITHDRAWAL - FAILED)
        BigDecimal failedWithdrawal = getUserTotalByType(user, Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.FAILED);
        if (failedWithdrawal == null)
            failedWithdrawal = BigDecimal.ZERO;

        // Tổng rút xu bị hủy (WITHDRAWAL - CANCELLED)
        BigDecimal cancelledWithdrawal = getUserTotalByType(user, Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.CANCELLED);
        if (cancelledWithdrawal == null)
            cancelledWithdrawal = BigDecimal.ZERO;

        // Số dư hiện tại
        BigDecimal currentBalance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;

        // Tổng rút xu đang chờ xử lý (PENDING + PROCESSING)
        BigDecimal totalPendingWithdrawal = pendingWithdrawal.add(processingWithdrawal);

        // Số dư khả dụng (trừ đi số xu đang chờ rút)
        BigDecimal availableBalance = currentBalance.subtract(totalPendingWithdrawal);
        if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
            availableBalance = BigDecimal.ZERO;
        }

        stats.put("totalRecharge", totalRecharge);
        stats.put("totalWithdrawal", totalWithdrawal);
        stats.put("pendingWithdrawal", pendingWithdrawal);
        stats.put("processingWithdrawal", processingWithdrawal);
        stats.put("failedWithdrawal", failedWithdrawal);
        stats.put("cancelledWithdrawal", cancelledWithdrawal);
        stats.put("currentBalance", currentBalance);
        stats.put("totalPendingWithdrawal", totalPendingWithdrawal);
        stats.put("availableBalance", availableBalance);

        return stats;
    }

    /**
     * Lấy thống kê giao dịch theo tháng cho user
     */
    public Map<String, Object> getMonthlyTransactionStats(User user, int months) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        // Thống kê nạp xu theo tháng
        List<Object[]> rechargeStats = transactionRepository.getMonthlyStatsByUserAndType(
                user, Transaction.TransactionType.PURCHASE, startDate, endDate);

        // Thống kê rút xu theo tháng
        List<Object[]> withdrawalStats = transactionRepository.getMonthlyStatsByUserAndType(
                user, Transaction.TransactionType.WITHDRAWAL, startDate, endDate);

        stats.put("rechargeStats", rechargeStats);
        stats.put("withdrawalStats", withdrawalStats);
        stats.put("months", months);

        return stats;
    }

    /**
     * Complete coin purchase and update user balance
     */
    public void completeCoinPurchase(Transaction transaction) {
        try {
            // Find transaction details
            List<TransactionDetail> details = transactionDetailRepository.findByTransaction(transaction);

            if (details.isEmpty()) {
                throw new IllegalStateException("Không tìm thấy chi tiết giao dịch");
            }

            // Update user balance
            User user = transaction.getUser();
            BigDecimal totalCoinsReceived = BigDecimal.ZERO;

            for (TransactionDetail detail : details) {
                if (detail.getCoinsReceived() != null) {
                    totalCoinsReceived = totalCoinsReceived.add(new BigDecimal(detail.getCoinsReceived()));
                }
            }

            // Update user's coin balance and statistics
            if (user.getCoinBalance() == null) {
                user.setCoinBalance(BigDecimal.ZERO);
            }
            user.setCoinBalance(user.getCoinBalance().add(totalCoinsReceived));

            if (user.getTotalSpent() == null) {
                user.setTotalSpent(BigDecimal.ZERO);
            }
            user.setTotalSpent(user.getTotalSpent().add(transaction.getAmount()));

            if (user.getTotalCoinsPurchased() == null) {
                user.setTotalCoinsPurchased(BigDecimal.ZERO);
            }
            user.setTotalCoinsPurchased(user.getTotalCoinsPurchased().add(totalCoinsReceived));

            userRepository.save(user);

            // Update transaction status to completed
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // Notify user
            String title = "Nạp xu thành công";
            String message = "Bạn đã nạp thành công " + totalCoinsReceived + " xu. Mã giao dịch: "
                    + transaction.getCode();
            notificationService.createNotification(user, title, message, "transaction");

        } catch (Exception e) {
            // Rollback transaction status
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            throw new RuntimeException("Lỗi cập nhật số dư xu: " + e.getMessage(), e);
        }
    }

    /**
     * Yêu cầu hoàn tiền cho giao dịch mua xu đã hoàn thành
     */
    public Transaction requestRefund(Transaction original, String reason) {
        if (original == null) {
            throw new IllegalArgumentException("Giao dịch không hợp lệ");
        }
        if (original.getType() != Transaction.TransactionType.PURCHASE) {
            throw new IllegalStateException("Chỉ hỗ trợ hoàn tiền cho giao dịch mua xu");
        }
        if (original.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Chỉ hoàn tiền cho giao dịch đã hoàn thành");
        }
        // Tạo giao dịch REFUND mới ở trạng thái PENDING
        Transaction refund = new Transaction();
        refund.setCode(generateTransactionCode());
        refund.setType(Transaction.TransactionType.REFUND);
        refund.setAmount(original.getAmount());
        refund.setStatus(Transaction.TransactionStatus.PENDING);
        refund.setPaymentMethod(original.getPaymentMethod());
        refund.setUser(original.getUser());
        refund.setNotes(
                reason != null && !reason.trim().isEmpty() ? ("Refund for " + original.getCode() + ": " + reason.trim())
                        : ("Refund for " + original.getCode()));
        refund.setCreatedAt(java.time.LocalDateTime.now());
        validateTransaction(refund);
        return transactionRepository.save(refund);
    }

    /**
     * Validate giao dịch trước khi lưu
     */
    public void validateTransaction(Transaction transaction) {
        if (transaction.getCode() == null || transaction.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã giao dịch không được để trống");
        }

        if (transaction.getType() == null) {
            throw new IllegalArgumentException("Loại giao dịch không được để trống");
        }

        if (transaction.getAmount() == null || transaction.getAmount().doubleValue() <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }

        if (transaction.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái giao dịch không được để trống");
        }

        if (transaction.getUser() == null || transaction.getUser().getId() == null) {
            throw new IllegalArgumentException("Người dùng không được để trống");
        }

        // Kiểm tra user có tồn tại không
        Optional<User> userOpt = userRepository.findById(transaction.getUser().getId());
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }
    }

    /**
     * Lấy tổng số giao dịch của user
     */
    public long getUserTotalTransactions(User user) {
        return transactionRepository.countByUserAndDeletedAtIsNull(user);
    }

    /**
     * Lấy số giao dịch theo trạng thái của user
     */
    public long getUserTransactionsByStatus(User user, Transaction.TransactionStatus status) {
        return transactionRepository.countByUserAndStatusAndDeletedAtIsNull(user, status);
    }

    /**
     * Lấy tổng số tiền đã chi của user
     */
    public BigDecimal getUserTotalSpent(User user) {
        return transactionRepository.sumAmountByUserAndDeletedAtIsNull(user);
    }

    /**
     * Lấy thống kê giao dịch theo ngày của user
     */
    public List<Map<String, Object>> getUserTransactionStatsByDate(User user, LocalDateTime startDate,
            LocalDateTime endDate, String groupBy) {
        List<Object[]> results;
        if ("day".equals(groupBy)) {
            results = transactionRepository.getUserTransactionStatsByDay(user, startDate, endDate);
        } else if ("week".equals(groupBy)) {
            results = transactionRepository.getUserTransactionStatsByWeek(user, startDate, endDate);
        } else {
            results = transactionRepository.getUserTransactionStatsByMonth(user, startDate, endDate);
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row[0]);
            item.put("amount", row[1]);
            item.put("count", row[2]);
            data.add(item);
        }
        return data;
    }

    /**
     * Lấy thống kê giao dịch theo loại của user
     */
    public List<Map<String, Object>> getUserTransactionStatsByType(User user, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Object[]> results = transactionRepository.getUserTransactionStatsByType(user, startDate, endDate);
        List<Map<String, Object>> data = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", row[0]);
            item.put("amount", row[1]);
            item.put("count", row[2]);
            data.add(item);
        }
        return data;
    }
}