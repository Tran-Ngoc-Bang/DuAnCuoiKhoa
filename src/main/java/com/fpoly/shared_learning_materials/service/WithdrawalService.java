package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WithdrawalService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Lấy tất cả withdrawals với phân trang
     */
    public Page<Transaction> getAllWithdrawals(Pageable pageable) {
        return transactionRepository.findByTypeAndDeletedAtIsNull(Transaction.TransactionType.WITHDRAWAL, pageable);
    }

    /**
     * Tìm kiếm withdrawals theo keyword
     */
    public Page<Transaction> searchWithdrawals(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllWithdrawals(pageable);
        }
        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                keyword.trim(), null, Transaction.TransactionType.WITHDRAWAL, pageable);
    }

    /**
     * Lọc withdrawals theo trạng thái
     */
    public Page<Transaction> getWithdrawalsByStatus(Transaction.TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                null, status, Transaction.TransactionType.WITHDRAWAL, pageable);
    }

    /**
     * Tìm kiếm và lọc withdrawals
     */
    public Page<Transaction> searchAndFilterWithdrawals(String keyword, Transaction.TransactionStatus status,
            LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                keyword, status, Transaction.TransactionType.WITHDRAWAL, pageable);
    }

    /**
     * Lấy withdrawal theo ID
     */
    public Optional<Transaction> getWithdrawalById(Long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent() && transaction.get().getType() == Transaction.TransactionType.WITHDRAWAL) {
            return transaction;
        }
        return Optional.empty();
    }

    /**
     * Lấy withdrawal theo mã
     */
    public Optional<Transaction> getWithdrawalByCode(String code) {
        Optional<Transaction> transaction = transactionRepository.findByCodeAndDeletedAtIsNull(code);
        if (transaction.isPresent() && transaction.get().getType() == Transaction.TransactionType.WITHDRAWAL) {
            return transaction;
        }
        return Optional.empty();
    }

    /**
     * Tạo withdrawal mới
     */
    public Transaction createWithdrawal(Transaction withdrawal) {
        // Set type to WITHDRAWAL
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);

        // Tự động generate code nếu chưa có
        if (withdrawal.getCode() == null || withdrawal.getCode().trim().isEmpty()) {
            withdrawal.setCode(generateWithdrawalCode());
        }

        validateWithdrawal(withdrawal);

        // Set default status nếu chưa có
        if (withdrawal.getStatus() == null) {
            withdrawal.setStatus(Transaction.TransactionStatus.PENDING);
        }

        return transactionRepository.save(withdrawal);
    }

    /**
     * Cập nhật withdrawal
     */
    public Transaction updateWithdrawal(Transaction withdrawal) {
        // Ensure type remains WITHDRAWAL
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);

        validateWithdrawal(withdrawal);
        return transactionRepository.save(withdrawal);
    }

    /**
     * Xóa withdrawal (soft delete)
     */
    public void deleteWithdrawal(Long id) {
        Optional<Transaction> withdrawalOpt = getWithdrawalById(id);
        if (withdrawalOpt.isPresent()) {
            Transaction withdrawal = withdrawalOpt.get();
            withdrawal.setDeletedAt(LocalDateTime.now());
            transactionRepository.save(withdrawal);
        }
    }

    /**
     * Xóa nhiều withdrawals
     */
    public void deleteWithdrawals(List<Long> ids) {
        List<Transaction> withdrawals = transactionRepository.findAllById(ids);
        withdrawals.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .forEach(t -> t.setDeletedAt(LocalDateTime.now()));
        transactionRepository.saveAll(withdrawals);
    }

    /**
     * Cập nhật trạng thái nhiều withdrawals
     */
    public void updateWithdrawalsStatus(List<Long> ids, Transaction.TransactionStatus status) {
        List<Transaction> withdrawals = transactionRepository.findAllById(ids);
        withdrawals.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .forEach(t -> t.setStatus(status));
        transactionRepository.saveAll(withdrawals);
    }

    /**
     * Kiểm tra mã withdrawal có tồn tại không
     */
    public boolean existsByCode(String code) {
        return transactionRepository.existsByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Tạo mã withdrawal tự động
     */
    public String generateWithdrawalCode() {
        String prefix = "WD";
        int counter = 1;
        String code;
        do {
            code = prefix + String.format("%08d", counter);
            counter++;
        } while (existsByCode(code));
        return code;
    }

    /**
     * Lấy withdrawals theo người dùng
     */
    public List<Transaction> getWithdrawalsByUser(User user) {
        return transactionRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user)
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .toList();
    }

    /**
     * Thống kê tổng số withdrawals trong tháng
     */
    public long getTotalWithdrawalsThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0)
                .withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23)
                .withMinute(59).withSecond(59);

        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                null, null, Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .stream()
                .filter(t -> t.getCreatedAt().isAfter(startOfMonth) && t.getCreatedAt().isBefore(endOfMonth))
                .count();
    }

    /**
     * Thống kê tổng số tiền đã rút trong tháng
     */
    public BigDecimal getTotalWithdrawalAmountThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).withHour(0)
                .withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = LocalDateTime.now().with(TemporalAdjusters.lastDayOfMonth()).withHour(23)
                .withMinute(59).withSecond(59);

        BigDecimal amount = transactionRepository.sumAmountByTypeAndStatusAndCreatedAtBetween(
                Transaction.TransactionType.WITHDRAWAL,
                Transaction.TransactionStatus.COMPLETED,
                startOfMonth,
                endOfMonth);

        return amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * Thống kê số withdrawals đang pending
     */
    public long getPendingWithdrawals() {
        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                null, Transaction.TransactionStatus.PENDING, Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .getTotalElements();
    }

    /**
     * Thống kê tỷ lệ thành công của withdrawals
     */
    public Double getWithdrawalSuccessRate() {
        Page<Transaction> allWithdrawals = transactionRepository.findByTypeAndDeletedAtIsNull(
                Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged());

        long totalWithdrawals = allWithdrawals.getTotalElements();
        if (totalWithdrawals == 0) {
            return 0.0;
        }

        long completedWithdrawals = transactionRepository.findByFiltersAndDeletedAtIsNull(
                null, Transaction.TransactionStatus.COMPLETED, Transaction.TransactionType.WITHDRAWAL,
                Pageable.unpaged())
                .getTotalElements();

        return (completedWithdrawals * 100.0) / totalWithdrawals;
    }

    /**
     * Thống kê withdrawals quá hạn xử lý (pending > 24h)
     */
    public long getOverdueWithdrawals() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        return transactionRepository.findByFiltersAndDeletedAtIsNull(
                null, Transaction.TransactionStatus.PENDING, Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .stream()
                .filter(t -> t.getCreatedAt().isBefore(yesterday))
                .count();
    }

    /**
     * Validate withdrawal trước khi lưu
     */
    public void validateWithdrawal(Transaction withdrawal) {
        if (withdrawal.getCode() == null || withdrawal.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã withdrawal không được để trống");
        }

        if (withdrawal.getType() != Transaction.TransactionType.WITHDRAWAL) {
            throw new IllegalArgumentException("Loại giao dịch phải là WITHDRAWAL");
        }

        if (withdrawal.getAmount() == null || withdrawal.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }

        // Kiểm tra số tiền tối thiểu rút
        BigDecimal minWithdrawalAmount = new BigDecimal("50000"); // 50,000 VND
        if (withdrawal.getAmount().compareTo(minWithdrawalAmount) < 0) {
            throw new IllegalArgumentException("Số tiền rút tối thiểu là " + minWithdrawalAmount + " VND");
        }

        // Kiểm tra số tiền tối đa rút
        BigDecimal maxWithdrawalAmount = new BigDecimal("50000000"); // 50,000,000 VND
        if (withdrawal.getAmount().compareTo(maxWithdrawalAmount) > 0) {
            throw new IllegalArgumentException("Số tiền rút tối đa là " + maxWithdrawalAmount + " VND");
        }

        if (withdrawal.getStatus() == null) {
            throw new IllegalArgumentException("Trạng thái withdrawal không được để trống");
        }

        if (withdrawal.getUser() == null || withdrawal.getUser().getId() == null) {
            throw new IllegalArgumentException("Người dùng không được để trống");
        }

        // Kiểm tra user có tồn tại không
        Optional<User> userOpt = userRepository.findById(withdrawal.getUser().getId());
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }

        User user = userOpt.get();

        // Kiểm tra user có bị ban không
        if ("banned".equals(user.getStatus())) {
            throw new IllegalArgumentException("Người dùng đã bị cấm không thể thực hiện withdrawal");
        }

        // Kiểm tra user có active không
        if (!"active".equals(user.getStatus())) {
            throw new IllegalArgumentException("Người dùng phải ở trạng thái active để thực hiện withdrawal");
        }

        // Kiểm tra phương thức thanh toán
        if (withdrawal.getPaymentMethod() == null || withdrawal.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống");
        }

        // Validate business rules for withdrawal status changes
        validateWithdrawalStatusChange(withdrawal);
    }

    /**
     * Validate withdrawal status changes
     */
    private void validateWithdrawalStatusChange(Transaction withdrawal) {
        if (withdrawal.getId() != null) {
            Optional<Transaction> existingOpt = transactionRepository.findById(withdrawal.getId());
            if (existingOpt.isPresent()) {
                Transaction existing = existingOpt.get();

                // Không thể thay đổi withdrawal đã completed
                if (existing.getStatus() == Transaction.TransactionStatus.COMPLETED &&
                        withdrawal.getStatus() != Transaction.TransactionStatus.COMPLETED) {
                    throw new IllegalArgumentException("Không thể thay đổi trạng thái withdrawal đã hoàn thành");
                }

                // Không thể thay đổi withdrawal đã cancelled
                if (existing.getStatus() == Transaction.TransactionStatus.CANCELLED &&
                        withdrawal.getStatus() != Transaction.TransactionStatus.CANCELLED) {
                    throw new IllegalArgumentException("Không thể thay đổi trạng thái withdrawal đã hủy");
                }
            }
        }
    }
}