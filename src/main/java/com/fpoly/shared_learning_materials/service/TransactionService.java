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
            int totalCoinsReceived = 0;

            for (TransactionDetail detail : details) {
                if (detail.getCoinsReceived() != null) {
                    totalCoinsReceived += detail.getCoinsReceived();
                }
            }

            // Update user's coin balance and statistics
            if (user.getCoinBalance() == null) {
                user.setCoinBalance(0);
            }
            user.setCoinBalance(user.getCoinBalance() + totalCoinsReceived);

            if (user.getTotalSpent() == null) {
                user.setTotalSpent(BigDecimal.ZERO);
            }
            user.setTotalSpent(user.getTotalSpent().add(transaction.getAmount()));

            if (user.getTotalCoinsPurchased() == null) {
                user.setTotalCoinsPurchased(0);
            }
            user.setTotalCoinsPurchased(user.getTotalCoinsPurchased() + totalCoinsReceived);

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
}