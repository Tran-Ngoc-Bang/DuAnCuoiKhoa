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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, Long> {
        List<TransactionDetail> findByTransaction(Transaction transaction);

        List<TransactionDetail> findByCoinPackage(CoinPackage coinPackage);

        List<TransactionDetail> findByTransactionId(Long transactionId);

        @Query("SELECT COALESCE(SUM(td.coinsReceived), 0) FROM TransactionDetail td WHERE td.transaction.user.id = :userId AND td.transaction.status = 'COMPLETED'")
        Long sumTotalCoinsPurchasedByUser(@Param("userId") Long userId);

        @Query("SELECT COALESCE(SUM(td.amount), 0) FROM TransactionDetail td WHERE td.transaction.user.id = :userId AND td.detailType = :detailType AND td.transaction.status = 'COMPLETED'")
        BigDecimal sumAmountByUserAndDetailType(@Param("userId") Long userId, @Param("detailType") String detailType);

        @Query("SELECT COUNT(td) FROM TransactionDetail td WHERE td.coinPackage.id = :packageId AND td.transaction.status = 'COMPLETED'")
        long countSoldPackages(@Param("packageId") Long packageId);

        @Query("SELECT COALESCE(SUM(td.amount), 0) FROM TransactionDetail td WHERE td.coinPackage.id = :packageId AND td.transaction.status = 'COMPLETED'")
        BigDecimal sumRevenueByPackage(@Param("packageId") Long packageId);

        @Query("SELECT t.user FROM TransactionDetail td JOIN td.transaction t WHERE td.coinPackage.id = :packageId AND t.status = 'COMPLETED' ORDER BY t.createdAt DESC")
        List<User> findRecentBuyersByPackage(@Param("packageId") Long packageId, Pageable pageable);

        // ---- Document purchase history ----
        @Query("SELECT CASE WHEN COUNT(td) > 0 THEN true ELSE false END FROM TransactionDetail td WHERE td.detailType = 'document' AND td.referenceId = :documentId AND td.transaction.user = :user AND td.transaction.status = 'COMPLETED'")
        boolean existsCompletedDocumentPurchase(@Param("user") User user, @Param("documentId") Long documentId);

        @Query("SELECT td FROM TransactionDetail td WHERE td.detailType = 'document' AND td.referenceId = :documentId AND td.transaction.user = :user ORDER BY td.createdAt DESC")
        List<TransactionDetail> findDocumentPurchaseHistory(@Param("user") User user,
                        @Param("documentId") Long documentId,
                        Pageable pageable);

        // ---- Commission and Revenue Statistics ----

        @Query("SELECT COALESCE(SUM(td.amount), 0) FROM TransactionDetail td WHERE td.detailType = 'document' AND td.transaction.status = 'COMPLETED' AND td.transaction.createdAt BETWEEN :fromDate AND :toDate")
        BigDecimal sumDocumentRevenueByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        @Query("SELECT COUNT(td) FROM TransactionDetail td WHERE td.detailType = 'document' AND td.transaction.status = 'COMPLETED' AND td.transaction.createdAt BETWEEN :fromDate AND :toDate")
        long countDocumentTransactionsByDateRange(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);

        @Query("SELECT t.user.fullName as sellerName, t.user.username as sellerUsername, " +
                        "COALESCE(SUM(td.amount), 0) as totalRevenue, COUNT(td) as totalSales " +
                        "FROM TransactionDetail td JOIN td.transaction t " +
                        "WHERE td.detailType = 'document_sale' AND t.status = 'COMPLETED' " +
                        "GROUP BY t.user.id, t.user.fullName, t.user.username " +
                        "ORDER BY totalRevenue DESC")
        List<Map<String, Object>> findTopSellers(@Param("limit") int limit);

        @Query("SELECT d.title as documentTitle, d.id as documentId, " +
                        "COALESCE(SUM(td.amount), 0) as totalRevenue, COUNT(td) as totalSales " +
                        "FROM TransactionDetail td JOIN Document d ON td.referenceId = d.id " +
                        "WHERE td.detailType = 'document' AND td.transaction.status = 'COMPLETED' " +
                        "GROUP BY d.id, d.title " +
                        "ORDER BY totalRevenue DESC")
        List<Map<String, Object>> findTopSellingDocuments(@Param("limit") int limit);

        @Query("SELECT DATE(td.transaction.createdAt) as saleDate, " +
                        "COALESCE(SUM(td.amount), 0) as dailyRevenue, " +
                        "COUNT(td) as dailyTransactions " +
                        "FROM TransactionDetail td " +
                        "WHERE td.detailType = 'document' AND td.transaction.status = 'COMPLETED' " +
                        "AND td.transaction.createdAt BETWEEN :fromDate AND :toDate " +
                        "GROUP BY DATE(td.transaction.createdAt) " +
                        "ORDER BY saleDate")
        Map<String, Object> getDailyCommissionStats(@Param("fromDate") LocalDateTime fromDate,
                        @Param("toDate") LocalDateTime toDate);
}