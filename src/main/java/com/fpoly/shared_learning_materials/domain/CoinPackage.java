package com.fpoly.shared_learning_materials.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "coin_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoinPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 255, nullable = false, columnDefinition = "nvarchar(max)")
    private String name;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "coin_amount", nullable = false)
    private Integer coinAmount;

    @Column(name = "original_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "sale_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal salePrice;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @Column(name = "currency", length = 10)
    private String currency = "VND";

    @Column(name = "bonus_coins")
    private Integer bonusCoins = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PackageStatus status = PackageStatus.ACTIVE;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum cho trạng thái gói xu
    public enum PackageStatus {
        ACTIVE("Hoạt động"),
        INACTIVE("Tạm ngưng"),
        PROMOTION("Khuyến mãi");

        private final String displayName;

        PackageStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper method để lấy text trạng thái
    public String getStatusText() {
        return status != null ? status.getDisplayName() : "Không xác định";
    }
}