package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.domain.*;
import com.fpoly.shared_learning_materials.repository.*;
import com.fpoly.shared_learning_materials.service.SettingService;

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
        private TransactionRepository transactionRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private TagRepository tagRepository;

        @Autowired
        private FileRepository fileRepository;

        @Autowired
        private DocumentRepository documentRepository;

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private FavoriteRepository favoriteRepository;

        @Autowired
        private ReportRepository reportRepository;

        @Autowired
        private SettingService settingService;

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

                if (!transactionRepository.existsByCodeAndDeletedAtIsNull("TXN000001")) {
                        createSampleTransactions();
                        System.out.println("Sample transactions created successfully!");
                } else {
                        System.out.println("Sample transactions already exist, skipping transaction initialization.");
                }

                if (!categoryRepository.existsBySlugAndDeletedAtIsNull("lap-trinh")) {
                        createSampleCategories();
                        System.out.println("Sample categories created successfully!");
                } else {
                        System.out.println("Sample categories already exist, skipping category initialization.");
                }

                if (!tagRepository.existsBySlugAndDeletedAtIsNull("java")) {
                        createSampleTags();
                        System.out.println("Sample tags created successfully!");
                } else {
                        System.out.println("Sample tags already exist, skipping tag initialization.");
                }

                if (!fileRepository.existsByFileNameAndDeletedAtIsNull("sample-document.pdf")) {
                        createSampleFiles();
                        System.out.println("Sample files created successfully!");
                } else {
                        System.out.println("Sample files already exist, skipping file initialization.");
                }

                if (!documentRepository.existsBySlugAndDeletedAtIsNull("tai-lieu-java-co-ban")) {
                        createSampleDocuments();
                        System.out.println("Sample documents created successfully!");
                } else {
                        System.out.println("Sample documents already exist, skipping document initialization.");
                }

                if (!commentRepository.existsByContentAndDeletedAtIsNull("Tài liệu rất hữu ích!")) {
                        createSampleComments();
                        System.out.println("Sample comments created successfully!");
                } else {
                        System.out.println("Sample comments already exist, skipping comment initialization.");
                }

                createSampleNotifications();
                createSampleFavorites();
                createSampleReports();
                
                // Khởi tạo settings mặc định
                settingService.initializeDefaultSettings();
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

        private void createSampleTransactions() {
                // Get users for transactions
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                if (user1 != null && user2 != null) {
                        // Create sample transactions
                        createTransaction("TXN000001", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("50000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói starter");

                        createTransaction("TXN000002", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("100000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Mua xu gói premium");

                        createTransaction("TXN000003", Transaction.TransactionType.WITHDRAWAL,
                                        new BigDecimal("25000.00"), Transaction.TransactionStatus.PENDING,
                                        "Bank Transfer", user1, "Rút tiền về tài khoản ngân hàng");

                        createTransaction("TXN000004", Transaction.TransactionType.PURCHASE,
                                        new BigDecimal("50000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user2, "Mua xu gói starter");

                        createTransaction("TXN000005", Transaction.TransactionType.REFUND,
                                        new BigDecimal("25000.00"), Transaction.TransactionStatus.COMPLETED,
                                        "VNPay", user1, "Hoàn tiền giao dịch lỗi");
                }
        }

        private void createTransaction(String code, Transaction.TransactionType type,
                        BigDecimal amount, Transaction.TransactionStatus status,
                        String paymentMethod, User user, String notes) {
                Transaction transaction = new Transaction();
                transaction.setCode(code);
                transaction.setType(type);
                transaction.setAmount(amount);
                transaction.setStatus(status);
                transaction.setPaymentMethod(paymentMethod);
                transaction.setUser(user);
                transaction.setNotes(notes);
                transaction.setCreatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);
        }

        private void createSampleCategories() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);

                Category[] categories = {
                                createCategory("Lập trình", "lap-trinh", "Các tài liệu về lập trình", admin),
                                createCategory("Cơ sở dữ liệu", "co-so-du-lieu", "Tài liệu về database và SQL", admin),
                                createCategory("Mạng máy tính", "mang-may-tinh", "Kiến thức về networking", admin),
                                createCategory("Hệ điều hành", "he-dieu-hanh", "Tài liệu về operating systems", admin),
                                createCategory("Toán học", "toan-hoc", "Toán học ứng dụng trong IT", admin),
                                createCategory("Tiếng Anh", "tieng-anh", "Tài liệu học tiếng Anh IT", admin),
                                createCategory("Thiết kế", "thiet-ke", "UI/UX và graphic design", admin),
                                createCategory("Quản lý dự án", "quan-ly-du-an", "Project management", admin)
                };

                for (Category category : categories) {
                        categoryRepository.save(category);
                }
        }

        private Category createCategory(String name, String slug, String description, User createdBy) {
                Category category = new Category();
                category.setName(name);
                category.setSlug(slug);
                category.setDescription(description);
                category.setStatus("active");
                category.setSortOrder(0);
                category.setCreatedBy(createdBy);
                category.setCreatedAt(LocalDateTime.now());
                category.setUpdatedAt(LocalDateTime.now());
                return category;
        }

        private void createSampleTags() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);

                Tag[] tags = {
                                createTag("Java", "java", "Ngôn ngữ lập trình Java", admin),
                                createTag("Spring Boot", "spring-boot", "Framework Spring Boot", admin),
                                createTag("JavaScript", "javascript", "Ngôn ngữ JavaScript", admin),
                                createTag("React", "react", "Thư viện React", admin),
                                createTag("MySQL", "mysql", "Hệ quản trị CSDL MySQL", admin),
                                createTag("MongoDB", "mongodb", "NoSQL database MongoDB", admin),
                                createTag("Docker", "docker", "Container technology", admin),
                                createTag("AWS", "aws", "Amazon Web Services", admin),
                                createTag("Git", "git", "Version control system", admin),
                                createTag("API", "api", "Application Programming Interface", admin)
                };

                for (Tag tag : tags) {
                        tagRepository.save(tag);
                }
        }

        private Tag createTag(String name, String slug, String description, User createdBy) {
                Tag tag = new Tag();
                tag.setName(name);
                tag.setSlug(slug);
                tag.setDescription(description);
                tag.setCreatedBy(createdBy);
                tag.setCreatedAt(LocalDateTime.now());
                tag.setUpdatedAt(LocalDateTime.now());
                return tag;
        }

        private void createSampleFiles() {
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);

                File[] files = {
                                createFile("sample-document.pdf", "/uploads/documents/sample-document.pdf", "pdf",
                                                1024000L, "application/pdf", "abc123def456", admin),
                                createFile("java-tutorial.docx", "/uploads/documents/java-tutorial.docx", "docx",
                                                2048000L,
                                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                                "def456ghi789", user1),
                                createFile("database-design.pptx", "/uploads/documents/database-design.pptx", "pptx",
                                                3072000L,
                                                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                                                "ghi789jkl012", admin),
                                createFile("web-development.zip", "/uploads/documents/web-development.zip", "zip",
                                                5120000L, "application/zip", "jkl012mno345", user1),
                                createFile("algorithms.txt", "/uploads/documents/algorithms.txt", "txt",
                                                512000L, "text/plain", "mno345pqr678", admin)
                };

                for (File file : files) {
                        fileRepository.save(file);
                }
        }

        private File createFile(String fileName, String filePath, String fileType, Long fileSize,
                        String mimeType, String checksum, User uploadedBy) {
                File file = new File();
                file.setFileName(fileName);
                file.setFilePath(filePath);
                file.setFileType(fileType);
                file.setFileSize(fileSize);
                file.setMimeType(mimeType);
                file.setChecksum(checksum);
                file.setUploadedBy(uploadedBy);
                file.setStatus("active");
                file.setCreatedAt(LocalDateTime.now());
                file.setUpdatedAt(LocalDateTime.now());
                return file;
        }

        private void createSampleDocuments() {
                File file1 = fileRepository.findByFileNameAndDeletedAtIsNull("sample-document.pdf").orElse(null);
                File file2 = fileRepository.findByFileNameAndDeletedAtIsNull("java-tutorial.docx").orElse(null);
                File file3 = fileRepository.findByFileNameAndDeletedAtIsNull("database-design.pptx").orElse(null);

                Document[] documents = {
                                createDocument("Tài liệu Java cơ bản", "tai-lieu-java-co-ban",
                                                "Hướng dẫn học Java từ cơ bản đến nâng cao", new BigDecimal("50000"),
                                                file1),
                                createDocument("Thiết kế cơ sở dữ liệu", "thiet-ke-co-so-du-lieu",
                                                "Nguyên lý thiết kế database hiệu quả", new BigDecimal("75000"), file2),
                                createDocument("Phát triển Web hiện đại", "phat-trien-web-hien-dai",
                                                "Công nghệ web mới nhất 2024", new BigDecimal("100000"), file3),
                                createDocument("Tài liệu miễn phí", "tai-lieu-mien-phi",
                                                "Tài liệu học tập không mất phí", BigDecimal.ZERO, null),
                                createDocument("Khóa học Spring Boot", "khoa-hoc-spring-boot",
                                                "Học Spring Boot từ A-Z", new BigDecimal("200000"), null)
                };

                for (Document document : documents) {
                        documentRepository.save(document);
                }
        }

        private Document createDocument(String title, String slug, String description, BigDecimal price, File file) {
                Document document = new Document();
                document.setTitle(title);
                document.setSlug(slug);
                document.setDescription(description);
                document.setPrice(price);
                document.setFile(file);
                document.setStatus("approved");
                document.setVisibility("public");
                document.setDownloadsCount(0L);
                document.setViewsCount(0L);
                document.setCreatedAt(LocalDateTime.now());
                document.setUpdatedAt(LocalDateTime.now());
                document.setPublishedAt(LocalDateTime.now());
                return document;
        }

        private void createSampleComments() {
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                Document doc1 = documentRepository.findBySlugAndDeletedAtIsNull("tai-lieu-java-co-ban").orElse(null);
                Document doc2 = documentRepository.findBySlugAndDeletedAtIsNull("thiet-ke-co-so-du-lieu").orElse(null);

                if (user1 != null && user2 != null && doc1 != null && doc2 != null) {
                        Comment[] comments = {
                                        createComment(doc1, user1, "Tài liệu rất hữu ích!"),
                                        createComment(doc1, user2, "Cảm ơn tác giả đã chia sẻ"),
                                        createComment(doc2, user1, "Nội dung chi tiết và dễ hiểu"),
                                        createComment(doc2, user2, "Có thể bổ sung thêm ví dụ thực tế"),
                                        createComment(doc1, user2, "Đánh giá 5 sao cho tài liệu này")
                        };

                        for (Comment comment : comments) {
                                commentRepository.save(comment);
                        }
                }
        }

        private Comment createComment(Document document, User user, String content) {
                Comment comment = new Comment();
                comment.setDocument(document);
                comment.setUser(user);
                comment.setContent(content);
                comment.setStatus("active");
                comment.setCreatedAt(LocalDateTime.now());
                comment.setUpdatedAt(LocalDateTime.now());
                return comment;
        }

        private void createSampleNotifications() {
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                if (user1 != null && user2 != null) {
                        Notification[] notifications = {
                                        createNotification(user1, "Chào mừng đến với hệ thống",
                                                        "Cảm ơn bạn đã đăng ký tài khoản", "system"),
                                        createNotification(user1, "Giao dịch thành công",
                                                        "Bạn đã mua xu thành công", "transaction"),
                                        createNotification(user2, "Tài liệu mới",
                                                        "Có tài liệu mới trong danh mục yêu thích", "document"),
                                        createNotification(user2, "Báo cáo được xử lý",
                                                        "Báo cáo của bạn đã được xem xét", "report"),
                                        createNotification(user1, "Khuyến mãi đặc biệt",
                                                        "Giảm giá 20% cho gói xu Premium", "system")
                        };

                        for (Notification notification : notifications) {
                                notificationRepository.save(notification);
                        }
                }
        }

        private Notification createNotification(User user, String title, String message, String type) {
                Notification notification = new Notification();
                notification.setUser(user);
                notification.setTitle(title);
                notification.setMessage(message);
                notification.setType(type);
                notification.setIsRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                return notification;
        }

        private void createSampleFavorites() {
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                Document doc1 = documentRepository.findBySlugAndDeletedAtIsNull("tai-lieu-java-co-ban").orElse(null);
                Document doc2 = documentRepository.findBySlugAndDeletedAtIsNull("thiet-ke-co-so-du-lieu").orElse(null);
                Document doc3 = documentRepository.findBySlugAndDeletedAtIsNull("phat-trien-web-hien-dai").orElse(null);

                if (user1 != null && user2 != null && doc1 != null && doc2 != null && doc3 != null) {
                        Favorite[] favorites = {
                                        createFavorite(user1, doc1),
                                        createFavorite(user1, doc2),
                                        createFavorite(user2, doc1),
                                        createFavorite(user2, doc3)
                        };

                        for (Favorite favorite : favorites) {
                                favoriteRepository.save(favorite);
                        }
                }
        }

        private Favorite createFavorite(User user, Document document) {
                Favorite favorite = new Favorite();
                favorite.setUser(user);
                favorite.setDocument(document);
                favorite.setCreatedAt(LocalDateTime.now());
                return favorite;
        }

        private void createSampleReports() {
                // Kiểm tra xem đã có báo cáo mẫu chưa
                if (reportRepository.count() > 0) {
                        System.out.println("Sample reports already exist, skipping report initialization.");
                        return;
                }

                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                User admin = userRepository.findByUsernameAndDeletedAtIsNull("admin").orElse(null);
                Document doc1 = documentRepository.findBySlugAndDeletedAtIsNull("tai-lieu-java-co-ban").orElse(null);
                Document doc2 = documentRepository.findBySlugAndDeletedAtIsNull("thiet-ke-co-so-du-lieu").orElse(null);
                Document doc3 = documentRepository.findBySlugAndDeletedAtIsNull("phat-trien-web-hien-dai").orElse(null);

                System.out.println("Debug - Users found: user1=" + (user1 != null) + ", user2=" + (user2 != null) + ", admin=" + (admin != null));
                System.out.println("Debug - Documents found: doc1=" + (doc1 != null) + ", doc2=" + (doc2 != null) + ", doc3=" + (doc3 != null));

                if (user1 != null && user2 != null && admin != null && doc1 != null && doc2 != null && doc3 != null) {
                        System.out.println("Creating sample reports with different types...");
                        Report[] reports = {
                                        // Copyright violations
                                        createReport(user1, doc1, null, "copyright",
                                                        "Tài liệu này vi phạm bản quyền của công ty ABC", admin,
                                                        "resolved"),
                                        createReport(user2, doc2, null, "copyright",
                                                        "Nội dung được sao chép từ sách giáo khoa mà không có phép",
                                                        null, "pending"),

                                        // Inappropriate content
                                        createReport(user1, doc3, null, "inappropriate",
                                                        "Tài liệu chứa nội dung không phù hợp với độ tuổi", admin,
                                                        "rejected"),
                                        createReport(user2, doc1, null, "inappropriate",
                                                        "Có hình ảnh và ngôn từ không phù hợp", null, "new"),

                                        // Spam content
                                        createReport(user1, doc2, null, "spam",
                                                        "Tài liệu chỉ là quảng cáo cho khóa học trả phí", null,
                                                        "pending"),
                                        createReport(user2, doc3, null, "spam", "Nội dung spam, toàn link affiliate",
                                                        admin, "resolved"),

                                        // Fake/misleading content
                                        createReport(user1, doc1, null, "fake",
                                                        "Thông tin trong tài liệu hoàn toàn sai lệch và gây hiểu lầm",
                                                        null, "new"),
                                        createReport(user2, doc2, null, "fake",
                                                        "Tài liệu giả mạo, không có nguồn gốc rõ ràng", admin,
                                                        "resolved"),

                                        // Other violations
                                        createReport(user1, doc3, null, "other",
                                                        "Vi phạm quy định về định dạng file và kích thước", null,
                                                        "pending"),
                                        createReport(user2, doc1, null, "other", "Tài liệu không đúng danh mục đã chọn",
                                                        admin, "rejected")
                        };

                        for (Report report : reports) {
                                reportRepository.save(report);
                                System.out.println("Created report: " + report.getType() + " - " + report.getStatus());
                        }
                        System.out.println("Sample reports created successfully!");
                } else {
                        System.out.println("Warning: Cannot create sample reports - missing required data:");
                        System.out.println("  - user1: " + (user1 != null ? "OK" : "MISSING"));
                        System.out.println("  - user2: " + (user2 != null ? "OK" : "MISSING"));
                        System.out.println("  - admin: " + (admin != null ? "OK" : "MISSING"));
                        System.out.println("  - doc1: " + (doc1 != null ? "OK" : "MISSING"));
                        System.out.println("  - doc2: " + (doc2 != null ? "OK" : "MISSING"));
                        System.out.println("  - doc3: " + (doc3 != null ? "OK" : "MISSING"));
                }
        }

        private Report createReport(User reporter, Document document, Comment comment, String type, String reason,
                        User reviewer, String status) {
                Report report = new Report();
                report.setReporter(reporter);
                report.setDocument(document);
                report.setComment(comment);
                
                // Đảm bảo type không null
                report.setType(type != null ? type : "other");
                
                // Đảm bảo reason không null
                report.setReason(reason != null ? reason : "Không có lý do cụ thể");
                
                // Đảm bảo status không null
                report.setStatus(status != null ? status : "pending");
                
                report.setCreatedAt(LocalDateTime.now());
                
                if (reviewer != null && ("resolved".equals(status) || "rejected".equals(status))) {
                        report.setReviewer(reviewer);
                        report.setReviewedAt(LocalDateTime.now());
                }
                
                System.out.println("Creating report - Type: " + report.getType() + ", Status: " + report.getStatus() + ", Reporter: " + (reporter != null ? reporter.getFullName() : "null"));
                
                return report;
        }
}