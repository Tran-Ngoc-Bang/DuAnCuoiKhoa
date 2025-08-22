package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.domain.*;
import com.fpoly.shared_learning_materials.repository.*;
import com.fpoly.shared_learning_materials.util.SlugUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CoinPackageRepository coinPackageRepository;

        @Autowired
        private TransactionRepository transactionRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private CategoryHierarchyRepository categoryHierarchyRepository;

        @Autowired
        private TagRepository tagRepository;

        @Autowired
        private DocumentRepository documentRepository;

        @Autowired
        private FileRepository fileRepository;

        @Autowired
        private DocumentCategoryRepository documentCategoryRepository;

        @Autowired
        private DocumentTagRepository documentTagRepository;

        @Autowired
        private DocumentOwnerRepository documentOwnerRepository;

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Autowired
        private ReplyRepository replyRepository;

        @Autowired
        private ReportRepository reportRepository;

        @Override
        public void run(String... args) throws Exception {
                // Initialize sample data
                if (!userRepository.existsByUsernameAndDeletedAtIsNull("admin")) {
                        createSampleUsers();
                        System.out.println("Sample users created successfully!");
                } else {
                        // System.out.println("Sample users already exist, skipping user
                        // initialization.");
                }

                if (!coinPackageRepository.existsByCodeAndDeletedAtIsNull("CP001")) {
                        createSampleCoinPackages();
                        System.out.println("Sample coin packages created successfully!");
                } else {
                        // System.out.println("Sample coin packages already exist, skipping package
                        // initialization.");
                }

                if (!transactionRepository.existsByCodeAndDeletedAtIsNull("TXN000001")) {
                        createSampleTransactions();
                        System.out.println("Sample transactions and withdrawals created successfully!");
                } else {
                        // System.out.println("Sample transactions already exist, skipping transaction
                        // initialization.");
                }

                // Check and create withdrawals separately
                if (!transactionRepository.existsByCodeAndDeletedAtIsNull("WD000001")) {
                        // Get users for withdrawals
                        User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                        User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                        User user3 = userRepository.findByUsernameAndDeletedAtIsNull("user3").orElse(null);
                        User user4 = userRepository.findByUsernameAndDeletedAtIsNull("user4").orElse(null);
                        User user5 = userRepository.findByUsernameAndDeletedAtIsNull("user5").orElse(null);
                        User contributor1 = userRepository.findByUsernameAndDeletedAtIsNull("contributor1")
                                        .orElse(null);
                        User contributor2 = userRepository.findByUsernameAndDeletedAtIsNull("contributor2")
                                        .orElse(null);

                        if (user1 != null && user2 != null) {
                                createSampleWithdrawals(user1, user2, user3, user4, user5, contributor1, contributor2);
                                System.out.println("Sample withdrawals created successfully!");
                        }
                } else {
                        // System.out.println("Sample withdrawals already exist, skipping withdrawal
                        // initialization.");
                }

                if (!categoryRepository.existsByNameAndDeletedAtIsNull("Công nghệ")) {
                        createSampleCategories();
                        System.out.println("Sample categories created successfully!");
                } else {
                        // System.out.println("Sample categories already exist, skipping category
                        // initialization.");
                }

                if (!tagRepository.existsByName("beginner")) {
                        createSampleTags();
                        System.out.println("Sample tags created successfully!");
                } else {
                        // System.out.println("Sample tags already exist, skipping tag
                        // initialization.");
                }

                if (!documentRepository.existsBySlug("java-programming-guide")) {
                        createSampleDocuments();
                        System.out.println("Sample documents created successfully!");
                } else {
                        // System.out.println("Sample documents already exist, skipping document
                        // initialization.");
                }

                if (commentRepository.count() == 0) {
                        createSampleComments();
                        System.out.println("Sample comments created successfully!");
                } else {
                        // System.out.println("Sample comments already exist, skipping comment
                        // initialization.");
                }

                if (replyRepository.count() == 0) {
                        createSampleReplies();
                        System.out.println("Sample replies created successfully!");
                } else {
                        // System.out.println("Sample replies already exist, skipping reply
                        // initialization.");
                }

                if (reportRepository.count() == 0) {
                        createSampleReports();
                        System.out.println("Sample reports created successfully!");
                } else {
                        // System.out.println("Sample reports already exist, skipping comment
                        // initialization.");
                }

        }

        private void createSampleUsers() {
                int currentYear = LocalDate.now().getYear();

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(passwordEncoder.encode("password"));
                admin.setFullName("Administrator");
                admin.setRole("ADMIN");
                admin.setStatus("active");
                admin.setCoinBalance(new BigDecimal(0));
                admin.setTotalSpent(BigDecimal.ZERO);
                admin.setTotalCoinsPurchased(0);
                admin.setCreatedAt(LocalDateTime.of(currentYear, 1, 10, 10, 0));
                admin.setUpdatedAt(LocalDateTime.of(currentYear, 1, 10, 10, 0));
                userRepository.save(admin);

                User user1 = new User();
                user1.setUsername("user1");
                user1.setEmail("user1@example.com");
                user1.setPasswordHash(passwordEncoder.encode("password"));
                user1.setFullName("Người dùng 1");
                user1.setRole("USER");
                user1.setStatus("active");
                user1.setCoinBalance(new BigDecimal(275));
                user1.setTotalSpent(new BigDecimal("158000.00"));
                user1.setTotalCoinsPurchased(350);
                user1.setCreatedAt(LocalDateTime.of(currentYear, 2, 15, 10, 0));
                user1.setUpdatedAt(LocalDateTime.of(currentYear, 2, 15, 10, 0));
                userRepository.save(user1);

                User user2 = new User();
                user2.setUsername("user2");
                user2.setEmail("user2@example.com");
                user2.setPasswordHash(passwordEncoder.encode("password"));
                user2.setFullName("Người dùng 2");
                user2.setRole("USER");
                user2.setStatus("active");
                user2.setCoinBalance(new BigDecimal(100));
                user2.setTotalSpent(new BigDecimal("50000.00"));
                user2.setTotalCoinsPurchased(100);
                user2.setCreatedAt(LocalDateTime.of(currentYear, 3, 20, 10, 0));
                user2.setUpdatedAt(LocalDateTime.of(currentYear, 3, 20, 10, 0));
                userRepository.save(user2);

                User user3 = new User();
                user3.setUsername("user3");
                user3.setEmail("user3@example.com");
                user3.setPasswordHash(passwordEncoder.encode("password"));
                user3.setFullName("Người dùng 3");
                user3.setRole("USER");
                user3.setStatus("active");
                user3.setCoinBalance(new BigDecimal(50));
                user3.setTotalSpent(new BigDecimal("20000.00"));
                user3.setTotalCoinsPurchased(70);
                user3.setCreatedAt(LocalDateTime.of(currentYear, 4, 5, 10, 0));
                user3.setUpdatedAt(LocalDateTime.of(currentYear, 4, 5, 10, 0));
                userRepository.save(user3);

                User user4 = new User();
                user4.setUsername("user4");
                user4.setEmail("user4@example.com");
                user4.setPasswordHash(passwordEncoder.encode("password"));
                user4.setFullName("Người dùng 4");
                user4.setRole("USER");
                user4.setStatus("active");
                user4.setCoinBalance(new BigDecimal(0));
                user4.setTotalSpent(new BigDecimal("0.00"));
                user4.setTotalCoinsPurchased(0);
                user4.setCreatedAt(LocalDateTime.of(currentYear, 5, 18, 10, 0));
                user4.setUpdatedAt(LocalDateTime.of(currentYear, 5, 18, 10, 0));
                userRepository.save(user4);

                User user5 = new User();
                user5.setUsername("user5");
                user5.setEmail("user5@example.com");
                user5.setPasswordHash(passwordEncoder.encode("password"));
                user5.setFullName("Người dùng 5");
                user5.setRole("USER");
                user5.setStatus("active");
                user5.setCoinBalance(new BigDecimal(120));
                user5.setTotalSpent(new BigDecimal("74000.00"));
                user5.setTotalCoinsPurchased(120);
                user5.setCreatedAt(LocalDateTime.of(currentYear, 6, 22, 10, 0));
                user5.setUpdatedAt(LocalDateTime.of(currentYear, 6, 22, 10, 0));
                userRepository.save(user5);

                User contributor1 = new User();
                contributor1.setUsername("contributor1");
                contributor1.setEmail("contributor1@example.com");
                contributor1.setPasswordHash(passwordEncoder.encode("password"));
                contributor1.setFullName("Cộng tác viên 1");
                contributor1.setRole("CONTRIBUTOR");
                contributor1.setStatus("active");
                contributor1.setCoinBalance(new BigDecimal(500));
                contributor1.setTotalSpent(new BigDecimal("210000.00"));
                contributor1.setTotalCoinsPurchased(600);
                contributor1.setCreatedAt(LocalDateTime.of(currentYear, 7, 8, 10, 0));
                contributor1.setUpdatedAt(LocalDateTime.of(currentYear, 7, 8, 10, 0));
                userRepository.save(contributor1);

                User contributor2 = new User();
                contributor2.setUsername("contributor2");
                contributor2.setEmail("contributor2@example.com");
                contributor2.setPasswordHash(passwordEncoder.encode("password"));
                contributor2.setFullName("Cộng tác viên 2");
                contributor2.setRole("CONTRIBUTOR");
                contributor2.setStatus("active");
                contributor2.setCoinBalance(new BigDecimal(300));
                contributor2.setTotalSpent(new BigDecimal("110000.00"));
                contributor2.setTotalCoinsPurchased(400);
                contributor2.setCreatedAt(LocalDateTime.of(currentYear, 8, 12, 10, 0));
                contributor2.setUpdatedAt(LocalDateTime.of(currentYear, 8, 12, 10, 0));
                userRepository.save(contributor2);
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
                                                CoinPackage.PackageStatus.ACTIVE, 5, admin)
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

        private void createSampleTransactions() {
                int currentYear = LocalDate.now().getYear();

                // Get users for transactions
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                User user3 = userRepository.findByUsernameAndDeletedAtIsNull("user3").orElse(null);
                User user4 = userRepository.findByUsernameAndDeletedAtIsNull("user4").orElse(null);
                User user5 = userRepository.findByUsernameAndDeletedAtIsNull("user5").orElse(null);
                User contributor1 = userRepository.findByUsernameAndDeletedAtIsNull("contributor1").orElse(null);
                User contributor2 = userRepository.findByUsernameAndDeletedAtIsNull("contributor2").orElse(null);

                if (user1 != null && user2 != null) {
                        // Tháng 1
                        createTransaction("TXN000001", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("50000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói starter",
                                        LocalDateTime.of(currentYear, 8, 10, 9, 30));

                        // Tháng 2
                        createTransaction("TXN000002", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("100000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói premium",
                                        LocalDateTime.of(currentYear, 6, 15, 14, 45));

                        // Tháng 3
                        createTransaction("TXN000003", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("25000.00"), Transaction.TransactionStatus.PENDING,
                                        "Bank Transfer", user1, "Rút tiền về tài khoản ngân hàng",
                                        LocalDateTime.of(currentYear, 2, 5, 11, 0));

                        // Tháng 4
                        createTransaction("TXN000004", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("50000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user2, "Mua xu gói starter",
                                        LocalDateTime.of(currentYear, 1, 18, 16, 10));

                        // Create comprehensive withdrawal sample data
                        createSampleWithdrawals(user1, user2, user3, user4, user5, contributor1, contributor2);
                }
        }

        private void createSampleWithdrawals(User user1, User user2, User user3, User user4, User user5,
                        User contributor1, User contributor2) {
                LocalDateTime now = LocalDateTime.now();

                // Withdrawal 1: Pending - Chờ duyệt
                createWithdrawalTransaction("WD000001", new BigDecimal("25000.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", user1,
                                "Rút tiền về tài khoản ngân hàng Vietcombank", now.minusDays(1));

                // Withdrawal 2: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000002", new BigDecimal("150000.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor1,
                                "Rút tiền từ doanh thu bán tài liệu - Đã chuyển khoản thành công", now.minusDays(3));

                // Withdrawal 3: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000003", new BigDecimal("75000.00"),
                                Transaction.TransactionStatus.COMPLETED, "E_WALLET", user2,
                                "Rút tiền về ví MoMo - Giao dịch thành công", now.minusDays(5));

                // Withdrawal 4: Pending - Chờ duyệt (số tiền lớn)
                createWithdrawalTransaction("WD000004", new BigDecimal("500000.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", contributor2,
                                "Rút tiền doanh thu tháng - Cần xác minh thông tin ngân hàng", now.minusDays(2));

                // Withdrawal 5: Failed - Thất bại
                createWithdrawalTransaction("WD000005", new BigDecimal("80000.00"),
                                Transaction.TransactionStatus.FAILED, "BANK_TRANSFER", user3,
                                "Rút tiền thất bại - Thông tin tài khoản không chính xác", now.minusDays(7));

                // Withdrawal 6: Cancelled - Đã hủy
                createWithdrawalTransaction("WD000006", new BigDecimal("30000.00"),
                                Transaction.TransactionStatus.CANCELLED, "E_WALLET", user4,
                                "Hủy yêu cầu rút tiền theo yêu cầu của người dùng", now.minusDays(4));

                // Withdrawal 7: Completed - Đã hoàn thành (tháng trước)
                createWithdrawalTransaction("WD000007", new BigDecimal("120000.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor1,
                                "Rút tiền tháng trước - Đã chuyển khoản", now.minusDays(35));

                // Withdrawal 8: Pending - Chờ duyệt (quá hạn xử lý)
                createWithdrawalTransaction("WD000008", new BigDecimal("45000.00"),
                                Transaction.TransactionStatus.PENDING, "E_WALLET", user5,
                                "Rút tiền về ví ZaloPay - Đang chờ xử lý quá 5 ngày", now.minusDays(6));

                // Withdrawal 9: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000009", new BigDecimal("200000.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor2,
                                "Rút tiền doanh thu - Chuyển khoản ACB thành công", now.minusDays(8));

                // Withdrawal 10: Pending - Chờ duyệt (mới)
                createWithdrawalTransaction("WD000010", new BigDecimal("60000.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", user1,
                                "Rút tiền về tài khoản Techcombank", now.minusDays(1));

                // Withdrawal 11: Failed - Thất bại (lỗi hệ thống)
                createWithdrawalTransaction("WD000011", new BigDecimal("90000.00"),
                                Transaction.TransactionStatus.FAILED, "E_WALLET", user2,
                                "Rút tiền thất bại - Lỗi kết nối với nhà cung cấp dịch vụ", now.minusDays(10));

                // Withdrawal 12: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000012", new BigDecimal("35000.00"),
                                Transaction.TransactionStatus.COMPLETED, "E_WALLET", user3,
                                "Rút tiền về ví Momo - Giao dịch hoàn tất", now.minusDays(12));

                // Withdrawal 13: Pending - Chờ duyệt (cần xác minh)
                createWithdrawalTransaction("WD000013", new BigDecimal("300000.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", contributor1,
                                "Rút tiền số lượng lớn - Cần xác minh danh tính", now.minusDays(1));

                // Withdrawal 14: Completed - Đã hoàn thành (tháng trước)
                createWithdrawalTransaction("WD000014", new BigDecimal("85000.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", user4,
                                "Rút tiền tháng trước - Đã chuyển khoản BIDV", now.minusDays(40));

                // Withdrawal 15: Cancelled - Đã hủy (do user)
                createWithdrawalTransaction("WD000015", new BigDecimal("55000.00"),
                                Transaction.TransactionStatus.CANCELLED, "E_WALLET", user5,
                                "Hủy yêu cầu rút tiền - Người dùng thay đổi ý định", now.minusDays(3));

                // Withdrawal 16: Processing - Đang xử lý
                createWithdrawalTransaction("WD000016", new BigDecimal("180000.00"),
                                Transaction.TransactionStatus.PROCESSING, "BANK_TRANSFER", contributor1,
                                "Đang xử lý chuyển khoản - Đã xác minh thông tin", now.minusHours(6));

                // Withdrawal 17: Processing - Đang xử lý (ví điện tử)
                createWithdrawalTransaction("WD000017", new BigDecimal("95000.00"),
                                Transaction.TransactionStatus.PROCESSING, "E_WALLET", user2,
                                "Đang xử lý chuyển tiền về ví MoMo", now.minusHours(2));
        }

        private void createWithdrawalTransaction(String code, BigDecimal amount,
                        Transaction.TransactionStatus status, String paymentMethod, User user,
                        String notes, LocalDateTime createdAt) {
                try {
                        Transaction transaction = new Transaction();
                        transaction.setCode(code);
                        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
                        transaction.setAmount(amount);
                        transaction.setStatus(status);
                        transaction.setPaymentMethod(paymentMethod);
                        transaction.setUser(user);
                        transaction.setNotes(notes);
                        transaction.setCreatedAt(createdAt);
                        transaction.setUpdatedAt(createdAt);

                        // Save directly to repository to bypass validation during initialization
                        transactionRepository.save(transaction);
                        System.out.println("Created withdrawal: " + code + " - " + amount + " VND");

                } catch (Exception e) {
                        System.err.println("Failed to create withdrawal " + code + ": " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void createTransaction(String code, Transaction.TransactionType type,
                        BigDecimal amount, Transaction.TransactionStatus status,
                        String paymentMethod, User user, String notes, LocalDateTime createdAt) {
                Transaction transaction = new Transaction();
                transaction.setCode(code);
                transaction.setType(type);
                transaction.setAmount(amount);
                transaction.setStatus(status);
                transaction.setPaymentMethod(paymentMethod);
                transaction.setUser(user);
                transaction.setNotes(notes);
                transaction.setCreatedAt(createdAt);
                transactionRepository.save(transaction);
        }

        private void createSampleCategories() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);

                // Tạo danh mục gốc
                Category tech = createCategory("Công nghệ", "Các tài liệu về công nghệ thông tin", admin);
                Category education = createCategory("Giáo dục", "Tài liệu giáo dục và học tập", admin);
                Category business = createCategory("Kinh doanh", "Tài liệu về kinh doanh và quản lý", admin);
                createCategory("Khoa học", "Tài liệu khoa học và nghiên cứu", admin);
                createCategory("Nghệ thuật", "Tài liệu về nghệ thuật và sáng tạo", admin);

                // Tạo danh mục con cho Công nghệ
                Category programming = createCategory("Lập trình", "Tài liệu về lập trình", admin);
                Category webDev = createCategory("Phát triển Web", "Tài liệu về phát triển web", admin);
                Category mobile = createCategory("Ứng dụng di động", "Tài liệu về phát triển mobile", admin);
                Category database = createCategory("Cơ sở dữ liệu", "Tài liệu về database", admin);
                Category ai = createCategory("Trí tuệ nhân tạo", "Tài liệu về AI và Machine Learning", admin);

                // Tạo hierarchy cho Công nghệ
                createHierarchy(tech, programming, 1);
                createHierarchy(tech, webDev, 1);
                createHierarchy(tech, mobile, 1);
                createHierarchy(tech, database, 1);
                createHierarchy(tech, ai, 1);

                // Tạo danh mục con cho Lập trình
                Category java = createCategory("Java", "Tài liệu về ngôn ngữ Java", admin);
                Category python = createCategory("Python", "Tài liệu về ngôn ngữ Python", admin);
                Category javascript = createCategory("JavaScript", "Tài liệu về JavaScript", admin);
                Category csharp = createCategory("C#", "Tài liệu về C#", admin);

                createHierarchy(programming, java, 2);
                createHierarchy(programming, python, 2);
                createHierarchy(programming, javascript, 2);
                createHierarchy(programming, csharp, 2);

                // Tạo danh mục con cho Giáo dục
                Category math = createCategory("Toán học", "Tài liệu toán học", admin);
                Category physics = createCategory("Vật lý", "Tài liệu vật lý", admin);
                Category chemistry = createCategory("Hóa học", "Tài liệu hóa học", admin);
                Category literature = createCategory("Văn học", "Tài liệu văn học", admin);

                createHierarchy(education, math, 1);
                createHierarchy(education, physics, 1);
                createHierarchy(education, chemistry, 1);
                createHierarchy(education, literature, 1);

                // Tạo danh mục con cho Kinh doanh
                Category marketing = createCategory("Marketing", "Tài liệu marketing", admin);
                Category finance = createCategory("Tài chính", "Tài liệu tài chính", admin);
                Category management = createCategory("Quản lý", "Tài liệu quản lý", admin);

                createHierarchy(business, marketing, 1);
                createHierarchy(business, finance, 1);
                createHierarchy(business, management, 1);
        }

        private Category createCategory(String name, String description, User createdBy) {
                Category category = new Category();
                category.setName(name);
                category.setSlug(SlugUtils.generateSlug(name));
                category.setDescription(description);
                category.setStatus("active");
                category.setSortOrder(0);
                category.setCreatedBy(createdBy);
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                return categoryRepository.save(category);
        }

        private void createHierarchy(Category parent, Category child, int level) {
                CategoryHierarchy hierarchy = new CategoryHierarchy();
                CategoryHierarchyId id = new CategoryHierarchyId();
                id.setParentId(parent.getId());
                id.setChildId(child.getId());
                hierarchy.setId(id);
                hierarchy.setParent(parent);
                hierarchy.setChild(child);
                hierarchy.setLevel(level);
                hierarchy.setCreatedAt(LocalDateTime.now());
                categoryHierarchyRepository.save(hierarchy);
        }

        private void createSampleTags() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);

                // Tạo tags theo chủ đề
                String[] tagData = {
                                "beginner:Dành cho người mới bắt đầu",
                                "intermediate:Trình độ trung cấp",
                                "advanced:Trình độ nâng cao",
                                "tutorial:Hướng dẫn chi tiết",
                                "guide:Hướng dẫn tổng quan",
                                "example:Ví dụ thực tế",
                                "java:Ngôn ngữ lập trình Java",
                                "python:Ngôn ngữ lập trình Python",
                                "javascript:Ngôn ngữ lập trình JavaScript",
                                "web:Phát triển web",
                                "mobile:Phát triển ứng dụng di động",
                                "database:Cơ sở dữ liệu",
                                "ai:Trí tuệ nhân tạo",
                                "machine-learning:Học máy",
                                "data-science:Khoa học dữ liệu",
                                "algorithm:Thuật toán",
                                "design-pattern:Mẫu thiết kế",
                                "spring-boot:Framework Spring Boot",
                                "react:Thư viện React",
                                "angular:Framework Angular",
                                "vue:Framework Vue.js",
                                "nodejs:Môi trường Node.js",
                                "express:Framework Express.js",
                                "mysql:Cơ sở dữ liệu MySQL",
                                "postgresql:Cơ sở dữ liệu PostgreSQL",
                                "mongodb:Cơ sở dữ liệu MongoDB",
                                "redis:Cơ sở dữ liệu Redis",
                                "docker:Containerization với Docker",
                                "kubernetes:Orchestration với Kubernetes",
                                "aws:Amazon Web Services",
                                "azure:Microsoft Azure",
                                "gcp:Google Cloud Platform",
                                "devops:DevOps practices",
                                "ci-cd:Continuous Integration/Deployment",
                                "testing:Kiểm thử phần mềm",
                                "security:Bảo mật thông tin",
                                "performance:Tối ưu hiệu suất",
                                "optimization:Tối ưu hóa",
                                "best-practices:Thực hành tốt nhất",
                                "free:Tài liệu miễn phí"
                };

                for (String tagInfo : tagData) {
                        String[] parts = tagInfo.split(":");
                        String tagName = parts[0];
                        String description = parts.length > 1 ? parts[1] : "Tag về " + tagName;

                        Tag tag = new Tag();
                        tag.setName(tagName);
                        tag.setSlug(SlugUtils.generateSlug(tagName));
                        tag.setDescription(description);
                        tag.setCreatedBy(admin);
                        tag.setCreatedAt(LocalDateTime.now());
                        tagRepository.save(tag);
                }
        }

        private void createSampleDocuments() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                // Tạo documents với thông tin chi tiết
                String[][] documentData = {
                                { "Java Programming Guide", "java-programming-guide",
                                                "Hướng dẫn lập trình Java từ cơ bản đến nâng cao", "50", "APPROVED",
                                                "user1" },
                                { "Python for Beginners", "python-for-beginners",
                                                "Khóa học Python dành cho người mới bắt đầu", "0", "APPROVED",
                                                "user1" },
                                { "JavaScript ES6 Features", "javascript-es6-features",
                                                "Tìm hiểu các tính năng mới của JavaScript ES6", "30", "APPROVED",
                                                "user2" },
                                { "Web Development Roadmap", "web-development-roadmap",
                                                "Lộ trình học phát triển web 2024", "0", "APPROVED", "admin" },
                                { "Spring Boot Tutorial", "spring-boot-tutorial", "Hướng dẫn Spring Boot từ A đến Z",
                                                "75", "PENDING", "user1" },
                                { "React Hooks Guide", "react-hooks-guide", "Hướng dẫn sử dụng React Hooks hiệu quả",
                                                "40", "APPROVED", "user2" },
                                { "Database Design Principles", "database-design-principles",
                                                "Nguyên tắc thiết kế cơ sở dữ liệu", "60", "APPROVED", "admin" },
                                { "Machine Learning Basics", "machine-learning-basics",
                                                "Cơ bản về Machine Learning và AI", "100", "PENDING", "user1" },
                                { "Docker for Developers", "docker-for-developers", "Docker dành cho lập trình viên",
                                                "45", "APPROVED", "user2" },
                                { "Git Version Control", "git-version-control", "Quản lý phiên bản với Git", "0",
                                                "APPROVED", "admin" },
                                { "Node.js Backend Development", "nodejs-backend-development",
                                                "Phát triển backend với Node.js", "80", "APPROVED", "user1" },
                                { "CSS Grid Layout", "css-grid-layout", "Hướng dẫn CSS Grid Layout", "25", "DRAFT",
                                                "user2" },
                                { "Algorithm and Data Structures", "algorithm-data-structures",
                                                "Thuật toán và cấu trúc dữ liệu", "90", "APPROVED", "admin" },
                                { "REST API Design", "rest-api-design", "Thiết kế REST API chuẩn", "55", "PENDING",
                                                "user1" },
                                { "MongoDB Tutorial", "mongodb-tutorial", "Hướng dẫn MongoDB từ cơ bản", "35",
                                                "APPROVED", "user2" },
                                { "Vue.js Complete Guide", "vuejs-complete-guide", "Hướng dẫn Vue.js đầy đủ", "70",
                                                "APPROVED", "admin" },
                                { "Microservices Architecture", "microservices-architecture", "Kiến trúc Microservices",
                                                "120", "APPROVED", "user1" },
                                { "AWS Cloud Fundamentals", "aws-cloud-fundamentals", "Cơ bản về AWS Cloud", "85",
                                                "REJECTED", "user2" },
                                { "Cybersecurity Essentials", "cybersecurity-essentials", "Bảo mật thông tin cơ bản",
                                                "95", "APPROVED", "admin" },
                                { "Mobile App Development", "mobile-app-development", "Phát triển ứng dụng di động",
                                                "110", "DRAFT", "user1" }
                };

                for (int i = 0; i < documentData.length; i++) {
                        String[] data = documentData[i];
                        String title = data[0];
                        String slug = data[1];
                        String description = data[2];
                        BigDecimal price = new BigDecimal(data[3]);
                        String status = data[4];
                        String ownerUsername = data[5];

                        User owner = "admin".equals(ownerUsername) ? admin
                                        : "user1".equals(ownerUsername) ? user1 : user2;

                        // Tạo document
                        Document document = createDocument(title, slug, description, price, status);

                        // Tạo file giả
                        File file = createSampleFile(title + ".pdf", (i + 1) * 1024 * 1024L, owner); // 1MB, 2MB, 3MB...
                        document.setFile(file);

                        // Cập nhật views và downloads ngẫu nhiên
                        document.setViewsCount((long) (Math.random() * 1000));
                        document.setDownloadsCount((long) (Math.random() * 100));
                        document = documentRepository.save(document);

                        // Tạo DocumentOwner
                        createDocumentOwner(document, owner);

                        // Gán categories và tags dựa trên nội dung
                        assignCategoriesAndTags(document, i);
                }
        }

        private Document createDocument(String title, String slug, String description,
                        BigDecimal price, String status) {
                Document document = new Document();
                document.setTitle(title);
                document.setSlug(slug);
                document.setDescription(description);
                document.setPrice(price);
                document.setStatus(status);
                document.setVisibility("public");
                document.setViewsCount(0L);
                document.setDownloadsCount(0L);
                document.setCreatedAt(LocalDateTime.now());
                document.setUpdatedAt(LocalDateTime.now());
                if ("APPROVED".equals(status)) {
                        document.setPublishedAt(LocalDateTime.now());
                }
                return document;
        }

        private File createSampleFile(String fileName, Long fileSize, User uploadedBy) {
                File file = new File();
                file.setFileName(fileName);
                file.setFileSize(fileSize);
                file.setFileType(getFileTypeFromName(fileName));
                file.setMimeType("application/pdf");
                file.setFilePath("uploads/documents/" + fileName);
                file.setUploadedBy(uploadedBy);
                file.setStatus("active");
                file.setCreatedAt(LocalDateTime.now());
                return fileRepository.save(file);
        }

        private String getFileTypeFromName(String fileName) {
                if (fileName.endsWith(".pdf"))
                        return "PDF";
                if (fileName.endsWith(".doc") || fileName.endsWith(".docx"))
                        return "DOC";
                if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx"))
                        return "PPT";
                return "OTHER";
        }

        private void createDocumentOwner(Document document, User user) {
                DocumentOwner owner = new DocumentOwner();
                DocumentOwnerId ownerId = new DocumentOwnerId();
                ownerId.setDocumentId(document.getId());
                ownerId.setUserId(user.getId());
                owner.setId(ownerId);
                owner.setDocument(document);
                owner.setUser(user);
                documentOwnerRepository.save(owner);
        }

        private void assignCategoriesAndTags(Document document, int index) {
                // Gán categories dựa trên index và tên document
                Category category = null;
                String title = document.getTitle().toLowerCase();

                if (title.contains("java")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Java").orElse(null);
                } else if (title.contains("python")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Python").orElse(null);
                } else if (title.contains("javascript") || title.contains("react") || title.contains("vue")
                                || title.contains("css")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("JavaScript").orElse(null);
                } else if (title.contains("web") || title.contains("node")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Phát triển Web").orElse(null);
                } else if (title.contains("database") || title.contains("mongodb")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Cơ sở dữ liệu").orElse(null);
                } else if (title.contains("machine learning") || title.contains("ai")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Trí tuệ nhân tạo").orElse(null);
                } else if (title.contains("mobile")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Ứng dụng di động").orElse(null);
                } else {
                        // Default to programming
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Lập trình").orElse(null);
                }

                if (category != null) {
                        DocumentCategory docCategory = new DocumentCategory();
                        DocumentCategoryId docCatId = new DocumentCategoryId();
                        docCatId.setDocumentId(document.getId());
                        docCatId.setCategoryId(category.getId());
                        docCategory.setId(docCatId);
                        docCategory.setDocument(document);
                        docCategory.setCategory(category);
                        documentCategoryRepository.save(docCategory);
                }

                // Gán tags dựa trên nội dung
                String[] possibleTags = determineTags(document.getTitle(), document.getDescription(),
                                document.getPrice());

                for (String tagName : possibleTags) {
                        Tag tag = tagRepository.findByName(tagName).orElse(null);
                        if (tag != null) {
                                DocumentTag docTag = new DocumentTag();
                                DocumentTagId docTagId = new DocumentTagId();
                                docTagId.setDocumentId(document.getId());
                                docTagId.setTagId(tag.getId());
                                docTag.setId(docTagId);
                                docTag.setDocument(document);
                                docTag.setTag(tag);
                                documentTagRepository.save(docTag);
                        }
                }
        }

        private String[] determineTags(String title, String description, BigDecimal price) {
                String content = (title + " " + description).toLowerCase();
                java.util.List<String> tags = new java.util.ArrayList<>();

                // Level tags
                if (content.contains("beginner") || content.contains("basic") || content.contains("cơ bản")) {
                        tags.add("beginner");
                } else if (content.contains("advanced") || content.contains("nâng cao")) {
                        tags.add("advanced");
                } else {
                        tags.add("intermediate");
                }

                // Content type tags
                if (content.contains("tutorial") || content.contains("hướng dẫn")) {
                        tags.add("tutorial");
                }
                if (content.contains("guide") || content.contains("roadmap")) {
                        tags.add("guide");
                }

                // Technology tags
                if (content.contains("java"))
                        tags.add("java");
                if (content.contains("python"))
                        tags.add("python");
                if (content.contains("javascript"))
                        tags.add("javascript");
                if (content.contains("react"))
                        tags.add("react");
                if (content.contains("vue"))
                        tags.add("vue");
                if (content.contains("node"))
                        tags.add("nodejs");
                if (content.contains("spring"))
                        tags.add("spring-boot");
                if (content.contains("docker"))
                        tags.add("docker");
                if (content.contains("aws"))
                        tags.add("aws");
                if (content.contains("database") || content.contains("mongodb"))
                        tags.add("database");
                if (content.contains("machine learning") || content.contains("ai"))
                        tags.add("ai");
                if (content.contains("web"))
                        tags.add("web");
                if (content.contains("mobile"))
                        tags.add("mobile");
                if (content.contains("security"))
                        tags.add("security");
                if (content.contains("algorithm"))
                        tags.add("algorithm");

                // Price tag
                if (price.compareTo(BigDecimal.ZERO) == 0) {
                        tags.add("free");
                }

                // Always add best-practices for some documents
                if (content.contains("design") || content.contains("architecture") || content.contains("principles")) {
                        tags.add("best-practices");
                }

                return tags.toArray(new String[0]);
        }

        private void createSampleComments() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                // Lấy một số documents để comment
                java.util.List<Document> documents = documentRepository.findAll();
                if (documents.isEmpty())
                        return;

                // Tạo comments cho các documents
                String[][] commentData = {
                                { "Tài liệu rất hữu ích, cảm ơn tác giả!", "active", "user1", "5" },
                                { "Nội dung chi tiết và dễ hiểu, 5 sao!", "active", "user2", "5" },
                                { "Có thể bổ sung thêm ví dụ thực tế không?", "active", "user1", "4" },
                                { "Đây là tài liệu tốt nhất về chủ đề này mà tôi từng đọc", "active", "user2", "5" },
                                { "Cảm ơn bạn đã chia sẻ kiến thức quý báu", "active", "admin", "4" },
                                { "Rất bổ ích cho người mới bắt đầu như tôi", "active", "user1", "3" },
                                { "Có thể cập nhật thêm phần về best practices không?", "active", "user2", "4" },
                                { "Tài liệu viết rất chuyên nghiệp và dễ theo dõi", "active", "admin", "5" },
                                { "Mình đã áp dụng thành công theo hướng dẫn này", "active", "user1", "5" },
                                { "Chất lượng tuyệt vời, đáng giá từng xu!", "active", "user2", "5" },
                                { "Có thể làm thêm video hướng dẫn không?", "active", "user1", "3" },
                                { "Phần code example rất hay và thực tế", "active", "admin", "5" },
                                { "Tài liệu này giúp tôi hiểu rõ hơn về chủ đề", "active", "user2", "4" },
                                { "Cảm ơn tác giả, mong có thêm nhiều tài liệu hay", "active", "user1", "5" },
                                { "Nội dung cập nhật và theo kịp xu hướng", "active", "admin", "4" },
                                { "Giải thích rất rõ ràng, dễ hiểu", "active", "user2", "4" },
                                { "Tôi sẽ recommend tài liệu này cho bạn bè", "active", "user1", "5" },
                                { "Phần troubleshooting rất hữu ích", "active", "admin", "5" },
                                { "Đây là investment tốt cho việc học", "active", "user2", "4" },
                                { "Cảm ơn vì đã chia sẻ kinh nghiệm thực tế", "active", "user1", "3" },
                                { "Tài liệu được tổ chức rất logic và khoa học", "active", "admin", "5" },
                                { "Mình đã bookmark để tham khảo lại", "active", "user2", "4" },
                                { "Có thể thêm phần FAQ không?", "active", "user1", "3" },
                                { "Chất lượng nội dung rất cao", "active", "admin", "5" },
                                { "Đọc xong cảm thấy tự tin hơn nhiều", "active", "user2", "4" },
                                { "Tác giả có kinh nghiệm thực tế rất tốt", "active", "user1", "5" },
                                { "Tài liệu này đáng để đầu tư", "active", "admin", "5" },
                                { "Nội dung được cập nhật thường xuyên", "active", "user2", "4" },
                                { "Rất chi tiết và đầy đủ thông tin", "active", "user1", "5" },
                                { "Cảm ơn vì tài liệu miễn phí chất lượng cao", "active", "admin", "5" }
                };

                // Tạo comments ngẫu nhiên cho các documents
                for (int i = 0; i < commentData.length && i < documents.size() * 3; i++) {
                        Document document = documents.get(i % documents.size());
                        String[] data = commentData[i % commentData.length];
                        String content = data[0];
                        String status = data[1];
                        String username = data[2];
                        Integer rating = Integer.parseInt(data[3]);

                        User commenter = "admin".equals(username) ? admin : "user1".equals(username) ? user1 : user2;

                        if (commenter != null) {
                                createComment(document, commenter, content, status, rating);
                        }
                }

        }

        private void createComment(Document document, User user, String content, String status, Integer rating) {
                Comment comment = new Comment();
                comment.setDocument(document);
                comment.setUser(user);
                comment.setContent(content);
                comment.setStatus(status);
                comment.setCreatedAt(LocalDateTime.now().minusDays((long) (Math.random() * 30))); // Random date within
                                                                                                  // last 30 days
                comment.setUpdatedAt(comment.getCreatedAt());
                comment.setRating(rating);
                commentRepository.save(comment);
        }

        private Reply createReply(Comment comment, User user, String content, LocalDateTime now) {
                Reply reply = new Reply();
                reply.setComment(comment);
                reply.setUser(user);
                reply.setContent(content);
                reply.setStatus("active");
                reply.setCreatedAt(now);
                reply.setUpdatedAt(now);
                reply.setUpdatedAt(now);
                return reply;
        }

        private void createSampleReplies() {
                try {
                        User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                        User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                        List<Comment> comments = commentRepository.findAll();

                        if (user1 == null || user2 == null || comments.isEmpty()) {
                                System.err.println("❌ Thiếu user hoặc comment để tạo reply.");
                                System.err.println("user1: " + (user1 == null ? "null" : "OK"));
                                System.err.println("user2: " + (user2 == null ? "null" : "OK"));
                                System.err.println("comments count: " + comments.size());
                                return;
                        }

                        LocalDateTime now = LocalDateTime.now();

                        Reply[] replies = {
                                        createReply(comments.get(0), user2, "Cảm ơn bạn, mình cũng thấy vậy!", now),
                                        createReply(comments.get(1), user1, "Mình nghĩ là được, nhớ ghi nguồn nhé.",
                                                        now),
                                        createReply(comments.get(2), user2, "Ý kiến hay, mình cũng muốn thêm ví dụ.",
                                                        now),
                                        createReply(comments.get(3), user1, "Chính xác, rất dễ hiểu luôn.", now),
                                        createReply(comments.get(4), user2, "Hình như có bản cập nhật hôm trước đấy.",
                                                        now)
                        };

                        for (Reply reply : replies) {
                                replyRepository.save(reply);
                                System.out.println("✅ Đã lưu reply: " + reply.getContent());
                        }
                } catch (Exception e) {
                        System.err.println("❌ Lỗi khi tạo replies: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        private void createSampleReports() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                if (admin == null || user1 == null || user2 == null)
                        return;

                List<Document> documents = documentRepository.findAll();

                if (documents.isEmpty())
                        return;

                String[][] reportData = {
                                { "Nội dung không phù hợp", "inappropriate", "user1" },
                                { "Spam hoặc quảng cáo", "spam", "user2" },
                                { "Thông tin sai lệch", "fake", "user2" },
                                { "Ngôn từ xúc phạm", "other", "admin" },
                                { "Sao chép từ nguồn khác", "copyright", "user1" },
                                { "Bình luận gây hiểu lầm", "other", "user2" }
                };

                for (int i = 0; i < reportData.length; i++) {
                        String reason = reportData[i][0];
                        String type = reportData[i][1];
                        String username = reportData[i][2];

                        User reporter = "admin".equals(username) ? admin : "user1".equals(username) ? user1 : user2;

                        Report report = new Report();
                        report.setReporter(reporter);
                        report.setType(type);
                        report.setReason(reason);
                        report.setStatus("pending");
                        report.setCreatedAt(LocalDateTime.now());

                        Document doc = documents.get(i % documents.size());
                        report.setDocument(doc);

                        reportRepository.save(report);
                }
        }

}