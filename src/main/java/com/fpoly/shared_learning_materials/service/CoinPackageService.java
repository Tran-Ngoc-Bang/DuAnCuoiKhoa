package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.repository.CoinPackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CoinPackageService {

    @Autowired
    private CoinPackageRepository coinPackageRepository;

    /**
     * Lấy tất cả gói xu với phân trang
     */
    public Page<CoinPackage> getAllPackages(Pageable pageable) {
        return coinPackageRepository.findAllActive(pageable);
    }

    /**
     * Tìm kiếm gói xu theo keyword (tên hoặc mã)
     */
    public Page<CoinPackage> searchPackages(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPackages(pageable);
        }
        return coinPackageRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndDeletedAtIsNull(
                keyword.trim(), keyword.trim(), pageable);
    }

    /**
     * Lọc gói xu theo trạng thái
     */
    public Page<CoinPackage> getPackagesByStatus(CoinPackage.PackageStatus status, Pageable pageable) {
        return coinPackageRepository.findByStatusAndDeletedAtIsNull(status, pageable);
    }

    /**
     * Tìm kiếm và lọc gói xu
     */
    public Page<CoinPackage> searchAndFilterPackages(String keyword, CoinPackage.PackageStatus status,
            Pageable pageable) {
        if (status != null && (keyword != null && !keyword.trim().isEmpty())) {
            return coinPackageRepository
                    .findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(
                            keyword.trim(), keyword.trim(), status, pageable);
        } else if (status != null) {
            return getPackagesByStatus(status, pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            return searchPackages(keyword, pageable);
        } else {
            return getAllPackages(pageable);
        }
    }

    /**
     * Lấy gói xu theo ID
     */
    public Optional<CoinPackage> getPackageById(Long id) {
        return coinPackageRepository.findById(id);
    }

    /**
     * Lấy gói xu theo mã
     */
    public Optional<CoinPackage> getPackageByCode(String code) {
        return coinPackageRepository.findByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Tạo gói xu mới
     */
    public CoinPackage createPackage(CoinPackage coinPackage) {
        validatePackage(coinPackage);
        return coinPackageRepository.save(coinPackage);
    }

    /**
     * Cập nhật gói xu
     */
    public CoinPackage updatePackage(CoinPackage coinPackage) {
        validatePackage(coinPackage);
        return coinPackageRepository.save(coinPackage);
    }

    /**
     * Xóa gói xu (soft delete)
     */
    public void deletePackage(Long id) {
        Optional<CoinPackage> packageOpt = coinPackageRepository.findById(id);
        if (packageOpt.isPresent()) {
            CoinPackage coinPackage = packageOpt.get();
            coinPackage.setDeletedAt(LocalDateTime.now());
            coinPackageRepository.save(coinPackage);
        }
    }

    /**
     * Xóa nhiều gói xu
     */
    public void deletePackages(List<Long> ids) {
        List<CoinPackage> packages = coinPackageRepository.findAllById(ids);
        packages.forEach(pkg -> pkg.setDeletedAt(LocalDateTime.now()));
        coinPackageRepository.saveAll(packages);
    }

    /**
     * Cập nhật trạng thái nhiều gói xu
     */
    public void updatePackagesStatus(List<Long> ids, CoinPackage.PackageStatus status) {
        List<CoinPackage> packages = coinPackageRepository.findAllById(ids);
        packages.forEach(pkg -> pkg.setStatus(status));
        coinPackageRepository.saveAll(packages);
    }

    /**
     * Kiểm tra mã gói xu có tồn tại không
     */
    public boolean existsByCode(String code) {
        return coinPackageRepository.existsByCodeAndDeletedAtIsNull(code);
    }

    /**
     * Tạo mã gói xu tự động
     */
    public String generatePackageCode(String name) {
        String prefix = "CP";
        if (name != null && !name.trim().isEmpty()) {
            String[] words = name.trim().split("\\s+");
            if (words.length > 1) {
                prefix = words[0].substring(0, 1).toUpperCase() +
                        words[1].substring(0, 1).toUpperCase();
            } else {
                prefix = name.substring(0, Math.min(2, name.length())).toUpperCase();
            }
        }

        // Tìm số thứ tự tiếp theo
        int counter = 1;
        String code;
        do {
            code = prefix + String.format("%03d", counter);
            counter++;
        } while (existsByCode(code));

        return code;
    }

    /**
     * Lấy các gói xu active cho client
     */
    public List<CoinPackage> getActivePackagesForClient() {
        return coinPackageRepository.findByStatusAndDeletedAtIsNull(CoinPackage.PackageStatus.ACTIVE);
    }

    /**
     * Validate gói xu trước khi lưu
     */
    public void validatePackage(CoinPackage coinPackage) {
        if (coinPackage.getCode() == null || coinPackage.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã gói xu không được để trống");
        }

        if (coinPackage.getName() == null || coinPackage.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên gói xu không được để trống");
        }

        if (coinPackage.getCoinAmount() == null || coinPackage.getCoinAmount() <= 0) {
            throw new IllegalArgumentException("Số xu phải lớn hơn 0");
        }

        if (coinPackage.getOriginalPrice() == null || coinPackage.getOriginalPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Giá gốc phải lớn hơn 0");
        }

        if (coinPackage.getSalePrice() == null || coinPackage.getSalePrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0");
        }

        if (coinPackage.getSalePrice().compareTo(coinPackage.getOriginalPrice()) > 0) {
            throw new IllegalArgumentException("Giá bán không được lớn hơn giá gốc");
        }
    }
}