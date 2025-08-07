package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end AND u.deletedAt IS NULL")
    long countNewUsersBetween(LocalDateTime start, LocalDateTime end);

    // Lưu và cập nhật người dùng
    @Override
    User save(User user);

    // Xóa theo ID
    void deleteById(Long id);

    // Tìm tất cả người dùng
    @Override
    List<User> findAll();

    // Tìm theo ID (Optional để tránh NullPointerException)
    Optional<User> findById(Long id);

    // Tìm người dùng theo email
    List<User> findByEmail(String email);

    User findUserByEmail(String email);

    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);

    // Kiểm tra username đã tồn tại
    boolean existsByUsername(String username);

    // Phân trang danh sách người dùng
    Page<User> findAll(Pageable pageable);

    // Tìm kiếm theo email hoặc tên đầy đủ (full_name) - không phân biệt hoa thường
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByEmailOrFullName(String keyword, Pageable pageable);

    // Tìm người dùng theo mã giới thiệu (referral_code)
    Optional<User> findByReferralCode(String referralCode);

}
