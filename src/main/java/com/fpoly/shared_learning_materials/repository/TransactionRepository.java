package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

        // Tìm theo mã giao dịch
        Optional<Transaction> findByCodeAndDeletedAtIsNull(String code);

        // Kiểm tra mã giao dịch có tồn tại không
        boolean existsByCodeAndDeletedAtIsNull(String code);

        // Lấy tất cả giao dịch chưa bị xóa với phân trang
        Page<Transaction> findByDeletedAtIsNull(Pageable pageable);

        // Tìm kiếm theo keyword (mã giao dịch hoặc tên người dùng)
        @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL AND " +
                        "(t.code LIKE %:keyword% OR t.user.fullName LIKE %:keyword% OR t.user.email LIKE %:keyword%)")
        Page<Transaction> findByKeywordAndDeletedAtIsNull(@Param("keyword") String keyword, Pageable pageable);

        // Lọc theo trạng thái
        Page<Transaction> findByStatusAndDeletedAtIsNull(Transaction.TransactionStatus status, Pageable pageable);

        // Lọc theo loại giao dịch
        Page<Transaction> findByTypeAndDeletedAtIsNull(Transaction.TransactionType type, Pageable pageable);

        // Tìm kiếm và lọc theo nhiều tiêu chí
        @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL " +
                        "AND (:keyword IS NULL OR t.code LIKE %:keyword% OR t.user.fullName LIKE %:keyword%) " +
                        "AND (:status IS NULL OR t.status = :status) " +
                        "AND (:type IS NULL OR t.type = :type)")
        Page<Transaction> findByFiltersAndDeletedAtIsNull(
                        @Param("keyword") String keyword,
                        @Param("status") Transaction.TransactionStatus status,
                        @Param("type") Transaction.TransactionType type,
                        Pageable pageable);

        // Lọc theo khoảng thời gian
        @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL " +
                        "AND t.createdAt BETWEEN :startDate AND :endDate")
        Page<Transaction> findByDateRangeAndDeletedAtIsNull(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // Lọc theo khoảng số tiền
        @Query("SELECT t FROM Transaction t WHERE t.deletedAt IS NULL " +
                        "AND t.amount BETWEEN :minAmount AND :maxAmount")
        Page<Transaction> findByAmountRangeAndDeletedAtIsNull(
                        @Param("minAmount") java.math.BigDecimal minAmount,
                        @Param("maxAmount") java.math.BigDecimal maxAmount,
                        Pageable pageable);

        // Lấy giao dịch theo người dùng
        List<Transaction> findByUserAndDeletedAtIsNullOrderByCreatedAtDesc(
                        com.fpoly.shared_learning_materials.domain.User user);

        // Thống kê tổng số giao dịch
        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deletedAt IS NULL")
        long countActiveTransactions();

        // Thống kê tổng doanh thu
        @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deletedAt IS NULL AND t.status = 'COMPLETED'")
        java.math.BigDecimal getTotalRevenue();

        // Thống kê tỷ lệ thành công
        @Query("SELECT CASE WHEN COUNT(t) > 0 THEN " +
                        "CAST(COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) * 100.0 / COUNT(t) AS double) " +
                        "ELSE 0 END FROM Transaction t WHERE t.deletedAt IS NULL")
        Double getSuccessRate();

        // Thống kê số giao dịch chờ xử lý
        @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deletedAt IS NULL AND t.status = 'PENDING'")
        long countPendingTransactions();

        @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.status = :status AND t.createdAt BETWEEN :start AND :end AND t.deletedAt IS NULL")
        BigDecimal sumAmountByTypeAndStatusAndCreatedAtBetween(
                        @Param("type") Transaction.TransactionType type,
                        @Param("status") Transaction.TransactionStatus status,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = :type AND t.status = :status")
        BigDecimal sumAmountByTypeAndStatus(
                        @Param("type") Transaction.TransactionType type,
                        @Param("status") Transaction.TransactionStatus status);

        @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), SUM(t.amount) " +
                        "FROM Transaction t " +
                        "WHERE t.type = :type AND t.status = :status AND t.createdAt BETWEEN :start AND :end AND t.deletedAt IS NULL "
                        +
                        "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
                        "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
        List<Object[]> getMonthlyRevenueSummary(
                        @Param("type") Transaction.TransactionType type,
                        @Param("status") Transaction.TransactionStatus status,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), SUM(t.amount) " +
                        "FROM Transaction t WHERE t.type = :type AND t.status = :status " +
                        "AND t.createdAt BETWEEN :start AND :end " +
                        "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
                        "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
        List<Object[]> getMonthlyRevenue(@Param("type") Transaction.TransactionType type,
                        @Param("status") Transaction.TransactionStatus status,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

}