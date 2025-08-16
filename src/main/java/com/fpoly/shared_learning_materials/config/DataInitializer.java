package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.domain.Category;
import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.domain.Comment;
import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.DocumentCategory;
import com.fpoly.shared_learning_materials.domain.DocumentCategoryId;
import com.fpoly.shared_learning_materials.domain.Reply;
import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.CategoryRepository;
import com.fpoly.shared_learning_materials.repository.CoinPackageRepository;
import com.fpoly.shared_learning_materials.repository.CommentRepository;
import com.fpoly.shared_learning_materials.repository.DocumentCategoryRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.ReplyRepository;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
        private PasswordEncoder passwordEncoder;
        
        @Autowired
        private CommentRepository commentRepository;
        
        @Autowired
        private DocumentRepository documentRepository;
        
        @Autowired
        private ReplyRepository replyRepository;
        
        @Autowired
        private CategoryRepository categoryRepository;
        
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
                
                if (categoryRepository.count() == 0) {
                    createSampleCategories();
                }

                
                if (documentRepository.count() == 0) {
                    createSampleDocuments();
                    System.out.println("Sample documents created successfully!");
                } else {
                    System.out.println("Sample documents already exist, skipping document initialization.");
                }

                if (commentRepository.count() == 0) {
                    createSampleComments();
                    System.out.println("Sample comments created successfully!");
                } else {
                    System.out.println("Sample comments already exist, skipping comment initialization.");
                }
                
                if (replyRepository.count() == 0) {
                    createSampleReplies();
                    System.out.println("Sample replies created successfully!");
                } else {
                    System.out.println("Sample replies already exist, skipping reply initialization.");
                }


                if (!transactionRepository.existsByCodeAndDeletedAtIsNull("TXN000001")) {
                        createSampleTransactions();
                        System.out.println("Sample transactions created successfully!");
                } else {
                        System.out.println("Sample transactions already exist, skipping transaction initialization.");
                }
        }
        
        private void createSampleCategories() {
            if (categoryRepository.count() > 0) {
                System.out.println("Categories already exist, skipping category initialization.");
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            Category[] categories = {
                new Category(null, "Công nghệ thông tin", "cong-nghe-thong-tin", "Tài liệu CNTT", "active", 1, null, now, now, null),
                new Category(null, "Kinh tế", "kinh-te", "Tài liệu kinh tế", "active", 2, null, now, now, null),
                new Category(null, "Kỹ năng mềm", "ky-nang-mem", "Tài liệu kỹ năng", "active", 3, null, now, now, null)
            };

            for (Category cat : categories) {
                categoryRepository.save(cat);
                System.out.println("✅ Đã tạo category: " + cat.getName());
            }
        }

        
        private Document createDocument(String title, String slug, String description,
                BigDecimal price, String status, String visibility,
                LocalDateTime now, User user) {
			Document doc = new Document();
			doc.setTitle(title);
			doc.setSlug(slug);
			doc.setDescription(description);
			doc.setPrice(price);
			doc.setStatus(status);
			doc.setVisibility(visibility);
			doc.setDownloadsCount(0L);
			doc.setViewsCount(0L);
			doc.setCreatedAt(now);
			doc.setUpdatedAt(now);
			doc.setPublishedAt(now);
			doc.setStatus("active");
			doc.setFile(null);
			return doc;
        }
        
        @Autowired
        private DocumentCategoryRepository documentCategoryRepository;
        
        private void createSampleDocuments() {
            try {
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);

                if (user1 == null) {
                    System.err.println("❌ Không tìm thấy user1 để gán cho document.");
                    return;
                }

                LocalDateTime now = LocalDateTime.now();

                Document[] documents = {
                    createDocument("Tài liệu Java cơ bản", "tai-lieu-java-co-ban", "Tài liệu hướng dẫn học Java từ cơ bản đến nâng cao.", new BigDecimal("0"), "published", "public", now, user1),
                    createDocument("Lập trình Spring Boot", "lap-trinh-spring-boot", "Tổng hợp kiến thức về Spring Boot và cách áp dụng trong dự án thực tế.", new BigDecimal("20000"), "published", "public", now, user1),
                    createDocument("Hướng dẫn Git nâng cao", "huong-dan-git-nang-cao", "Các thao tác nâng cao trong Git giúp quản lý mã nguồn hiệu quả hơn.", new BigDecimal("10000"), "published", "private", now, user1),
                    createDocument("SQL Cơ bản đến nâng cao", "sql-co-ban-den-nang-cao", "Tài liệu chi tiết về cú pháp SQL và tối ưu truy vấn.", new BigDecimal("15000"), "published", "public", now, user1),
                    createDocument("Kỹ thuật viết CV xin việc", "ky-thuat-viet-cv", "Bí quyết để viết CV gây ấn tượng với nhà tuyển dụng.", new BigDecimal("5000"), "published", "public", now, user1)
                };

                List<Category> categories = categoryRepository.findAll();

                int i = 0;
                
                for (Document doc : documents) {
                    documentRepository.save(doc);

                    Category category = categories.get(i % categories.size());

                    DocumentCategory dc = new DocumentCategory();
                    dc.setId(new DocumentCategoryId(doc.getId(), category.getId()));
                    dc.setDocument(doc);
                    dc.setCategory(category);
                    dc.setCreatedAt(now);

                    documentCategoryRepository.save(dc);

                    System.out.println("✅ Đã gán document '" + doc.getTitle() + "' vào category '" + category.getName() + "'");

                    i++;
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tạo documents: " + e.getMessage());
                e.printStackTrace();
            }
        }

        
        private Comment createComment(Document document, User user, String content, LocalDateTime now) {
            Comment comment = new Comment();
            comment.setDocument(document);
            comment.setUser(user);
            comment.setContent(content);
            comment.setStatus("active");
            comment.setCreatedAt(now);
            comment.setUpdatedAt(now);
            return comment;
        }

        private Document getAnyActiveDocument() {
            return documentRepository.findFirstByDeletedAtIsNull().orElse(null);
        }

        
        private void createSampleComments() {
            try {
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);
                Document document = getAnyActiveDocument();

                if (user1 == null || user2 == null || document == null) {
                    System.err.println("❌ Thiếu user hoặc document để tạo comment.");
                    System.err.println("user1: " + (user1 == null ? "null" : "OK"));
                    System.err.println("user2: " + (user2 == null ? "null" : "OK"));
                    System.err.println("document: " + (document == null ? "null" : "OK"));
                    return;
                }

                LocalDateTime now = LocalDateTime.now();

                Comment[] comments = {
                    createComment(document, user1, "Tài liệu này rất hữu ích, cảm ơn bạn đã chia sẻ!", now),
                    createComment(document, user2, "Mình có thể dùng nội dung này cho bài thuyết trình không?", now),
                    createComment(document, user1, "Góp ý: nên bổ sung thêm ví dụ minh họa.", now),
                    createComment(document, user2, "Rất dễ hiểu, mình đã học được rất nhiều!", now),
                    createComment(document, user1, "Có bản cập nhật mới hơn không nhỉ?", now)
                };

                for (Comment comment : comments) {
                    commentRepository.save(comment);
                    System.out.println("✅ Đã lưu comment: " + comment.getContent());
                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tạo comments: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private Reply createReply(Comment comment, User user, String content, LocalDateTime now) {
            Reply reply = new Reply();
            reply.setComment(comment);
            reply.setUser(user);
            reply.setContent(content);
            reply.setStatus("active");
            reply.setCreatedAt(now);
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
                    createReply(comments.get(1), user1, "Mình nghĩ là được, nhớ ghi nguồn nhé.", now),
                    createReply(comments.get(2), user2, "Ý kiến hay, mình cũng muốn thêm ví dụ.", now),
                    createReply(comments.get(3), user1, "Chính xác, rất dễ hiểu luôn.", now),
                    createReply(comments.get(4), user2, "Hình như có bản cập nhật hôm trước đấy.", now)
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
                User user1 = userRepository.findByUsernameAndDeletedAtIsNull("user1").orElse(null);
                User user2 = userRepository.findByUsernameAndDeletedAtIsNull("user2").orElse(null);

                if (user1 != null && user2 != null) {
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
        
        
}