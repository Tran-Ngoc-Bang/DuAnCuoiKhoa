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
import java.time.LocalDateTime;

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

    /**
     * Xử lý mua/tải tài liệu bằng xu.
     * - Không trừ xu nếu user đã sở hữu
     * - Nếu có giá > 0 và chưa sở hữu: tạo Transaction DOCUMENT_DOWNLOAD, trừ xu,
     * ghi ownership, tăng download
     */
    @Transactional
    public Transaction processDocumentDownload(User user, Long documentId) {
        Document document = documentService.findById(documentId);
        if (document == null || document.getFile() == null) {
            throw new IllegalArgumentException("Không tìm thấy tài liệu hoặc file không tồn tại");
        }

        // Nếu đã sở hữu: không trừ xu, chỉ tăng download và trả null transaction (hoặc
        // giao dịch thông tin)
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

        // Tạo transaction DOCUMENT_DOWNLOAD ở trạng thái PENDING
        Transaction txn = new Transaction();
        txn.setType(Transaction.TransactionType.DOCUMENT_DOWNLOAD);
        txn.setStatus(Transaction.TransactionStatus.PENDING);
        txn.setUser(user);
        txn.setPaymentMethod("COIN_BALANCE");
        txn.setAmount(price);
        txn.setCode(generateDocumentDownloadCode());
        txn.setCreatedAt(LocalDateTime.now());
        txn = transactionRepository.save(txn);

        try {
            // Trừ xu ngay và chuyển COMPLETED (đồng bộ)
            user.setCoinBalance(balance.subtract(price));
            userRepository.save(user);

            // Lưu chi tiết giao dịch
            TransactionDetail detail = new TransactionDetail();
            detail.setTransaction(txn);
            detail.setDetailType("document");
            detail.setReferenceId(document.getId());
            detail.setQuantity(1);
            detail.setUnitPrice(price);
            detail.setAmount(price);
            transactionDetailRepository.save(detail);

            // Ghi ownership cho người mua
            saveOwnership(user, document, "buyer");

            // Tăng download và lưu document
            incrementDownloadStats(document);

            // Cập nhật trạng thái giao dịch
            txn.setStatus(Transaction.TransactionStatus.COMPLETED);
            txn.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(txn);

            // Thông báo
            if (notificationService != null) {
                notificationService.createNotification(user, "Tải tài liệu thành công",
                        "Bạn đã tải '" + document.getTitle() + "' và bị trừ " + price + " xu.",
                        "transaction");
            }

            // Gửi email xác nhận
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

            return txn;
        } catch (RuntimeException ex) {
            // Rollback mềm: đánh dấu FAILED và hoàn xu nếu đã trừ
            try {
                // Nếu đã trừ, hoàn lại
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
}