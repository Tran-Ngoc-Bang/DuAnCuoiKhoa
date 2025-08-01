package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic queries
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // Check existence
    boolean existsByUsernameAndDeletedAtIsNull(String username);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByUsernameAndIdNotAndDeletedAtIsNull(String username, Long id);

    boolean existsByEmailAndIdNotAndDeletedAtIsNull(String email, Long id);

    // Status queries
    List<User> findByStatusAndDeletedAtIsNull(String status);

    Page<User> findByStatusAndDeletedAtIsNull(String status, Pageable pageable);

    // Role queries
    List<User> findByRoleAndDeletedAtIsNull(String role);

    Page<User> findByRoleAndDeletedAtIsNull(String role, Pageable pageable);

    // Search queries
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
            "(LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    // Combined search and filter
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL " +
            "AND (:status IS NULL OR u.status = :status) " +
            "AND (:role IS NULL OR u.role = :role) " +
            "AND (:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> findUsersWithFilters(@Param("keyword") String keyword,
            @Param("status") String status,
            @Param("role") String role,
            Pageable pageable);

    // Security related queries
    List<User> findByFailedLoginAttemptsGreaterThanAndDeletedAtIsNull(Integer attempts);

    List<User> findByLockedUntilBeforeAndDeletedAtIsNull(LocalDateTime now);

    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.status = 'active'")
    Long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.role = :role")
    Long countUsersByRole(@Param("role") String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL AND u.createdAt BETWEEN :from AND :to")
    Long countUsersByDateRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}