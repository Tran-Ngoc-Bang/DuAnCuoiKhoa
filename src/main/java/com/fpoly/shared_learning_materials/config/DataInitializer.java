package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.CoinPackageRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CoinPackageRepository coinPackageRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) throws Exception {
                // Initialize sample data
                if (!userRepository.existsByUsernameAndDeletedAtIsNull("admin")) {
                        createSampleUsers();
                        System.out.println("Sample users created successfully!");
                } else {
                        System.out.println("Sample users already exist, skipping user initialization.");
                }

                if (!coinPackageRepository.existsByCodeAndDeletedAtIsNull("CP001")) {
                        createSampleCoinPackages();
                        System.out.println("Sample coin packages created successfully!");
                } else {
                        System.out.println("Sample coin packages already exist, skipping package initialization.");
                }
        }

        private void createSampleUsers() {
                // Create admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("password"));
                admin.setFullName("Administrator");
                admin.setRole("ADMIN");
                admin.setStatus("active");
                admin.setCoinBalance(0);
                admin.setTotalSpent(BigDecimal.ZERO);
                admin.setTotalCoinsPurchased(0);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);

                // Create user1
                User user1 = new User();
                user1.setUsername("user1");
                user1.setEmail("user1@example.com");
                user1.setPasswordHash(passwordEncoder.encode("password"));
                user1.setFullName("Người dùng 1");
                user1.setRole("USER");
                user1.setStatus("active");
                user1.setCoinBalance(275);
                user1.setTotalSpent(new BigDecimal("158000.00"));
                user1.setTotalCoinsPurchased(350);
                user1.setCreatedAt(LocalDateTime.now());
                user1.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user1);

                // Create user2
                User user2 = new User();
                user2.setUsername("user2");
                user2.setEmail("user2@example.com");
                user2.setPasswordHash(passwordEncoder.encode("password"));
                user2.setFullName("Người dùng 2");
                user2.setRole("USER");
                user2.setStatus("active");
                user2.setCoinBalance(100);
                user2.setTotalSpent(new BigDecimal("50000.00"));
                user2.setTotalCoinsPurchased(100);
                user2.setCreatedAt(LocalDateTime.now());
                user2.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user2);
        }

        private void createSampleCoinPackages() {
                // Get admin user for createdBy field
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);

                // Create sample coin packages
                CoinPackage[] packages = {
                                createCoinPackage("CP001", "Gói Starter", "Gói xu cơ bản cho người mới bắt đầu",
                                                100, new BigDecimal("50000.00"), new BigDecimal("50000.00"),
                                                BigDecimal.ZERO, 0,
                                                CoinPackage.PackageStatus.ACTIVE, 1, admin),

                                createCoinPackage("CP002", "Gói Standard", "Gói xu tiêu chuẩn với ưu đãi 10%",
                                                250, new BigDecimal("120000.00"), new BigDecimal("108000.00"),
                                                new BigDecimal("10.00"), 25,
                                                CoinPackage.PackageStatus.ACTIVE, 2, admin),

                                createCoinPackage("CP003", "Gói Premium", "Gói xu cao cấp với nhiều ưu đãi",
                                                500, new BigDecimal("200000.00"), new BigDecimal("170000.00"),
                                                new BigDecimal("15.00"), 75,
                                                CoinPackage.PackageStatus.ACTIVE, 3, admin),

                                createCoinPackage("CP004", "Gói VIP", "Gói xu VIP với ưu đãi tốt nhất",
                                                1000, new BigDecimal("350000.00"), new BigDecimal("280000.00"),
                                                new BigDecimal("20.00"), 200,
                                                CoinPackage.PackageStatus.PROMOTION, 4, admin),

                                createCoinPackage("CP005", "Gói Mega", "Gói xu lớn cho doanh nghiệp",
                                                2500, new BigDecimal("800000.00"), new BigDecimal("720000.00"),
                                                new BigDecimal("10.00"), 250,
                                                CoinPackage.PackageStatus.ACTIVE, 5, admin),

                                createCoinPackage("CP006", "Gói Test", "Gói xu test - tạm ngưng",
                                                50, new BigDecimal("25000.00"), new BigDecimal("25000.00"),
                                                BigDecimal.ZERO, 0,
                                                CoinPackage.PackageStatus.INACTIVE, 6, admin),

                                createCoinPackage("CP007", "Gói Flash Sale", "Gói xu khuyến mãi đặc biệt",
                                                300, new BigDecimal("150000.00"), new BigDecimal("99000.00"),
                                                new BigDecimal("34.00"), 50,
                                                CoinPackage.PackageStatus.PROMOTION, 7, admin),

                                createCoinPackage("CP008", "Gói Student", "Gói xu dành cho sinh viên",
                                                150, new BigDecimal("60000.00"), new BigDecimal("54000.00"),
                                                new BigDecimal("10.00"), 15,
                                                CoinPackage.PackageStatus.ACTIVE, 8, admin),

                                createCoinPackage("CP009", "Gói Teacher", "Gói xu dành cho giáo viên",
                                                400, new BigDecimal("160000.00"), new BigDecimal("144000.00"),
                                                new BigDecimal("10.00"), 40,
                                                CoinPackage.PackageStatus.ACTIVE, 9, admin),

                                createCoinPackage("CP010", "Gói Enterprise", "Gói xu dành cho doanh nghiệp lớn",
                                                5000, new BigDecimal("1800000.00"), new BigDecimal("1620000.00"),
                                                new BigDecimal("10.00"), 500,
                                                CoinPackage.PackageStatus.ACTIVE, 10, admin)
                };

                for (CoinPackage pkg : packages) {
                        coinPackageRepository.save(pkg);
                }
        }

        private CoinPackage createCoinPackage(String code, String name, String description,
                        Integer coinAmount, BigDecimal originalPrice, BigDecimal salePrice,
                        BigDecimal discountPercent, Integer bonusCoins,
                        CoinPackage.PackageStatus status, Integer sortOrder, User createdBy) {
                CoinPackage pkg = new CoinPackage();
                pkg.setCode(code);
                pkg.setName(name);
                pkg.setDescription(description);
                pkg.setCoinAmount(coinAmount);
                pkg.setOriginalPrice(originalPrice);
                pkg.setSalePrice(salePrice);
                pkg.setDiscountPercent(discountPercent);
                pkg.setCurrency("VND");
                pkg.setBonusCoins(bonusCoins);
                pkg.setStatus(status);
                pkg.setSortOrder(sortOrder);
                pkg.setCreatedBy(createdBy);
                pkg.setCreatedAt(LocalDateTime.now());
                pkg.setUpdatedAt(LocalDateTime.now());
                return pkg;
        }
}