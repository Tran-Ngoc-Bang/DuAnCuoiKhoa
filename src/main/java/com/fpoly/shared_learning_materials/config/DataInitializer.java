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
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.Map;

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
                // Log thông tin về dữ liệu hiện tại
                logCurrentDataStatus();

                // Xóa categories duplicate trước khi tạo dữ liệu mới
                cleanupDuplicateCategories();

                // Initialize sample data
                if (!userRepository.existsByUsernameAndDeletedAtIsNull("admin") ||
                                !userRepository.existsByUsernameAndDeletedAtIsNull("user1") ||
                                !userRepository.existsByUsernameAndDeletedAtIsNull("contributor1")) {
                        createSampleUsers();
                        System.out.println("Sample users created successfully!");
                } else {
                        System.out.println("Sample users already exist, skipping user initialization.");
                }

                if (!coinPackageRepository.existsByCodeAndDeletedAtIsNull("CP001")) {
                        createSampleCoinPackages();
                        // System.out.println("Sample coin packages created successfully!");
                } else {
                        // System.out.println("Sample coin packages already exist, skipping package
                        // initialization.");
                }

                if (!transactionRepository.existsByCodeAndDeletedAtIsNull("TXN000001")) {
                        createSampleTransactions();
                        // System.out.println("Sample transactions and withdrawals created
                        // successfully!");
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
                                // System.out.println("Sample withdrawals created successfully!");
                        }
                } else {
                        // System.out.println("Sample withdrawals already exist, skipping withdrawal
                        // initialization.");
                }

                // Kiểm tra xem có categories nào chưa
                if (categoryRepository.count() == 0) {
                        createSampleCategories();
                        System.out.println("Sample categories created successfully!");
                } else {
                        System.out.println("Categories already exist, skipping category initialization.");
                }

                if (!tagRepository.existsByName("beginner")) {
                        createSampleTags();
                        // System.out.println("Sample tags created successfully!");
                } else {
                        // System.out.println("Sample tags already exist, skipping tag
                        // initialization.");
                }

                if (!documentRepository.existsBySlug("java-programming-guide")) {
                        createSampleDocuments();
                        // System.out.println("Sample documents created successfully!");
                } else {
                        // System.out.println("Sample documents already exist, skipping document
                        // initialization.");
                }

                if (commentRepository.count() == 0) {
                        createSampleComments();
                        // System.out.println("Sample comments created successfully!");
                } else {
                        // System.out.println("Sample comments already exist, skipping comment
                        // initialization.");
                }

                if (replyRepository.count() == 0) {
                        createSampleReplies();
                        // System.out.println("Sample replies created successfully!");
                } else {
                        // System.out.println("Sample replies already exist, skipping reply
                        // initialization.");
                }

                if (reportRepository.count() == 0) {
                        createSampleReports();
                        // System.out.println("Sample reports created successfully!");
                } else {
                        // System.out.println("Sample reports already exist, skipping comment
                        // initialization.");
                }

                // Tạo dữ liệu mẫu cho tags và document-tag relationships để test popular tags
                // Chỉ tạo nếu chưa có document-tag relationships
                if (documentTagRepository.count() == 0) {
                        createSampleTagsAndRelationships();
                } else {
                        System.out.println("Document-tag relationships already exist, skipping creation");
                }
        }

        /**
         * Xóa categories duplicate để tránh lỗi
         */
        private void cleanupDuplicateCategories() {
                System.out.println("=== CLEANING UP DUPLICATE CATEGORIES ===");

                List<Category> allCategories = categoryRepository.findAll();
                Map<String, List<Category>> nameGroups = allCategories.stream()
                                .collect(Collectors.groupingBy(Category::getName));

                int deletedCount = 0;
                for (Map.Entry<String, List<Category>> entry : nameGroups.entrySet()) {
                        String name = entry.getKey();
                        List<Category> categories = entry.getValue();

                        if (categories.size() > 1) {
                                System.out.println(
                                                "Found " + categories.size() + " categories with name: '" + name + "'");

                                // Giữ lại category đầu tiên, xóa các category còn lại
                                Category keepCategory = categories.get(0);
                                List<Category> toDelete = categories.subList(1, categories.size());

                                for (Category category : toDelete) {
                                        System.out.println("  - Deleting category ID: " + category.getId()
                                                        + " (created: " + category.getCreatedAt() + ")");
                                        categoryRepository.delete(category);
                                        deletedCount++;
                                }
                        }
                }

                System.out.println("Deleted " + deletedCount + " duplicate categories");
                System.out.println("=== END CLEANUP ===");
        }

        /**
         * Log thông tin về dữ liệu hiện tại trong database
         */
        private void logCurrentDataStatus() {
                System.out.println("=== CURRENT DATA STATUS ===");
                System.out.println("Users: " + userRepository.count());
                System.out.println("Categories: " + categoryRepository.count());
                System.out.println("Category Hierarchies: " + categoryHierarchyRepository.count());
                System.out.println("Tags: " + tagRepository.count());
                System.out.println("Documents: " + documentRepository.count());
                System.out.println("Document-Category relationships: " + documentCategoryRepository.count());
                System.out.println("Document-Tag relationships: " + documentTagRepository.count());
                System.out.println("Comments: " + commentRepository.count());
                System.out.println("Replies: " + replyRepository.count());
                System.out.println("Reports: " + reportRepository.count());
                System.out.println("Coin Packages: " + coinPackageRepository.count());
                System.out.println("Transactions: " + transactionRepository.count());
                System.out.println("=== END DATA STATUS ===");
        }

        private void createSampleUsers() {
                System.out.println("=== Creating sample users ===");
                int currentYear = LocalDate.now().getYear();

                // Tạo users một cách an toàn
                createUserIfNotExists("admin", "admin@example.com", "Administrator", "admin",
                                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                                LocalDateTime.of(currentYear, 1, 10, 10, 0));

                createUserIfNotExists("user1", "user1@example.com", "Người dùng 1", "user",
                                new BigDecimal("2365"), new BigDecimal("328000.00"), new BigDecimal("850"),
                                LocalDateTime.of(currentYear, 2, 15, 10, 0));

                createUserIfNotExists("user2", "user2@example.com", "Người dùng 2", "user",
                                new BigDecimal("930"), new BigDecimal("50000.00"), new BigDecimal("100"),
                                LocalDateTime.of(currentYear, 3, 20, 10, 0));

                createUserIfNotExists("user3", "user3@example.com", "Người dùng 3", "user",
                                new BigDecimal("755"), BigDecimal.ZERO, new BigDecimal("0"),
                                LocalDateTime.of(currentYear, 4, 5, 10, 0));

                createUserIfNotExists("user4", "user4@example.com", "Người dùng 4", "user",
                                new BigDecimal("100"), BigDecimal.ZERO, BigDecimal.ZERO,
                                LocalDateTime.of(currentYear, 5, 18, 10, 0));

                createUserIfNotExists("user5", "user5@example.com", "Người dùng 5", "user",
                                new BigDecimal("120"), BigDecimal.ZERO, new BigDecimal("0"),
                                LocalDateTime.of(currentYear, 6, 22, 10, 0));

                createUserIfNotExists("contributor1", "contributor1@example.com", "Người đóng góp 1", "user",
                                new BigDecimal("2347"), BigDecimal.ZERO, BigDecimal.ZERO,
                                LocalDateTime.of(currentYear, 1, 5, 9, 0));

                createUserIfNotExists("contributor2", "contributor2@example.com", "Người đóng góp 2", "user",
                                new BigDecimal("1780"), BigDecimal.ZERO, BigDecimal.ZERO,
                                LocalDateTime.of(currentYear, 1, 8, 14, 30));

                System.out.println("=== Sample users creation completed ===");
        }

        private void createUserIfNotExists(String username, String email, String fullName, String role,
                        BigDecimal coinBalance, BigDecimal totalSpent, BigDecimal totalCoinsPurchased,
                        LocalDateTime createdAt) {
                User existingUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
                if (existingUser != null) {
                        System.out.println("User '" + username + "' already exists, skipping creation");
                        return;
                }

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode("password"));
                user.setFullName(fullName);
                user.setRole(role);
                user.setStatus("active");
                user.setCoinBalance(coinBalance);
                user.setTotalSpent(totalSpent);
                user.setTotalCoinsPurchased(totalCoinsPurchased);
                user.setCreatedAt(createdAt);
                user.setUpdatedAt(createdAt);
                userRepository.save(user);
                System.out.println("Created user: " + username);
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
                                        new BigDecimal("170000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói premium",
                                        LocalDateTime.of(currentYear, 6, 15, 14, 45));

                        // Tháng 3
                        createTransaction("TXN000003", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("108000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói standard",
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

                // Seed trusted history for user3: 3 withdrawals COMPLETED within 90 days
                if (user3 != null) {
                        createWithdrawalTransaction("WD_U3_001", new BigDecimal("60.00"),
                                        Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", user3,
                                        "Trusted seed 1", now.minusDays(20));
                        createWithdrawalTransaction("WD_U3_002", new BigDecimal("70.00"),
                                        Transaction.TransactionStatus.COMPLETED, "E_WALLET", user3,
                                        "Trusted seed 2", now.minusDays(50));
                        createWithdrawalTransaction("WD_U3_003", new BigDecimal("80.00"),
                                        Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", user3,
                                        "Trusted seed 3", now.minusDays(75));
                }

                // Withdrawal 1: Pending - Chờ duyệt
                createWithdrawalTransaction("WD000001", new BigDecimal("25.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", user1,
                                "Rút tiền về tài khoản ngân hàng Vietcombank", now.minusDays(1));

                // Withdrawal 2: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000002", new BigDecimal("15.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor1,
                                "Rút tiền từ doanh thu bán tài liệu - Đã chuyển khoản thành công", now.minusDays(3));

                // Withdrawal 3: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000003", new BigDecimal("75.00"),
                                Transaction.TransactionStatus.COMPLETED, "E_WALLET", user2,
                                "Rút tiền về ví MoMo - Giao dịch thành công", now.minusDays(5));

                // Withdrawal 4: Pending - Chờ duyệt (số tiền lớn)
                createWithdrawalTransaction("WD000004", new BigDecimal("45.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", contributor2,
                                "Rút tiền doanh thu tháng - Cần xác minh thông tin ngân hàng", now.minusDays(2));

                // Withdrawal 5: Failed - Thất bại
                createWithdrawalTransaction("WD000005", new BigDecimal("80.00"),
                                Transaction.TransactionStatus.FAILED, "BANK_TRANSFER", user3,
                                "Rút tiền thất bại - Thông tin tài khoản không chính xác", now.minusDays(7));

                // Withdrawal 6: Cancelled - Đã hủy
                createWithdrawalTransaction("WD000006", new BigDecimal("30.00"),
                                Transaction.TransactionStatus.CANCELLED, "E_WALLET", user4,
                                "Hủy yêu cầu rút tiền theo yêu cầu của người dùng", now.minusDays(4));

                // Withdrawal 7: Completed - Đã hoàn thành (tháng trước)
                createWithdrawalTransaction("WD000007", new BigDecimal("120.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor1,
                                "Rút tiền tháng trước - Đã chuyển khoản", now.minusDays(35));

                // Withdrawal 8: Pending - Chờ duyệt (quá hạn xử lý)
                createWithdrawalTransaction("WD000008", new BigDecimal("45.00"),
                                Transaction.TransactionStatus.PENDING, "E_WALLET", user5,
                                "Rút tiền về ví ZaloPay - Đang chờ xử lý quá 5 ngày", now.minusDays(6));

                // Withdrawal 9: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000009", new BigDecimal("20.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", contributor2,
                                "Rút tiền doanh thu - Chuyển khoản ACB thành công", now.minusDays(8));

                // Withdrawal 10: Pending - Chờ duyệt (mới)
                createWithdrawalTransaction("WD000010", new BigDecimal("60.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", user1,
                                "Rút tiền về tài khoản Techcombank", now.minusDays(1));

                // Withdrawal 11: Failed - Thất bại (lỗi hệ thống)
                createWithdrawalTransaction("WD000011", new BigDecimal("90.00"),
                                Transaction.TransactionStatus.FAILED, "E_WALLET", user2,
                                "Rút tiền thất bại - Lỗi kết nối với nhà cung cấp dịch vụ", now.minusDays(10));

                // Withdrawal 12: Completed - Đã hoàn thành (tháng này)
                createWithdrawalTransaction("WD000012", new BigDecimal("35.00"),
                                Transaction.TransactionStatus.COMPLETED, "E_WALLET", user3,
                                "Rút tiền về ví Momo - Giao dịch hoàn tất", now.minusDays(12));

                // Withdrawal 13: Pending - Chờ duyệt (cần xác minh)
                createWithdrawalTransaction("WD000013", new BigDecimal("48.00"),
                                Transaction.TransactionStatus.PENDING, "BANK_TRANSFER", contributor1,
                                "Rút tiền số lượng lớn - Cần xác minh danh tính", now.minusDays(1));

                // Withdrawal 14: Completed - Đã hoàn thành (tháng trước)
                createWithdrawalTransaction("WD000014", new BigDecimal("85.00"),
                                Transaction.TransactionStatus.COMPLETED, "BANK_TRANSFER", user4,
                                "Rút tiền tháng trước - Đã chuyển khoản BIDV", now.minusDays(40));

                // Withdrawal 15: Cancelled - Đã hủy (do user)
                createWithdrawalTransaction("WD000015", new BigDecimal("55.00"),
                                Transaction.TransactionStatus.CANCELLED, "E_WALLET", user5,
                                "Hủy yêu cầu rút tiền - Người dùng thay đổi ý định", now.minusDays(3));

                // Withdrawal 16: Processing - Đang xử lý
                createWithdrawalTransaction("WD000016", new BigDecimal("18.00"),
                                Transaction.TransactionStatus.PROCESSING, "BANK_TRANSFER", contributor1,
                                "Đang xử lý chuyển khoản - Đã xác minh thông tin", now.minusHours(6));

                // Withdrawal 17: Processing - Đang xử lý (ví điện tử)
                createWithdrawalTransaction("WD000017", new BigDecimal("95.00"),
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

                // Tạo danh mục gốc - Mở rộng từ 5 lên 12 danh mục chính
                Category tech = createCategory("Công nghệ thông tin",
                                "Các tài liệu về công nghệ thông tin, lập trình, phần mềm", admin);
                Category education = createCategory("Giáo dục", "Tài liệu giáo dục và học tập các cấp", admin);
                Category business = createCategory("Kinh tế - Quản trị", "Tài liệu về kinh doanh, quản lý và tài chính",
                                admin);
                Category science = createCategory("Khoa học", "Tài liệu khoa học tự nhiên và nghiên cứu", admin);
                Category arts = createCategory("Nghệ thuật", "Tài liệu về nghệ thuật và sáng tạo", admin);
                Category language = createCategory("Ngoại ngữ", "Tài liệu học ngoại ngữ và ngôn ngữ học", admin);
                Category engineering = createCategory("Kỹ thuật", "Tài liệu về các ngành kỹ thuật", admin);
                Category social = createCategory("Khoa học xã hội", "Tài liệu về tâm lý, xã hội, triết học", admin);
                Category health = createCategory("Y tế - Sức khỏe", "Tài liệu về y học và chăm sóc sức khỏe", admin);
                Category law = createCategory("Luật pháp", "Tài liệu về luật pháp và pháp lý", admin);
                Category agriculture = createCategory("Nông nghiệp", "Tài liệu về nông nghiệp và thủy sản", admin);
                Category environment = createCategory("Môi trường",
                                "Tài liệu về bảo vệ môi trường và phát triển bền vững", admin);

                // === CÔNG NGHỆ THÔNG TIN ===
                Category programming = createCategory("Lập trình", "Tài liệu về lập trình và phát triển phần mềm",
                                admin);
                Category webDev = createCategory("Phát triển Web", "Tài liệu về phát triển website và ứng dụng web",
                                admin);
                Category mobile = createCategory("Ứng dụng di động", "Tài liệu về phát triển ứng dụng mobile", admin);
                Category database = createCategory("Cơ sở dữ liệu", "Tài liệu về database và quản lý dữ liệu", admin);
                Category ai = createCategory("Trí tuệ nhân tạo", "Tài liệu về AI, Machine Learning và Deep Learning",
                                admin);
                Category cybersecurity = createCategory("An ninh mạng", "Tài liệu về bảo mật và an ninh thông tin",
                                admin);
                Category cloud = createCategory("Điện toán đám mây", "Tài liệu về cloud computing và DevOps", admin);

                createHierarchy(tech, programming, 1);
                createHierarchy(tech, webDev, 1);
                createHierarchy(tech, mobile, 1);
                createHierarchy(tech, database, 1);
                createHierarchy(tech, ai, 1);
                createHierarchy(tech, cybersecurity, 1);
                createHierarchy(tech, cloud, 1);

                // Danh mục con của Lập trình
                Category java = createCategory("Java", "Tài liệu về ngôn ngữ lập trình Java", admin);
                Category python = createCategory("Python", "Tài liệu về ngôn ngữ lập trình Python", admin);
                Category javascript = createCategory("JavaScript", "Tài liệu về JavaScript và TypeScript", admin);
                Category csharp = createCategory("C#", "Tài liệu về ngôn ngữ C# và .NET", admin);
                Category cpp = createCategory("C++", "Tài liệu về ngôn ngữ C++", admin);
                Category php = createCategory("PHP", "Tài liệu về ngôn ngữ PHP", admin);

                createHierarchy(programming, java, 2);
                createHierarchy(programming, python, 2);
                createHierarchy(programming, javascript, 2);
                createHierarchy(programming, csharp, 2);
                createHierarchy(programming, cpp, 2);
                createHierarchy(programming, php, 2);

                // Danh mục con của Phát triển Web
                Category frontend = createCategory("Frontend", "Tài liệu về phát triển giao diện người dùng", admin);
                Category backend = createCategory("Backend", "Tài liệu về phát triển phía máy chủ", admin);
                Category fullstack = createCategory("Fullstack", "Tài liệu về phát triển toàn bộ ứng dụng", admin);

                createHierarchy(webDev, frontend, 2);
                createHierarchy(webDev, backend, 2);
                createHierarchy(webDev, fullstack, 2);

                // === GIÁO DỤC ===
                Category math = createCategory("Toán học", "Tài liệu toán học từ cơ bản đến nâng cao", admin);
                Category physics = createCategory("Vật lý", "Tài liệu vật lý và cơ học", admin);
                Category chemistry = createCategory("Hóa học", "Tài liệu hóa học và hóa sinh", admin);
                Category biology = createCategory("Sinh học", "Tài liệu sinh học và y sinh", admin);
                Category literature = createCategory("Văn học", "Tài liệu văn học và ngôn ngữ", admin);
                Category history = createCategory("Lịch sử", "Tài liệu lịch sử và văn hóa", admin);
                Category geography = createCategory("Địa lý", "Tài liệu địa lý và môi trường", admin);

                createHierarchy(education, math, 1);
                createHierarchy(education, physics, 1);
                createHierarchy(education, chemistry, 1);
                createHierarchy(education, biology, 1);
                createHierarchy(education, literature, 1);
                createHierarchy(education, history, 1);
                createHierarchy(education, geography, 1);

                // === KINH TẾ - QUẢN TRỊ ===
                Category marketing = createCategory("Marketing", "Tài liệu marketing và quảng cáo", admin);
                Category finance = createCategory("Tài chính", "Tài liệu tài chính và đầu tư", admin);
                Category management = createCategory("Quản lý", "Tài liệu quản lý doanh nghiệp", admin);
                Category accounting = createCategory("Kế toán", "Tài liệu kế toán và kiểm toán", admin);
                Category hr = createCategory("Nhân sự", "Tài liệu quản trị nhân sự", admin);
                Category startup = createCategory("Khởi nghiệp", "Tài liệu về startup và kinh doanh mới", admin);

                createHierarchy(business, marketing, 1);
                createHierarchy(business, finance, 1);
                createHierarchy(business, management, 1);
                createHierarchy(business, accounting, 1);
                createHierarchy(business, hr, 1);
                createHierarchy(business, startup, 1);

                // === NGOẠI NGỮ ===
                Category english = createCategory("Tiếng Anh", "Tài liệu học tiếng Anh", admin);
                Category japanese = createCategory("Tiếng Nhật", "Tài liệu học tiếng Nhật", admin);
                Category korean = createCategory("Tiếng Hàn", "Tài liệu học tiếng Hàn", admin);
                Category chinese = createCategory("Tiếng Trung", "Tài liệu học tiếng Trung", admin);
                Category french = createCategory("Tiếng Pháp", "Tài liệu học tiếng Pháp", admin);
                Category german = createCategory("Tiếng Đức", "Tài liệu học tiếng Đức", admin);

                createHierarchy(language, english, 1);
                createHierarchy(language, japanese, 1);
                createHierarchy(language, korean, 1);
                createHierarchy(language, chinese, 1);
                createHierarchy(language, french, 1);
                createHierarchy(language, german, 1);

                // === KỸ THUẬT ===
                Category mechanical = createCategory("Cơ khí", "Tài liệu về cơ khí và chế tạo", admin);
                Category electrical = createCategory("Điện - Điện tử", "Tài liệu về điện và điện tử", admin);
                Category civil = createCategory("Xây dựng", "Tài liệu về xây dựng và kiến trúc", admin);
                Category chemical = createCategory("Hóa học kỹ thuật", "Tài liệu về hóa học công nghiệp", admin);
                Category automotive = createCategory("Ô tô", "Tài liệu về công nghệ ô tô", admin);

                createHierarchy(engineering, mechanical, 1);
                createHierarchy(engineering, electrical, 1);
                createHierarchy(engineering, civil, 1);
                createHierarchy(engineering, chemical, 1);
                createHierarchy(engineering, automotive, 1);

                // === KHOA HỌC XÃ HỘI ===
                Category psychology = createCategory("Tâm lý học", "Tài liệu về tâm lý học", admin);
                Category sociology = createCategory("Xã hội học", "Tài liệu về xã hội học", admin);
                Category philosophy = createCategory("Triết học", "Tài liệu về triết học", admin);
                Category politics = createCategory("Chính trị", "Tài liệu về chính trị học", admin);
                Category economics = createCategory("Kinh tế học", "Tài liệu về kinh tế học", admin);

                createHierarchy(social, psychology, 1);
                createHierarchy(social, sociology, 1);
                createHierarchy(social, philosophy, 1);
                createHierarchy(social, politics, 1);
                createHierarchy(social, economics, 1);

                // === Y TẾ - SỨC KHỎE ===
                Category medicine = createCategory("Y học", "Tài liệu về y học lâm sàng", admin);
                Category pharmacy = createCategory("Dược học", "Tài liệu về dược phẩm", admin);
                Category nursing = createCategory("Điều dưỡng", "Tài liệu về điều dưỡng", admin);
                Category nutrition = createCategory("Dinh dưỡng", "Tài liệu về dinh dưỡng học", admin);
                Category publicHealth = createCategory("Y tế công cộng", "Tài liệu về y tế cộng đồng", admin);

                createHierarchy(health, medicine, 1);
                createHierarchy(health, pharmacy, 1);
                createHierarchy(health, nursing, 1);
                createHierarchy(health, nutrition, 1);
                createHierarchy(health, publicHealth, 1);

                // === LUẬT PHÁP ===
                Category civilLaw = createCategory("Luật dân sự", "Tài liệu về luật dân sự", admin);
                Category criminalLaw = createCategory("Luật hình sự", "Tài liệu về luật hình sự", admin);
                Category commercialLaw = createCategory("Luật thương mại", "Tài liệu về luật thương mại", admin);
                Category internationalLaw = createCategory("Luật quốc tế", "Tài liệu về luật quốc tế", admin);
                Category constitutionalLaw = createCategory("Luật hiến pháp", "Tài liệu về luật hiến pháp", admin);

                createHierarchy(law, civilLaw, 1);
                createHierarchy(law, criminalLaw, 1);
                createHierarchy(law, commercialLaw, 1);
                createHierarchy(law, internationalLaw, 1);
                createHierarchy(law, constitutionalLaw, 1);

                // === NÔNG NGHIỆP ===
                Category cropScience = createCategory("Trồng trọt", "Tài liệu về trồng trọt", admin);
                Category animalScience = createCategory("Chăn nuôi", "Tài liệu về chăn nuôi", admin);
                Category aquaculture = createCategory("Thủy sản", "Tài liệu về nuôi trồng thủy sản", admin);
                Category forestry = createCategory("Lâm nghiệp", "Tài liệu về lâm nghiệp", admin);
                Category biotechnology = createCategory("Công nghệ sinh học",
                                "Tài liệu về công nghệ sinh học nông nghiệp", admin);

                createHierarchy(agriculture, cropScience, 1);
                createHierarchy(agriculture, animalScience, 1);
                createHierarchy(agriculture, aquaculture, 1);
                createHierarchy(agriculture, forestry, 1);
                createHierarchy(agriculture, biotechnology, 1);

                // === MÔI TRƯỜNG ===
                Category environmentalScience = createCategory("Khoa học môi trường", "Tài liệu về khoa học môi trường",
                                admin);
                Category climateChange = createCategory("Biến đổi khí hậu", "Tài liệu về biến đổi khí hậu", admin);
                Category renewableEnergy = createCategory("Năng lượng tái tạo", "Tài liệu về năng lượng xanh", admin);
                Category wasteManagement = createCategory("Quản lý chất thải", "Tài liệu về xử lý chất thải", admin);
                Category biodiversity = createCategory("Đa dạng sinh học", "Tài liệu về bảo tồn đa dạng sinh học",
                                admin);

                createHierarchy(environment, environmentalScience, 1);
                createHierarchy(environment, climateChange, 1);
                createHierarchy(environment, renewableEnergy, 1);
                createHierarchy(environment, wasteManagement, 1);
                createHierarchy(environment, biodiversity, 1);
        }

        private Category createCategory(String name, String description, User createdBy) {
                // Kiểm tra xem category đã tồn tại chưa
                try {
                        Category existingCategory = categoryRepository.findByNameAndDeletedAtIsNull(name).orElse(null);
                        if (existingCategory != null) {
                                System.out.println("Category '" + name + "' already exists, skipping creation");
                                return existingCategory;
                        }
                } catch (Exception e) {
                        // Nếu có nhiều categories cùng tên, lấy category đầu tiên chưa bị xóa
                        System.out.println(
                                        "Multiple categories found with name '" + name + "', using first active one");
                        List<Category> categories = categoryRepository.findAll().stream()
                                        .filter(cat -> name.equals(cat.getName()) && cat.getDeletedAt() == null)
                                        .collect(Collectors.toList());
                        if (!categories.isEmpty()) {
                                return categories.get(0);
                        }
                }

                Category category = new Category();
                category.setName(name);
                category.setSlug(generateUniqueSlug(name));
                category.setDescription(description);
                category.setStatus("active");
                category.setSortOrder(0);
                category.setCreatedBy(createdBy);
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                return categoryRepository.save(category);
        }

        private String generateUniqueSlug(String name) {
                String baseSlug = SlugUtils.generateSlug(name);

                // Handle special cases for programming languages
                if ("C#".equals(name)) {
                        baseSlug = "c-sharp";
                } else if ("C++".equals(name)) {
                        baseSlug = "cpp";
                } else if ("C".equals(name)) {
                        baseSlug = "c-language";
                }

                // Ensure uniqueness
                String uniqueSlug = baseSlug;
                int counter = 1;
                while (categoryRepository.existsBySlug(uniqueSlug)) {
                        uniqueSlug = baseSlug + "-" + counter++;
                        if (counter > 100) {
                                uniqueSlug = baseSlug + "-" + System.currentTimeMillis();
                                break;
                        }
                }

                return uniqueSlug;
        }

        private void createHierarchy(Category parent, Category child, int level) {
                // Kiểm tra xem hierarchy đã tồn tại chưa
                CategoryHierarchyId id = new CategoryHierarchyId();
                id.setParentId(parent.getId());
                id.setChildId(child.getId());

                if (categoryHierarchyRepository.existsById(id)) {
                        System.out.println("Hierarchy between '" + parent.getName() + "' and '" + child.getName()
                                        + "' already exists, skipping creation");
                        return;
                }

                CategoryHierarchy hierarchy = new CategoryHierarchy();
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
                                "free:Tài liệu miễn phí",
                                // Education tags
                                "mathematics:Toán học",
                                "physics:Vật lý",
                                "chemistry:Hóa học",
                                "biology:Sinh học",
                                "literature:Văn học",
                                "history:Lịch sử",
                                "geography:Địa lý",
                                // Business tags
                                "marketing:Marketing",
                                "finance:Tài chính",
                                "management:Quản lý",
                                "accounting:Kế toán",
                                "startup:Khởi nghiệp",
                                // Language tags
                                "english:Tiếng Anh",
                                "japanese:Tiếng Nhật",
                                "korean:Tiếng Hàn",
                                "chinese:Tiếng Trung",
                                // Engineering tags
                                "mechanical:Cơ khí",
                                "electrical:Điện - Điện tử",
                                "civil-engineering:Xây dựng",
                                "automotive:Ô tô",
                                // Social Science tags
                                "psychology:Tâm lý học",
                                "sociology:Xã hội học",
                                "philosophy:Triết học",
                                // Health tags
                                "medicine:Y học",
                                "pharmacy:Dược học",
                                "nursing:Điều dưỡng",
                                // Law tags
                                "law:Luật pháp",
                                // Agriculture tags
                                "agriculture:Nông nghiệp",
                                "animal-science:Chăn nuôi",
                                "aquaculture:Thủy sản",
                                // Environment tags
                                "environmental-science:Khoa học môi trường",
                                "climate-change:Biến đổi khí hậu",
                                "renewable-energy:Năng lượng tái tạo"
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

                // Công nghệ thông tin
                if (title.contains("java")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Java").orElse(null);
                } else if (title.contains("python")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Python").orElse(null);
                } else if (title.contains("javascript") || title.contains("typescript")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("JavaScript").orElse(null);
                } else if (title.contains("c#") || title.contains("dotnet")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("C#").orElse(null);
                } else if (title.contains("c++")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("C++").orElse(null);
                } else if (title.contains("php")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("PHP").orElse(null);
                } else if (title.contains("react") || title.contains("vue") || title.contains("angular")
                                || title.contains("frontend")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Frontend").orElse(null);
                } else if (title.contains("spring") || title.contains("express") || title.contains("backend")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Backend").orElse(null);
                } else if (title.contains("fullstack")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Fullstack").orElse(null);
                } else if (title.contains("web") || title.contains("node")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Phát triển Web").orElse(null);
                } else if (title.contains("database") || title.contains("mongodb") || title.contains("mysql")
                                || title.contains("postgresql")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Cơ sở dữ liệu").orElse(null);
                } else if (title.contains("machine learning") || title.contains("ai")
                                || title.contains("deep learning")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Trí tuệ nhân tạo").orElse(null);
                } else if (title.contains("mobile") || title.contains("android") || title.contains("ios")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Ứng dụng di động").orElse(null);
                } else if (title.contains("security") || title.contains("cybersecurity")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("An ninh mạng").orElse(null);
                } else if (title.contains("cloud") || title.contains("aws") || title.contains("azure")
                                || title.contains("devops")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Điện toán đám mây").orElse(null);
                }
                // Giáo dục
                else if (title.contains("toán") || title.contains("math")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Toán học").orElse(null);
                } else if (title.contains("vật lý") || title.contains("physics")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Vật lý").orElse(null);
                } else if (title.contains("hóa học") || title.contains("chemistry")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Hóa học").orElse(null);
                } else if (title.contains("sinh học") || title.contains("biology")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Sinh học").orElse(null);
                } else if (title.contains("văn học") || title.contains("literature")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Văn học").orElse(null);
                } else if (title.contains("lịch sử") || title.contains("history")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Lịch sử").orElse(null);
                } else if (title.contains("địa lý") || title.contains("geography")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Địa lý").orElse(null);
                }
                // Kinh tế - Quản trị
                else if (title.contains("marketing")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Marketing").orElse(null);
                } else if (title.contains("tài chính") || title.contains("finance")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tài chính").orElse(null);
                } else if (title.contains("quản lý") || title.contains("management")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Quản lý").orElse(null);
                } else if (title.contains("kế toán") || title.contains("accounting")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Kế toán").orElse(null);
                } else if (title.contains("nhân sự") || title.contains("hr")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Nhân sự").orElse(null);
                } else if (title.contains("startup") || title.contains("khởi nghiệp")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Khởi nghiệp").orElse(null);
                }
                // Ngoại ngữ
                else if (title.contains("tiếng anh") || title.contains("english")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Anh").orElse(null);
                } else if (title.contains("tiếng nhật") || title.contains("japanese")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Nhật").orElse(null);
                } else if (title.contains("tiếng hàn") || title.contains("korean")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Hàn").orElse(null);
                } else if (title.contains("tiếng trung") || title.contains("chinese")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Trung").orElse(null);
                } else if (title.contains("tiếng pháp") || title.contains("french")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Pháp").orElse(null);
                } else if (title.contains("tiếng đức") || title.contains("german")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tiếng Đức").orElse(null);
                }
                // Kỹ thuật
                else if (title.contains("cơ khí") || title.contains("mechanical")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Cơ khí").orElse(null);
                } else if (title.contains("điện") || title.contains("electrical")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Điện - Điện tử").orElse(null);
                } else if (title.contains("xây dựng") || title.contains("civil")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Xây dựng").orElse(null);
                } else if (title.contains("ô tô") || title.contains("automotive")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Ô tô").orElse(null);
                }
                // Khoa học xã hội
                else if (title.contains("tâm lý") || title.contains("psychology")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Tâm lý học").orElse(null);
                } else if (title.contains("xã hội") || title.contains("sociology")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Xã hội học").orElse(null);
                } else if (title.contains("triết học") || title.contains("philosophy")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Triết học").orElse(null);
                }
                // Y tế - Sức khỏe
                else if (title.contains("y học") || title.contains("medicine")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Y học").orElse(null);
                } else if (title.contains("dược") || title.contains("pharmacy")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Dược học").orElse(null);
                } else if (title.contains("điều dưỡng") || title.contains("nursing")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Điều dưỡng").orElse(null);
                }
                // Luật pháp
                else if (title.contains("luật dân sự") || title.contains("civil law")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Luật dân sự").orElse(null);
                } else if (title.contains("luật hình sự") || title.contains("criminal law")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Luật hình sự").orElse(null);
                } else if (title.contains("luật thương mại") || title.contains("commercial law")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Luật thương mại").orElse(null);
                }
                // Nông nghiệp
                else if (title.contains("nông nghiệp") || title.contains("agriculture")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Trồng trọt").orElse(null);
                } else if (title.contains("chăn nuôi") || title.contains("animal")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Chăn nuôi").orElse(null);
                } else if (title.contains("thủy sản") || title.contains("aquaculture")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Thủy sản").orElse(null);
                }
                // Môi trường
                else if (title.contains("môi trường") || title.contains("environment")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Khoa học môi trường").orElse(null);
                } else if (title.contains("biến đổi khí hậu") || title.contains("climate")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Biến đổi khí hậu").orElse(null);
                } else if (title.contains("năng lượng tái tạo") || title.contains("renewable")) {
                        category = categoryRepository.findByNameAndDeletedAtIsNull("Năng lượng tái tạo").orElse(null);
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
                if (content.contains("angular"))
                        tags.add("angular");
                if (content.contains("node"))
                        tags.add("nodejs");
                if (content.contains("spring"))
                        tags.add("spring-boot");
                if (content.contains("docker"))
                        tags.add("docker");
                if (content.contains("aws"))
                        tags.add("aws");
                if (content.contains("azure"))
                        tags.add("azure");
                if (content.contains("gcp"))
                        tags.add("gcp");
                if (content.contains("database") || content.contains("mongodb") || content.contains("mysql")
                                || content.contains("postgresql"))
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
                if (content.contains("devops"))
                        tags.add("devops");
                if (content.contains("testing"))
                        tags.add("testing");
                if (content.contains("performance"))
                        tags.add("performance");
                if (content.contains("optimization"))
                        tags.add("optimization");

                // Education tags
                if (content.contains("toán") || content.contains("math"))
                        tags.add("mathematics");
                if (content.contains("vật lý") || content.contains("physics"))
                        tags.add("physics");
                if (content.contains("hóa học") || content.contains("chemistry"))
                        tags.add("chemistry");
                if (content.contains("sinh học") || content.contains("biology"))
                        tags.add("biology");
                if (content.contains("văn học") || content.contains("literature"))
                        tags.add("literature");
                if (content.contains("lịch sử") || content.contains("history"))
                        tags.add("history");
                if (content.contains("địa lý") || content.contains("geography"))
                        tags.add("geography");

                // Business tags
                if (content.contains("marketing"))
                        tags.add("marketing");
                if (content.contains("tài chính") || content.contains("finance"))
                        tags.add("finance");
                if (content.contains("quản lý") || content.contains("management"))
                        tags.add("management");
                if (content.contains("kế toán") || content.contains("accounting"))
                        tags.add("accounting");
                if (content.contains("startup") || content.contains("khởi nghiệp"))
                        tags.add("startup");

                // Language tags
                if (content.contains("tiếng anh") || content.contains("english"))
                        tags.add("english");
                if (content.contains("tiếng nhật") || content.contains("japanese"))
                        tags.add("japanese");
                if (content.contains("tiếng hàn") || content.contains("korean"))
                        tags.add("korean");
                if (content.contains("tiếng trung") || content.contains("chinese"))
                        tags.add("chinese");

                // Engineering tags
                if (content.contains("cơ khí") || content.contains("mechanical"))
                        tags.add("mechanical");
                if (content.contains("điện") || content.contains("electrical"))
                        tags.add("electrical");
                if (content.contains("xây dựng") || content.contains("civil"))
                        tags.add("civil-engineering");
                if (content.contains("ô tô") || content.contains("automotive"))
                        tags.add("automotive");

                // Social Science tags
                if (content.contains("tâm lý") || content.contains("psychology"))
                        tags.add("psychology");
                if (content.contains("xã hội") || content.contains("sociology"))
                        tags.add("sociology");
                if (content.contains("triết học") || content.contains("philosophy"))
                        tags.add("philosophy");

                // Health tags
                if (content.contains("y học") || content.contains("medicine"))
                        tags.add("medicine");
                if (content.contains("dược") || content.contains("pharmacy"))
                        tags.add("pharmacy");
                if (content.contains("điều dưỡng") || content.contains("nursing"))
                        tags.add("nursing");

                // Law tags
                if (content.contains("luật"))
                        tags.add("law");

                // Agriculture tags
                if (content.contains("nông nghiệp") || content.contains("agriculture"))
                        tags.add("agriculture");
                if (content.contains("chăn nuôi") || content.contains("animal"))
                        tags.add("animal-science");
                if (content.contains("thủy sản") || content.contains("aquaculture"))
                        tags.add("aquaculture");

                // Environment tags
                if (content.contains("môi trường") || content.contains("environment"))
                        tags.add("environmental-science");
                if (content.contains("biến đổi khí hậu") || content.contains("climate"))
                        tags.add("climate-change");
                if (content.contains("năng lượng tái tạo") || content.contains("renewable"))
                        tags.add("renewable-energy");

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

        /**
         * Tạo dữ liệu mẫu cho tags và document-tag relationships để test popular tags
         */
        private void createSampleTagsAndRelationships() {
                System.out.println("=== Creating sample tags and document-tag relationships ===");

                // Tạo các tags mẫu
                String[] sampleTags = {
                                "Java", "Spring Boot", "Database", "Web Development", "API",
                                "JavaScript", "React", "Vue.js", "Node.js", "Python",
                                "Machine Learning", "Data Science", "DevOps", "Docker", "Kubernetes",
                                "Microservices", "REST API", "GraphQL", "Security", "Testing"
                };

                List<Tag> createdTags = new ArrayList<>();
                for (String tagName : sampleTags) {
                        String slug = tagName.toLowerCase().replaceAll("\\s+", "-");
                        Tag existingTag = tagRepository.findBySlug(slug).orElse(null);
                        if (existingTag == null) {
                                Tag tag = new Tag();
                                tag.setName(tagName);
                                tag.setSlug(slug);
                                tag.setDescription("Tag for " + tagName);
                                tag.setCreatedAt(LocalDateTime.now());
                                tag.setUpdatedAt(LocalDateTime.now());
                                tag = tagRepository.save(tag);
                                createdTags.add(tag);
                                System.out.println("Created tag: " + tagName);
                        } else {
                                createdTags.add(existingTag);
                                System.out.println("Found existing tag: " + tagName);
                        }
                }

                // Lấy tất cả documents
                List<Document> documents = documentRepository.findByDeletedAtIsNull();
                System.out.println("Found " + documents.size() + " documents to tag");

                // Gắn tags ngẫu nhiên cho documents
                Random random = new Random();
                int totalRelationships = 0;

                for (Document document : documents) {
                        // Mỗi document sẽ có 2-5 tags ngẫu nhiên
                        int numTags = random.nextInt(4) + 2; // 2-5 tags
                        List<Tag> documentTags = new ArrayList<>();

                        // Chọn tags ngẫu nhiên
                        for (int i = 0; i < numTags; i++) {
                                Tag randomTag = createdTags.get(random.nextInt(createdTags.size()));
                                if (!documentTags.contains(randomTag)) {
                                        documentTags.add(randomTag);
                                }
                        }

                        // Tạo document-tag relationships
                        for (Tag tag : documentTags) {
                                // Kiểm tra xem relationship đã tồn tại chưa
                                DocumentTagId docTagId = new DocumentTagId();
                                docTagId.setDocumentId(document.getId());
                                docTagId.setTagId(tag.getId());

                                if (!documentTagRepository.existsById(docTagId)) {
                                        DocumentTag documentTag = new DocumentTag();
                                        documentTag.setId(docTagId);
                                        documentTag.setDocument(document);
                                        documentTag.setTag(tag);
                                        documentTag.setCreatedAt(LocalDateTime.now());
                                        documentTagRepository.save(documentTag);
                                        totalRelationships++;
                                }
                        }
                }

                System.out.println("Created " + totalRelationships + " document-tag relationships");
                System.out.println("=== Sample tags and relationships creation completed ===");
        }

}