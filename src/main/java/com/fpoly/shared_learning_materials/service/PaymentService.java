package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.dto.PaymentRequest;
import com.fpoly.shared_learning_materials.dto.PaymentResult;
import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.repository.TransactionRepository;
import com.fpoly.shared_learning_materials.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    // VNPay Configuration
    @Value("${payment.vnpay.tmn-code:DEMO}")
    private String vnpayTmnCode;

    @Value("${payment.vnpay.secret-key:DEMO}")
    private String vnpaySecretKey;

    @Value("${payment.vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnpayUrl;

    @Value("${payment.vnpay.return-url:http://localhost:8080/payment/vnpay/return}")
    private String vnpayReturnUrl;

    @Value("${payment.vnpay.notify-url:http://localhost:8080/payment/vnpay/callback}")
    private String vnpayNotifyUrl;

    // QR Banking Configuration
    @Value("${payment.qr-banking.bank-code:VCB}")
    private String qrBankCode;

    @Value("${payment.qr-banking.bank-bin:970407}") // Techcombank BIN default
    private String qrBankBin;

    @Value("${payment.qr-banking.account-number:1234567890}")
    private String qrAccountNumber;

    @Value("${payment.qr-banking.account-name:EDUSHARE}")
    private String qrAccountName;

    /**
     * Process QR Banking payment
     */
    public PaymentResult processQRPayment(PaymentRequest request) {
        try {
            // Validate transaction
            Optional<Transaction> transactionOpt = transactionRepository
                    .findByCodeAndDeletedAtIsNull(request.getTransactionCode());
            if (!transactionOpt.isPresent()) {
                return new PaymentResult(false, "Giao dịch không tồn tại", null, null,
                        request.getTransactionCode(), request.getAmount(), "qr_banking", "INVALID_TRANSACTION", null);
            }

            Transaction transaction = transactionOpt.get();

            // Generate QR code data for bank transfer (VietQR URL)
            String qrData = generateQRCodeData(transaction);

            // Update transaction with QR info
            transaction.setPaymentMethod("QR_BANKING");
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            log.info("QR Code generated for transaction {}: {}", transaction.getCode(), qrData);
            return new PaymentResult(true, "QR Code được tạo thành công", null, qrData,
                    transaction.getCode(), transaction.getAmount(), "qr_banking", null, null);

        } catch (Exception e) {
            return new PaymentResult(false, "Lỗi tạo QR Code", null, null,
                    request.getTransactionCode(), request.getAmount(), "qr_banking", "QR_GENERATION_ERROR",
                    e.getMessage());
        }
    }

    /**
     * Process VNPay payment
     */
    public PaymentResult processVNPayPayment(PaymentRequest request) {
        try {
            // Validate transaction
            Optional<Transaction> transactionOpt = transactionRepository
                    .findByCodeAndDeletedAtIsNull(request.getTransactionCode());
            if (!transactionOpt.isPresent()) {
                return new PaymentResult(false, "Giao dịch không tồn tại", null, null,
                        request.getTransactionCode(), request.getAmount(), "vnpay", "INVALID_TRANSACTION", null);
            }

            Transaction transaction = transactionOpt.get();

            // Create VNPay URL with parameters
            String paymentUrl = createVNPayUrl(transaction);
            log.info("VNPay URL created for {}: {}", transaction.getCode(), paymentUrl);

            // Update transaction with VNPay info
            transaction.setPaymentMethod("VNPAY");
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return new PaymentResult(true, "VNPay URL được tạo thành công", paymentUrl, null,
                    transaction.getCode(), transaction.getAmount(), "vnpay", null, null);

        } catch (Exception e) {
            log.error("processVNPayPayment error for {}: {}", request.getTransactionCode(), e.getMessage(), e);
            return new PaymentResult(false, "Lỗi tạo VNPay URL", null, null,
                    request.getTransactionCode(), request.getAmount(), "vnpay", "VNPAY_URL_ERROR", e.getMessage());
        }
    }

    /**
     * Process payment callback from gateway (IPN)
     */
    public PaymentResult processIPNCallback(String paymentMethod, Map<String, String> params) {
        try {
            String transactionCode = params.get("vnp_TxnRef"); // VNPay
            if (transactionCode == null) {
                transactionCode = params.get("transaction_code"); // QR Banking
            }

            if (transactionCode == null) {
                return new PaymentResult(false, "Không tìm thấy mã giao dịch", null, null,
                        null, null, paymentMethod, "01", "Order not Found");
            }

            // Verify signature
            if (!verifyCallbackSignature(paymentMethod, params)) {
                return new PaymentResult(false, "Chữ ký không hợp lệ", null, null,
                        transactionCode, null, paymentMethod, "97", "Invalid Checksum");
            }

            // Find transaction
            Optional<Transaction> transactionOpt = transactionRepository.findByCodeAndDeletedAtIsNull(transactionCode);
            if (!transactionOpt.isPresent()) {
                return new PaymentResult(false, "Giao dịch không tồn tại", null, null,
                        transactionCode, null, paymentMethod, "01", "Order not Found");
            }

            Transaction transaction = transactionOpt.get();

            // Check if transaction is still pending
            if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
                return new PaymentResult(false, "Giao dịch đã được xử lý", null, null,
                        transactionCode, null, paymentMethod, "02", "Order already confirmed");
            }

            // Check amount (VNPay returns amount * 100)
            String vnpAmount = params.get("vnp_Amount");
            if (vnpAmount != null) {
                long expectedAmount = transaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
                if (Long.parseLong(vnpAmount) != expectedAmount) {
                    return new PaymentResult(false, "Số tiền không khớp", null, null,
                            transactionCode, null, paymentMethod, "04", "Invalid Amount");
                }
            }

            // Check payment status
            String responseCode = params.get("vnp_ResponseCode"); // VNPay
            String transactionStatus = params.get("vnp_TransactionStatus"); // VNPay

            if (responseCode == null) {
                responseCode = params.get("status"); // QR Banking
            }

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transactionService.completeCoinPurchase(transaction);
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
            }

            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            return new PaymentResult(true, "Xử lý IPN thành công", null, null,
                    transactionCode, transaction.getAmount(), paymentMethod, "00", "Confirm Success");

        } catch (Exception e) {
            return new PaymentResult(false, "Lỗi xử lý IPN", null, null,
                    null, null, paymentMethod, "99", "Unknown error");
        }
    }

    /**
     * Process payment return from gateway (Return URL)
     */
    public PaymentResult processReturnCallback(String paymentMethod, Map<String, String> params) {
        try {
            String transactionCode = params.get("vnp_TxnRef"); // VNPay
            if (transactionCode == null) {
                transactionCode = params.get("transaction_code"); // QR Banking
            }

            if (transactionCode == null) {
                return new PaymentResult(false, "Không tìm thấy mã giao dịch", null, null,
                        null, null, paymentMethod, "MISSING_TRANSACTION_CODE", null);
            }

            // Verify signature
            if (!verifyCallbackSignature(paymentMethod, params)) {
                return new PaymentResult(false, "Chữ ký không hợp lệ", null, null,
                        transactionCode, null, paymentMethod, "INVALID_SIGNATURE", null);
            }

            // Find transaction (read-only for display)
            Optional<Transaction> transactionOpt = transactionRepository.findByCodeAndDeletedAtIsNull(transactionCode);
            if (!transactionOpt.isPresent()) {
                return new PaymentResult(false, "Giao dịch không tồn tại", null, null,
                        transactionCode, null, paymentMethod, "TRANSACTION_NOT_FOUND", null);
            }

            Transaction transaction = transactionOpt.get();

            // Check payment status (display only, no DB update)
            String responseCode = params.get("vnp_ResponseCode"); // VNPay
            if (responseCode == null) {
                responseCode = params.get("status"); // QR Banking
            }

            boolean isSuccess = "00".equals(responseCode);

            return new PaymentResult(isSuccess,
                    isSuccess ? "Thanh toán thành công" : "Thanh toán thất bại",
                    null, null, transactionCode, transaction.getAmount(), paymentMethod, null, null);

        } catch (Exception e) {
            return new PaymentResult(false, "Lỗi xử lý return", null, null,
                    null, null, paymentMethod, "RETURN_PROCESSING_ERROR", e.getMessage());
        }
    }

    /**
     * Generate QR code data for bank transfer (VietQR URL)
     */
    private String generateQRCodeData(Transaction transaction) {
        // Build VietQR image URL:
        // https://img.vietqr.io/image/{bin}-{account}-qr_only.png?amount={amount}&addInfo={content}&accountName={name}
        // Ensure amount is integer VND
        BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;
        long amountVnd = amount.setScale(0, RoundingMode.HALF_UP).longValue();

        String bin = (qrBankBin != null && !qrBankBin.isEmpty()) ? qrBankBin : "970407";
        String account = qrAccountNumber;
        String accountName = qrAccountName;
        String addInfo = transaction.getCode();

        String base = "https://img.vietqr.io/image/" + bin + "-" + account + "-qr_only.png";
        StringBuilder url = new StringBuilder(base);
        url.append("?amount=").append(amountVnd);
        if (addInfo != null) {
            url.append("&addInfo=").append(java.net.URLEncoder.encode(addInfo, StandardCharsets.UTF_8));
        }
        if (accountName != null) {
            url.append("&accountName=").append(java.net.URLEncoder.encode(accountName, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    /**
     * Create VNPay URL with parameters and secure hash (query built per docs)
     */
    private String createVNPayUrl(Transaction transaction) {
        Map<String, String> vnpayParams = new TreeMap<>();

        vnpayParams.put("vnp_Version", "2.1.0");
        vnpayParams.put("vnp_Command", "pay");
        vnpayParams.put("vnp_TmnCode", vnpayTmnCode);
        vnpayParams.put("vnp_Amount",
                String.valueOf(transaction.getAmount().multiply(BigDecimal.valueOf(100)).longValue()));
        vnpayParams.put("vnp_CurrCode", "VND");
        // Do not send empty vnp_BankCode; let user choose on VNPay page
        vnpayParams.put("vnp_TxnRef", transaction.getCode());
        vnpayParams.put("vnp_OrderInfo", "Thanh toan goi xu " + transaction.getCode());
        vnpayParams.put("vnp_OrderType", "other");
        vnpayParams.put("vnp_Locale", "vn");
        vnpayParams.put("vnp_ReturnUrl", vnpayReturnUrl);
        vnpayParams.put("vnp_IpAddr", "127.0.0.1"); // TODO: inject real client IP

        String createDate = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpayParams.put("vnp_CreateDate", createDate);

        // Add expire date (15 minutes from now)
        LocalDateTime expireDate = LocalDateTime.now().plusMinutes(15);
        String expireDateStr = expireDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpayParams.put("vnp_ExpireDate", expireDateStr);

        // Build query string (sorted, url-encoded, joined by '&')
        String queryWithoutHash = buildQueryString(vnpayParams);

        // Create secure hash on queryWithoutHash
        String secureHash = hmacSHA512(vnpaySecretKey, queryWithoutHash);

        // Append hash type + hash to URL
        StringBuilder url = new StringBuilder(vnpayUrl);
        url.append("?").append(queryWithoutHash)
                .append("&vnp_SecureHashType=").append("HmacSHA512")
                .append("&vnp_SecureHash=").append(secureHash);

        return url.toString();
    }

    /**
     * Build query string from params per VNPay docs (sorted keys, url-encoded,
     * joined by '&')
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder query = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isEmpty())
                continue;
            if (!first)
                query.append('&');
            first = false;
            query.append(java.net.URLEncoder.encode(key, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(java.net.URLEncoder.encode(value, StandardCharsets.US_ASCII));
        }
        return query.toString();
    }

    /**
     * Create hash data for VNPay (kept for callback verification, uses same query
     * rules)
     */
    private String createVNPayHashData(Map<String, String> params) {
        // Filter out SecureHash/Type and build query in sorted order
        Map<String, String> filtered = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String k = e.getKey();
            if (k.startsWith("vnp_") && !"vnp_SecureHash".equals(k) && !"vnp_SecureHashType".equals(k)) {
                filtered.put(k, e.getValue());
            }
        }
        return buildQueryString(filtered);
    }

    /**
     * Verify callback signature
     */
    private boolean verifyCallbackSignature(String paymentMethod, Map<String, String> params) {
        if ("vnpay".equals(paymentMethod)) {
            return verifyVNPaySignature(params);
        } else if ("qr_banking".equals(paymentMethod)) {
            return verifyQRBankingSignature(params);
        }
        return false;
    }

    /**
     * Verify VNPay signature
     */
    private boolean verifyVNPaySignature(Map<String, String> params) {
        try {
            String receivedHash = params.get("vnp_SecureHash");
            if (receivedHash == null) {
                return false;
            }

            String hashData = createVNPayHashData(params);
            String calculatedHash = hmacSHA512(vnpaySecretKey, hashData);

            return calculatedHash.equals(receivedHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verify QR Banking signature (mock implementation)
     */
    private boolean verifyQRBankingSignature(Map<String, String> params) {
        // For QR Banking, we'll implement a simple validation
        // In production, this should verify bank's signature
        return params.containsKey("transaction_code") && params.containsKey("status");
    }

    /**
     * HMAC SHA512 implementation
     */
    private String hmacSHA512(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error creating HMAC SHA512", e);
        }
    }

    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}