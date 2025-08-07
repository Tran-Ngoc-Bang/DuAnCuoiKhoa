package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 255, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "address", columnDefinition = "nvarchar(max)")
    private String address;

    @Column(name = "bio", columnDefinition = "nvarchar(max)")
    private String bio;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "status", length = 20)
    private String status = "active";

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private LocalDateTime phoneVerifiedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 255)
    private String lastLoginIp;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "language", length = 10)
    private String language = "vi";

    @Column(name = "timezone", length = 50)
    private String timezone;

    @Column(name = "referral_code", length = 100)
    private String referralCode;

    @Column(name = "seller_status", length = 20)
    private String sellerStatus = "inactive";

    @Column(name = "consent", columnDefinition = "nvarchar(max)")
    private String consent;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "role", length = 50)
    private String role = "user";

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}