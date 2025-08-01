package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.CoinPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinPackageRepository extends JpaRepository<CoinPackage, Long> {

        // Basic queries
        Optional<CoinPackage> findByIdAndDeletedAtIsNull(Long id);

        Optional<CoinPackage> findByCodeAndDeletedAtIsNull(String code);

        boolean existsByCodeAndDeletedAtIsNull(String code);

        // Status-based queries
        Page<CoinPackage> findByStatusAndDeletedAtIsNull(CoinPackage.PackageStatus status, Pageable pageable);

        List<CoinPackage> findByStatusAndDeletedAtIsNull(CoinPackage.PackageStatus status);

        // Search queries
        Page<CoinPackage> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndDeletedAtIsNull(
                        String nameKeyword, String codeKeyword, Pageable pageable);

        // Combined search and filter
        Page<CoinPackage> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseAndStatusAndDeletedAtIsNull(
                        String nameKeyword, String codeKeyword, CoinPackage.PackageStatus status, Pageable pageable);

        // Custom queries
        @Query("SELECT cp FROM CoinPackage cp WHERE cp.deletedAt IS NULL ORDER BY cp.sortOrder ASC")
        List<CoinPackage> findAllActivePackages();

        // Override default findAll to exclude deleted
        @Query("SELECT cp FROM CoinPackage cp WHERE cp.deletedAt IS NULL")
        Page<CoinPackage> findAllActive(Pageable pageable);
}