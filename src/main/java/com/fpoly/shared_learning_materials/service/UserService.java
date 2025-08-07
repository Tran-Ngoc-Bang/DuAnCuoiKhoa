package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository
    // PasswordEncoder passwordEncoder

    ) {
        this.userRepository = userRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    // Lấy toàn bộ người dùng
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Phân trang người dùng
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    // Tìm kiếm theo keyword (email hoặc tên đầy đủ)
    public Page<User> searchUsersByKeyword(String keyword, Pageable pageable) {
        return userRepository.searchByEmailOrFullName(keyword, pageable);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public long getNewUsersBetween(LocalDateTime start, LocalDateTime end) {
        return userRepository.countNewUsersBetween(start, end);
    }

    // // Tạo mới user
    // public User save(User user) {
    // user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    // user.setCreatedAt(LocalDateTime.now());
    // user.setUpdatedAt(LocalDateTime.now());
    // return userRepository.save(user);
    // }

    // Cập nhật user
    public User update(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    // Xóa user
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    // // Reset mật khẩu
    // public User resetPassword(String email, String newPassword) {
    // Optional<User> optional = userRepository.findByEmail(email);
    // if (optional.isPresent()) {
    // User user = optional.get();
    // user.setPasswordHash(passwordEncoder.encode(newPassword));
    // user.setUpdatedAt(LocalDateTime.now());
    // return userRepository.save(user);
    // }
    // return null;
    // }

    // // Kích hoạt user
    // public User activateUser(String code) {
    // User user = userRepository.findByReferralCode(code);
    // if (user != null && user.getEmailVerifiedAt() == null) {
    // user.setEmailVerifiedAt(LocalDateTime.now());
    // return userRepository.save(user);
    // }
    // return null;
    // }

    public String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
