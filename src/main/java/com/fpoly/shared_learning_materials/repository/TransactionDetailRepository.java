package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.TransactionDetail;
import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
    List<TransactionDetail> findByTransaction(Transaction transaction);

    List<TransactionDetail> findByCoinPackage(CoinPackage coinPackage);

    List<TransactionDetail> findByTransactionId(Long transactionId);

    @Query("SELECT COALESCE(SUM(td.coinsReceived), 0) FROM TransactionDetail td WHERE td.transaction.user.id = :userId AND td.transaction.status = 'COMPLETED'")
    Long sumTotalCoinsPurchasedByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(td) FROM TransactionDetail td WHERE td.coinPackage.id = :packageId AND td.transaction.status = 'COMPLETED'")
    long countSoldPackages(@Param("packageId") Long packageId);

    @Query("SELECT COALESCE(SUM(td.amount), 0) FROM TransactionDetail td WHERE td.coinPackage.id = :packageId AND td.transaction.status = 'COMPLETED'")
    BigDecimal sumRevenueByPackage(@Param("packageId") Long packageId);

    @Query("SELECT t.user FROM TransactionDetail td JOIN td.transaction t WHERE td.coinPackage.id = :packageId AND t.status = 'COMPLETED' ORDER BY t.createdAt DESC")
    List<User> findRecentBuyersByPackage(@Param("packageId") Long packageId, Pageable pageable);
}