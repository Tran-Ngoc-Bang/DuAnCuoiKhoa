package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Tính tổng doanh thu từ các giao dịch completed - sử dụng native query
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE status = 'completed'", nativeQuery = true)
    BigDecimal getTotalRevenue();

    // Đếm số giao dịch theo status - sử dụng native query
    @Query(value = "SELECT COUNT(*) FROM transactions WHERE status = ?1", nativeQuery = true)
    long countByStatus(String status);
}