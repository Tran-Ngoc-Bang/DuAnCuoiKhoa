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
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class WithdrawalService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private EmailConfigService emailConfigService;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Value("${promo.withdraw.enabled:false}")
    private boolean promoEnabled;

    @Value("${promo.withdraw.discount:0.5}")
    private double promoDiscount; // 0.5 = 50%

    // -------- Helper flags --------
    public boolean isUserPremium(User user) {
        if (user == null)
            return false;
        // Hệ thống chỉ có ADMIN/USER. Premium được suy ra theo ngưỡng tích lũy.
        // Quy ước đơn giản: tổng xu đã mua >= 500 thì coi như premium benefits.
        try {
            java.math.BigDecimal purchased = user.getTotalCoinsPurchased();
            return purchased != null && purchased.compareTo(new java.math.BigDecimal("500")) >= 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean isFirstWithdrawal(User user) {
        if (user == null || user.getId() == null)
            return false;
        long count = transactionRepository.countByUserAndTypeAndDeletedAtIsNull(user,
                Transaction.TransactionType.WITHDRAWAL);
        return count == 0;
    }

    public boolean isPromotionActive() {
        return promoEnabled && promoDiscount > 0;
    }

    public boolean isNewUser(User user) {
        if (user == null)
            return false;
        LocalDateTime created = user.getCreatedAt();
        return created != null && created.isAfter(LocalDateTime.now().minusDays(30));
    }

    // -------- Fee rules --------
    public java.math.BigDecimal getBaseFeePercentage(java.math.BigDecimal coinAmount) {
        if (coinAmount == null)
            return java.math.BigDecimal.ZERO;
        if (coinAmount.compareTo(new java.math.BigDecimal("500")) > 0) {
            return new java.math.BigDecimal("0.02"); // >500 xu => 2%
        } else if (coinAmount.compareTo(new java.math.BigDecimal("200")) > 0) {
            return new java.math.BigDecimal("0.03"); // 201-500 xu => 3%
        } else {
            return new java.math.BigDecimal("0.05"); // 50-200 xu => 5%
        }
    }

    public java.math.BigDecimal getEffectiveFeePercentage(User user, java.math.BigDecimal coinAmount) {
        // First withdrawal => free
        if (isFirstWithdrawal(user)) {
            return java.math.BigDecimal.ZERO;
        }

        java.math.BigDecimal pct = getBaseFeePercentage(coinAmount);

        // Premium discount: -0.5%
        if (isUserPremium(user)) {
            pct = pct.subtract(new java.math.BigDecimal("0.005"));
            if (pct.compareTo(java.math.BigDecimal.ZERO) < 0)
                pct = java.math.BigDecimal.ZERO;
        }

        // Promo 50% off fee
        if (isPromotionActive() && promoDiscount > 0) {
            pct = pct.multiply(new java.math.BigDecimal(1 - promoDiscount));
        }
        return pct.max(java.math.BigDecimal.ZERO);
    }

    public java.math.BigDecimal calculateWithdrawalFee(User user, java.math.BigDecimal coinAmount) {
        if (coinAmount == null || coinAmount.compareTo(java.math.BigDecimal.ZERO) <= 0)
            return java.math.BigDecimal.ZERO;
        java.math.BigDecimal pct = getEffectiveFeePercentage(user, coinAmount);
        return coinAmount.multiply(pct);
    }

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

    // ---------- Dashboard stats for Requirement 3.1 ----------
    public Map<String, Long> getWithdrawalStatusCounts() {
        Map<String, Long> stats = new HashMap<>();
        long total = transactionRepository
                .findByTypeAndDeletedAtIsNull(Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .getTotalElements();
        long pending = transactionRepository.findByFiltersAndDeletedAtIsNull(null,
                Transaction.TransactionStatus.PENDING, Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .getTotalElements();
        long completed = transactionRepository.findByFiltersAndDeletedAtIsNull(null,
                Transaction.TransactionStatus.COMPLETED, Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged())
                .getTotalElements();
        long failed = transactionRepository.findByFiltersAndDeletedAtIsNull(null, Transaction.TransactionStatus.FAILED,
                Transaction.TransactionType.WITHDRAWAL, Pageable.unpaged()).getTotalElements();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("completed", completed);
        stats.put("failed", failed);
        return stats;
    }

    // ---------- Risk scoring for Requirement 3.2, 3.3, 3.6, 3.7 ----------
    public int calculateRiskScore(Transaction withdrawal) {
        if (withdrawal == null)
            return 0;
        User user = withdrawal.getUser();
        int score = 0;

        // High amount
        if (withdrawal.getAmount() != null && withdrawal.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            score += 40;
        }

        // New user boost
        if (isNewUser(user)) {
            score += 20;
        }

        // History: number of withdrawals total
        List<Transaction> userTx = transactionRepository
                .findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user)
                .stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .collect(Collectors.toList());
        int totalWithdrawals = userTx.size();
        score += Math.min(totalWithdrawals * 2, 20); // up to +20

        // Frequency: last 7 days
        LocalDateTime last7d = LocalDateTime.now().minusDays(7);
        long recent7 = userTx.stream().filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(last7d))
                .count();
        score += Math.min((int) recent7 * 5, 20); // up to +20

        return Math.min(score, 100);
    }

    public String riskLevel(int score) {
        if (score >= 70)
            return "high";
        if (score >= 40)
            return "medium";
        return "low";
    }

    public boolean isHighAmount(Transaction withdrawal) {
        return withdrawal != null && withdrawal.getAmount() != null
                && withdrawal.getAmount().compareTo(new BigDecimal("1000")) > 0;
    }

    public boolean isLowRisk(Transaction withdrawal) {
        return calculateRiskScore(withdrawal) < 40; // low threshold
    }

    public Map<String, Object> getRiskInfo(Transaction withdrawal) {
        Map<String, Object> info = new HashMap<>();
        int score = calculateRiskScore(withdrawal);
        info.put("score", score);
        info.put("level", riskLevel(score));
        info.put("highAmount", isHighAmount(withdrawal));
        info.put("isNewUser", isNewUser(withdrawal != null ? withdrawal.getUser() : null));
        return info;
    }

    public List<Long> filterLowRiskWithdrawalIds(List<Long> ids) {
        List<Transaction> list = transactionRepository.findAllById(ids).stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .collect(Collectors.toList());
        return list.stream().filter(this::isLowRisk).map(Transaction::getId).collect(Collectors.toList());
    }

    // ---------- Approve/Reject with side-effects for Requirement 3.4, 3.5
    // ----------
    public String generatePaymentInstruction(Transaction withdrawal) {
        String method = withdrawal.getPaymentMethod() != null ? withdrawal.getPaymentMethod() : "BANK_TRANSFER";
        BigDecimal vnd = calculateVndAmount(withdrawal.getUser(), withdrawal.getAmount());
        return "Thanh toán qua " + method + " số tiền " + vnd + " VND cho user " + withdrawal.getUser().getEmail();
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
            throw new IllegalArgumentException("Số xu phải lớn hơn 0");
        }

        // Kiểm tra số xu tối thiểu rút (50 xu)
        BigDecimal minWithdrawalAmount = new BigDecimal("50");
        if (withdrawal.getAmount().compareTo(minWithdrawalAmount) < 0) {
            throw new IllegalArgumentException("Số xu rút tối thiểu là " + minWithdrawalAmount + " xu");
        }

        // Kiểm tra số xu tối đa rút (50,000 xu)
        BigDecimal maxWithdrawalAmount = new BigDecimal("50000");
        if (withdrawal.getAmount().compareTo(maxWithdrawalAmount) > 0) {
            throw new IllegalArgumentException("Số xu rút tối đa là " + maxWithdrawalAmount + " xu");
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
            throw new IllegalArgumentException("Tài khoản đã bị khóa");
        }

        // Kiểm tra số dư coin có đủ không (bao gồm cả phí)
        BigDecimal userBalance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;
        BigDecimal withdrawalFee = calculateWithdrawalFee(user, withdrawal.getAmount());
        BigDecimal totalRequired = withdrawal.getAmount().add(withdrawalFee);

        if (userBalance.compareTo(totalRequired) < 0) {
            throw new IllegalArgumentException("Số dư xu không đủ để thực hiện giao dịch. Cần " + totalRequired
                    + " xu (bao gồm phí " + withdrawalFee + " xu), hiện có " + userBalance + " xu");
        }

        // Validate status changes
        validateWithdrawalStatusChange(withdrawal);
    }

    /**
     * Tạo withdrawal mới với kiểm tra balance và trừ coin
     */
    @Transactional
    public Transaction createWithdrawalWithBalanceCheck(Transaction withdrawal) {
        // Set type to WITHDRAWAL
        withdrawal.setType(Transaction.TransactionType.WITHDRAWAL);

        // Tự động generate code nếu chưa có
        if (withdrawal.getCode() == null || withdrawal.getCode().trim().isEmpty()) {
            withdrawal.setCode(generateWithdrawalCode());
        }

        // Validate withdrawal
        validateWithdrawal(withdrawal);

        // Set default status nếu chưa có
        if (withdrawal.getStatus() == null) {
            withdrawal.setStatus(Transaction.TransactionStatus.PENDING);
        }

        // Trừ coin từ user balance ngay khi tạo withdrawal
        User user = withdrawal.getUser();
        BigDecimal userBalance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;
        BigDecimal withdrawalFee = calculateWithdrawalFee(user, withdrawal.getAmount());
        BigDecimal totalDeducted = withdrawal.getAmount().add(withdrawalFee);

        if (userBalance.compareTo(totalDeducted) < 0) {
            throw new IllegalArgumentException("Số dư xu không đủ để thực hiện giao dịch");
        }

        // Trừ coin từ balance
        user.setCoinBalance(userBalance.subtract(totalDeducted));
        userRepository.save(user);

        // Lưu withdrawal
        return transactionRepository.save(withdrawal);
    }

    /**
     * Approve withdrawal và cập nhật transaction status
     */
    @Transactional
    public void approve(Transaction withdrawal, String adminNote) {
        // Kiểm tra trạng thái hiện tại
        if (withdrawal.getStatus() != Transaction.TransactionStatus.PENDING
                && withdrawal.getStatus() != Transaction.TransactionStatus.PROCESSING) {
            throw new IllegalArgumentException("Chỉ có thể approve withdrawal đang ở trạng thái PENDING/PROCESSING");
        }

        // Cập nhật status thành COMPLETED
        withdrawal.setStatus(Transaction.TransactionStatus.COMPLETED);

        // Thêm ghi chú admin
        String timestamp = LocalDateTime.now().toString();
        String pi = generatePaymentInstruction(withdrawal);
        String currentNotes = withdrawal.getNotes();
        String note = (currentNotes != null ? currentNotes + "\n" : "") + "[APPROVED " + timestamp + "] "
                + (adminNote != null && !adminNote.isBlank() ? adminNote.trim() + " | " : "") + pi;
        withdrawal.setNotes(note);

        // Cập nhật thời gian hoàn thành
        withdrawal.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(withdrawal);

        // Gửi notification nội bộ
        try {
            if (notificationService != null) {
                String title = "Rút tiền hoàn tất";
                String message = "Giao dịch " + withdrawal.getCode() + " đã hoàn tất. Số tiền: "
                        + calculateVndAmount(withdrawal.getUser(), withdrawal.getAmount()) + " VND.";
                notificationService.createNotification(withdrawal.getUser(), title, message, "payout");
            }
        } catch (Exception ignored) {
        }

        // Gửi email thông báo
        try {
            if (emailConfigService != null) {
                emailConfigService.sendHtmlEmail(
                        withdrawal.getUser().getEmail(),
                        "Yêu cầu rút tiền đã được duyệt",
                        "<p>Xin chào,</p><p>Yêu cầu rút tiền " + withdrawal.getCode() + " của bạn đã được duyệt.</p><p>"
                                + pi + "</p>");
            }
        } catch (Exception ignored) {
            // Log error but don't fail the transaction
        }
    }

    /**
     * Reject withdrawal và hoàn lại coin
     */
    @Transactional
    public void reject(Transaction withdrawal, String reason) {
        // Kiểm tra trạng thái hiện tại
        if (withdrawal.getStatus() != Transaction.TransactionStatus.PENDING
                && withdrawal.getStatus() != Transaction.TransactionStatus.PROCESSING) {
            throw new IllegalArgumentException("Chỉ có thể reject withdrawal đang ở trạng thái PENDING/PROCESSING");
        }

        // Cập nhật status thành FAILED
        withdrawal.setStatus(Transaction.TransactionStatus.FAILED);

        // Thêm ghi chú reject
        String timestamp = LocalDateTime.now().toString();
        String currentNotes = withdrawal.getNotes();
        String note = (currentNotes != null ? currentNotes + "\n" : "") + "[REJECTED " + timestamp + "] Lý do: "
                + (reason == null ? "" : reason.trim());
        withdrawal.setNotes(note);

        // Cập nhật thời gian
        withdrawal.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(withdrawal);

        // Hoàn lại coin cho user
        User user = withdrawal.getUser();
        BigDecimal userBalance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;
        BigDecimal withdrawalFee = calculateConsistentFeeForRefund(withdrawal);
        BigDecimal totalRefunded = withdrawal.getAmount().add(withdrawalFee);

        user.setCoinBalance(userBalance.add(totalRefunded));
        userRepository.save(user);

        // Gửi email thông báo
        try {
            if (emailConfigService != null) {
                emailConfigService.sendHtmlEmail(
                        withdrawal.getUser().getEmail(),
                        "Yêu cầu rút tiền bị từ chối",
                        "<p>Xin chào,</p><p>Rất tiếc, yêu cầu rút tiền " + withdrawal.getCode()
                                + " của bạn đã bị từ chối.</p><p>Lý do: " + (reason == null ? "" : reason)
                                + "</p><p>Số xu đã được hoàn lại vào tài khoản (" + totalRefunded + " xu).</p>");
            }
        } catch (Exception ignored) {
            // Log error but don't fail the transaction
        }
    }

    /**
     * Cancel withdrawal (chỉ user mới có thể cancel)
     */
    @Transactional
    public void cancelWithdrawal(Transaction withdrawal) {
        // Chỉ có thể cancel withdrawal ở trạng thái PENDING
        if (withdrawal.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể hủy withdrawal đang ở trạng thái PENDING");
        }

        // Cập nhật status thành CANCELLED
        withdrawal.setStatus(Transaction.TransactionStatus.CANCELLED);

        // Thêm ghi chú cancel
        String timestamp = LocalDateTime.now().toString();
        String currentNotes = withdrawal.getNotes();
        String note = (currentNotes != null ? currentNotes + "\n" : "") + "[CANCELLED " + timestamp
                + "] Hủy bởi người dùng";
        withdrawal.setNotes(note);

        // Cập nhật thời gian
        withdrawal.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(withdrawal);

        // Hoàn lại coin cho user
        User user = withdrawal.getUser();
        BigDecimal userBalance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;
        BigDecimal withdrawalFee = calculateConsistentFeeForRefund(withdrawal);
        BigDecimal totalRefunded = withdrawal.getAmount().add(withdrawalFee);

        user.setCoinBalance(userBalance.add(totalRefunded));
        userRepository.save(user);
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

    public BigDecimal calculateWithdrawalFee(BigDecimal coinAmount) {
        // Backward-compatible: base fee without user context
        BigDecimal pct = getBaseFeePercentage(coinAmount);
        return coinAmount == null ? BigDecimal.ZERO : coinAmount.multiply(pct);
    }

    /**
     * Tính toán số xu thực nhận sau khi trừ phí
     */
    public BigDecimal calculateNetAmount(BigDecimal coinAmount) {
        if (coinAmount == null || coinAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = calculateWithdrawalFee(coinAmount);
        return coinAmount.subtract(fee);
    }

    public BigDecimal calculateNetAmount(User user, BigDecimal coinAmount) {
        if (coinAmount == null || coinAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal fee = calculateWithdrawalFee(user, coinAmount);
        return coinAmount.subtract(fee);
    }

    /**
     * Tính toán số tiền VND tương ứng
     */
    public BigDecimal calculateVndAmount(BigDecimal coinAmount) {
        if (coinAmount == null || coinAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal netAmount = calculateNetAmount(coinAmount);
        BigDecimal exchangeRate = new BigDecimal("1000"); // 1 xu = 1000 VND
        return netAmount.multiply(exchangeRate);
    }

    public BigDecimal calculateVndAmount(User user, BigDecimal coinAmount) {
        if (coinAmount == null || coinAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal netAmount = calculateNetAmount(user, coinAmount);
        return netAmount.multiply(getExchangeRate());
    }

    /**
     * Lấy tỷ giá quy đổi hiện tại
     */
    public BigDecimal getExchangeRate() {
        return new BigDecimal("1000"); // 1 xu = 1000 VND
    }

    /**
     * Lấy số xu tối thiểu để rút
     */
    public BigDecimal getMinWithdrawalAmount() {
        return new BigDecimal("50");
    }

    /**
     * Lấy số xu tối đa để rút
     */
    public BigDecimal getMaxWithdrawalAmount() {
        return new BigDecimal("50000");
    }

    // -------- Trust & ETA helpers --------
    public boolean isTrustedUser(User user) {
        if (user == null)
            return false;
        // Trusted nếu có >=3 withdrawal COMPLETED và không có FAILED trong 90 ngày
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
        List<Transaction> history = transactionRepository.findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user);
        long completed = history.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .filter(t -> t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(ninetyDaysAgo))
                .count();
        boolean hasFailed = history.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                .anyMatch(t -> t.getStatus() == Transaction.TransactionStatus.FAILED
                        && t.getCreatedAt() != null && t.getCreatedAt().isAfter(ninetyDaysAgo));
        return completed >= 3 && !hasFailed;
    }

    public LocalDateTime estimateCompletionAt(User user, BigDecimal coinAmount) {
        // Base: 24h. Trusted -20%, Premium -10%, Low amount (<200) -20%, High risk
        // +50%.
        double hours = 24.0;
        if (isTrustedUser(user))
            hours *= 0.8;
        if (isUserPremium(user))
            hours *= 0.9;
        if (coinAmount != null && coinAmount.compareTo(new BigDecimal("200")) < 0)
            hours *= 0.8;
        // Risk based on a mock temp transaction
        Transaction temp = new Transaction();
        temp.setUser(user);
        temp.setAmount(coinAmount != null ? coinAmount : BigDecimal.ZERO);
        int risk = calculateRiskScore(temp);
        if (risk >= 70)
            hours *= 1.5;
        else if (risk >= 40)
            hours *= 1.2;
        long rounded = Math.max(2, (long) Math.round(hours));
        return LocalDateTime.now().plusHours(rounded);
    }

    public Map<String, Object> getEtaInfo(User user, BigDecimal coinAmount) {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime eta = estimateCompletionAt(user, coinAmount);
        map.put("estimatedCompletionAt", eta);
        long hoursLeft = java.time.Duration.between(LocalDateTime.now(), eta).toHours();
        map.put("etaHours", Math.max(0, hoursLeft));
        map.put("trustedUser", isTrustedUser(user));
        return map;
    }

    private java.math.BigDecimal extractFeeFromNotes(Transaction withdrawal) {
        try {
            if (withdrawal == null || withdrawal.getNotes() == null)
                return null;
            String notes = withdrawal.getNotes();
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("Phí:\\s*([0-9]+(?:\\.[0-9]+)?) xu")
                    .matcher(notes);
            if (m.find()) {
                return new java.math.BigDecimal(m.group(1));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean isFirstWithdrawalBefore(User user, LocalDateTime timePoint) {
        if (user == null)
            return false;
        try {
            return transactionRepository
                    .findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(user)
                    .stream()
                    .filter(t -> t.getType() == Transaction.TransactionType.WITHDRAWAL)
                    .filter(t -> t.getCreatedAt() != null
                            && (timePoint == null || t.getCreatedAt().isBefore(timePoint)))
                    .count() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private java.math.BigDecimal calculateConsistentFeeForRefund(Transaction withdrawal) {
        // Prefer fee parsed from notes (the exact fee at creation time)
        java.math.BigDecimal parsed = extractFeeFromNotes(withdrawal);
        if (parsed != null)
            return parsed;
        // Fallback: recompute fee but treat "first withdrawal" relative to time before
        // this tx
        User user = withdrawal.getUser();
        java.math.BigDecimal pct = getBaseFeePercentage(withdrawal.getAmount());
        if (isUserPremium(user)) {
            pct = pct.subtract(new java.math.BigDecimal("0.005")).max(java.math.BigDecimal.ZERO);
        }
        if (isFirstWithdrawalBefore(user, withdrawal.getCreatedAt())) {
            // First withdrawal at that time → free
            return java.math.BigDecimal.ZERO;
        }
        return withdrawal.getAmount().multiply(pct);
    }
}