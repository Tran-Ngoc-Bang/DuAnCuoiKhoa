package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserReferral {

    @EmbeddedId
    private UserReferralId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("referrerId")
    @JoinColumn(name = "referrer_id")
    private User referrer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("referredId")
    @JoinColumn(name = "referred_id")
    private User referred;

    @Column(name = "referral_code", nullable = false, length = 100)
    private String referralCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}