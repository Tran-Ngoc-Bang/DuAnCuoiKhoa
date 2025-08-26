package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.domain.DocumentOwner;
import com.fpoly.shared_learning_materials.domain.DocumentOwnerId;
import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.TransactionDetail;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.DocumentOwnerRepository;
import com.fpoly.shared_learning_materials.repository.TransactionDetailRepository;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.EmailConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentPurchaseService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentOwnerRepository documentOwnerRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Autowired(required = false)
    private EmailConfigService emailConfigService;

    // Hằng số cho hoa hồng
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.15"); // 15%
    private static final BigDecimal SELLER_RATE = new BigDecimal("0.85"); // 85%

    /**
     * Xử lý mua/tải tài liệu bằng xu.
     * - Không trừ xu nếu user đã sở hữu
     * - Nếu có giá > 0 và chưa sở hữu: tạo Transaction DOCUMENT_DOWNLOAD, trừ xu,
     * ghi ownership, tăng download
     * - Chia xu cho người bán (85%) và trừ hoa hồng (15%)
     */
    @Transactional
    public Transaction processDocumentDownload(User user, Long documentId) {
        Document document = documentService.findById(documentId);
        if (document == null || document.getFile() == null) {
            throw new IllegalArgumentException("Không tìm thấy tài liệu hoặc file không tồn tại");
        }

        // Nếu đã sở hữu: không trừ xu, chỉ tăng download và trả null transaction
        boolean alreadyOwned = documentOwnerRepository.existsByUserAndDocument(user, document);
        if (alreadyOwned) {
            incrementDownloadStats(document);
            return null;
        }

        BigDecimal price = document.getPrice() != null ? document.getPrice() : BigDecimal.ZERO;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            // Tài liệu miễn phí: ghi ownership và tăng download
            saveOwnership(user, document, "free");
            incrementDownloadStats(document);
            return null;
        }

        BigDecimal balance = user.getCoinBalance() != null ? user.getCoinBalance() : BigDecimal.ZERO;
        if (balance.compareTo(price) < 0) {
            throw new IllegalStateException("Số dư xu không đủ để tải tài liệu này");
        }

        // Kiểm tra lịch sử mua (idempotent): nếu đã có giao dịch completed cho tài liệu
        // này thì không trừ nữa
        boolean purchasedBefore = transactionDetailRepository.existsCompletedDocumentPurchase(user, documentId);
        if (purchasedBefore) {
            saveOwnership(user, document, "buyer");
            incrementDownloadStats(document);
            return null;
        }

        // Tìm người bán (owner của tài liệu)
        User seller = findDocumentSeller(document);
        if (seller == null) {
            throw new IllegalStateException("Không tìm thấy thông tin người bán tài liệu");
        }

        // Tạo transaction PURCHASE ở trạng thái PENDING
        Transaction txn = new Transaction();
        txn.setType(Transaction.TransactionType.PURCHASE);
        txn.setStatus(Transaction.TransactionStatus.PENDING);
        txn.setUser(user);
        txn.setPaymentMethod("COIN_BALANCE");
        txn.setAmount(price);
        txn.setCode(generateDocumentDownloadCode());
        txn.setCreatedAt(LocalDateTime.now());
        txn = transactionRepository.save(txn);

        try {
            // Trừ xu từ người mua
            user.setCoinBalance(balance.subtract(price));
            userRepository.save(user);

            // Tính toán số xu cho người bán và hoa hồng
            BigDecimal commission = price.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal sellerAmount = price.multiply(SELLER_RATE).setScale(2, RoundingMode.HALF_UP);

            // Cộng xu cho người bán
            BigDecimal sellerBalance = seller.getCoinBalance() != null ? seller.getCoinBalance() : BigDecimal.ZERO;
            seller.setCoinBalance(sellerBalance.add(sellerAmount));
            userRepository.save(seller);

            // Lưu chi tiết giao dịch cho người mua
            TransactionDetail buyerDetail = new TransactionDetail();
            buyerDetail.setTransaction(txn);
            buyerDetail.setDetailType("document");
            buyerDetail.setReferenceId(document.getId());
            buyerDetail.setQuantity(1);
            buyerDetail.setUnitPrice(price);
            buyerDetail.setAmount(price);
            transactionDetailRepository.save(buyerDetail);

            // Tạo transaction cho người bán (nếu có xu nhận được)
            if (sellerAmount.compareTo(BigDecimal.ZERO) > 0) {
                Transaction sellerTxn = new Transaction();
                sellerTxn.setType(Transaction.TransactionType.PURCHASE); // Sử dụng PURCHASE thay vì DOCUMENT_SALE
                sellerTxn.setStatus(Transaction.TransactionStatus.COMPLETED);
                sellerTxn.setUser(seller);
                sellerTxn.setPaymentMethod("DOCUMENT_SALE");
                sellerTxn.setAmount(sellerAmount);
                sellerTxn.setCode(generateDocumentSaleCode());
                sellerTxn.setNotes("Nhận xu từ việc bán tài liệu: " + document.getTitle() +
                        " (Hoa hồng: " + commission + " xu)");
                sellerTxn.setCreatedAt(LocalDateTime.now());
                sellerTxn.setUpdatedAt(LocalDateTime.now());
                sellerTxn = transactionRepository.save(sellerTxn);

                // Lưu chi tiết giao dịch cho người bán
                TransactionDetail sellerDetail = new TransactionDetail();
                sellerDetail.setTransaction(sellerTxn);
                sellerDetail.setDetailType("document_sale");
                sellerDetail.setReferenceId(document.getId());
                sellerDetail.setQuantity(1);
                sellerDetail.setUnitPrice(sellerAmount);
                sellerDetail.setAmount(sellerAmount);
                transactionDetailRepository.save(sellerDetail);
            }

            // Ghi ownership cho người mua
            saveOwnership(user, document, "buyer");

            // Tăng download và lưu document
            incrementDownloadStats(document);

            // Cập nhật trạng thái giao dịch
            txn.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            // Thông báo cho người mua
            if (notificationService != null) {
                notificationService.createNotification(user, "Mua tài liệu thành công",
                        "Bạn đã mua '" + document.getTitle() + "' và bị trừ " + price + " xu.",
                        "transaction");
            }

            // Thông báo cho người bán
            if (notificationService != null && sellerAmount.compareTo(BigDecimal.ZERO) > 0) {
                notificationService.createNotification(seller, "Tài liệu được mua",
                        "Tài liệu '" + document.getTitle() + "' đã được mua thành công. Bạn nhận được " +
                                sellerAmount + " xu (hoa hồng: " + commission + " xu).",
                        "transaction");
            }

            // Gửi email xác nhận cho người mua
            if (emailConfigService != null && user.getEmail() != null && !user.getEmail().isBlank()) {
                String subject = "Xác nhận mua tài liệu thành công";
                String html = "<p>Chào " + (user.getFullName() != null ? user.getFullName() : user.getUsername())
                        + ",</p>"
                        + "<p>Bạn đã mua thành công tài liệu: <strong>" + document.getTitle() + "</strong>.</p>"
                        + "<p>Số xu đã trừ: <strong>" + price + " xu</strong>.</p>"
                        + "<p>Mã giao dịch: <strong>" + txn.getCode() + "</strong>.</p>"
                        + "<p>Cảm ơn bạn đã sử dụng EduShare.</p>";
                try {
                    emailConfigService.sendHtmlEmail(user.getEmail(), subject, html);
                } catch (Exception ignore) {
                }
            }

            // Gửi email thông báo cho người bán
            if (emailConfigService != null && seller.getEmail() != null && !seller.getEmail().isBlank()
                    && sellerAmount.compareTo(BigDecimal.ZERO) > 0) {
                String subject = "Tài liệu của bạn đã được mua";
                String html = "<p>Chào " + (seller.getFullName() != null ? seller.getFullName() : seller.getUsername())
                        + ",</p>"
                        + "<p>Tài liệu <strong>" + document.getTitle() + "</strong> đã được mua thành công.</p>"
                        + "<p>Số xu bạn nhận được: <strong>" + sellerAmount + " xu</strong>.</p>"
                        + "<p>Hoa hồng hệ thống: <strong>" + commission + " xu</strong>.</p>"
                        + "<p>Bạn có thể rút xu hoặc sử dụng để mua tài liệu khác.</p>";
                try {
                    emailConfigService.sendHtmlEmail(seller.getEmail(), subject, html);
                } catch (Exception ignore) {
                }
            }

            return txn;
        } catch (RuntimeException ex) {
            // Rollback mềm: đánh dấu FAILED và hoàn xu nếu đã trừ
            try {
                // Nếu đã trừ từ người mua, hoàn lại
                User fresh = userRepository.findById(user.getId()).orElse(user);
                fresh.setCoinBalance(
                        (fresh.getCoinBalance() != null ? fresh.getCoinBalance() : BigDecimal.ZERO).add(price));
                userRepository.save(fresh);
            } catch (Exception ignore) {
            }

            txn.setStatus(Transaction.TransactionStatus.FAILED);
            txn.setNotes("Error: " + ex.getMessage());
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);
            throw ex;
        }
    }

    /**
     * Tìm người bán (owner) của tài liệu
     */
    private User findDocumentSeller(Document document) {
        List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(document.getId());
        for (DocumentOwner owner : owners) {
            if ("owner".equals(owner.getOwnershipType())) {
                return owner.getUser();
            }
        }
        // Nếu không tìm thấy owner, trả về owner đầu tiên
        return owners.isEmpty() ? null : owners.get(0).getUser();
    }

    private void saveOwnership(User user, Document document, String ownershipType) {
        if (!documentOwnerRepository.existsByUserAndDocument(user, document)) {
            DocumentOwner owner = new DocumentOwner();
            owner.setId(new DocumentOwnerId(user.getId(), document.getId()));
            owner.setUser(user);
            owner.setDocument(document);
            owner.setOwnershipType(ownershipType);
            owner.setCreatedAt(LocalDateTime.now());
            documentOwnerRepository.save(owner);
        }
    }

    private void incrementDownloadStats(Document document) {
        document.setDownloadsCount((document.getDownloadsCount() != null ? document.getDownloadsCount() : 0L) + 1);
        documentService.save(document);
    }

    private String generateDocumentDownloadCode() {
        // Reuse pattern with prefix TXN; could be prefixed DD for clarity
        String prefix = "TXN";
        int counter = 1;
        String code;
        do {
            code = prefix + String.format("%06d", counter);
            counter++;
        } while (transactionRepository.existsByCodeAndDeletedAtIsNull(code));
        return code;
    }

    private String generateDocumentSaleCode() {
        // Prefix TXN for Document Sale (following the pattern)
        String prefix = "TXN";
        int counter = 1;
        String code;
        do {
            code = prefix + String.format("%06d", counter);
            counter++;
        } while (transactionRepository.existsByCodeAndDeletedAtIsNull(code));
        return code;
    }
}