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

            log.info("VNPay callback - Transaction: {}, ResponseCode: {}, TransactionStatus: {}",
                    transactionCode, responseCode, transactionStatus);

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transactionService.completeCoinPurchase(transaction);
                log.info("Transaction {} completed successfully", transactionCode);
            } else {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                log.warn("Transaction {} failed - ResponseCode: {}, TransactionStatus: {}",
                        transactionCode, responseCode, transactionStatus);
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
            String message = getVNPayErrorMessage(responseCode);

            return new PaymentResult(isSuccess, message,
                    null, null, transactionCode, transaction.getAmount(), paymentMethod, responseCode, null);

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
        vnpayParams.put("vnp_IpAddr", getClientIpAddress());

        // Use Vietnam timezone for VNPay
        java.time.ZoneId vietnamZone = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now(vietnamZone);
        String createDate = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpayParams.put("vnp_CreateDate", createDate);

        // Add expire date (15 minutes from now) in Vietnam timezone
        LocalDateTime expireDate = now.plusMinutes(15);
        String expireDateStr = expireDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        vnpayParams.put("vnp_ExpireDate", expireDateStr);

        // Log parameters for debugging
        log.info("VNPay parameters for transaction {}: {}", transaction.getCode(), vnpayParams);

        // Build hash data first (sorted, url-encoded, joined by '&')
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : vnpayParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isEmpty())
                continue;

            // Build hash data
            if (!first) {
                hashData.append('&');
            }
            hashData.append(key).append('=').append(java.net.URLEncoder.encode(value, StandardCharsets.US_ASCII));

            // Build query
            if (!first) {
                query.append('&');
            }
            query.append(java.net.URLEncoder.encode(key, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(java.net.URLEncoder.encode(value, StandardCharsets.US_ASCII));

            first = false;
        }

        // Log hash data for debugging
        log.info("VNPay hash data for transaction {}: {}", transaction.getCode(), hashData.toString());

        // Create secure hash on hashData
        String secureHash = hmacSHA512(vnpaySecretKey, hashData.toString());
        log.info("VNPay secure hash for transaction {}: {}", transaction.getCode(), secureHash);

        // Append hash to query
        query.append("&vnp_SecureHash=").append(secureHash);

        // Build final URL
        StringBuilder url = new StringBuilder(vnpayUrl);
        url.append("?").append(query.toString());

        String finalUrl = url.toString();
        log.info("VNPay final URL for transaction {}: {}", transaction.getCode(), finalUrl);

        return finalUrl;
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

            // Build hash data from params (excluding SecureHash and SecureHashType)
            Map<String, String> filteredParams = new TreeMap<>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("vnp_") && !"vnp_SecureHash".equals(key) && !"vnp_SecureHashType".equals(key)) {
                    filteredParams.put(key, entry.getValue());
                }
            }

            // Build hash data string (sorted, url-encoded, joined by '&')
            StringBuilder hashData = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : filteredParams.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (value == null || value.isEmpty())
                    continue;

                if (!first) {
                    hashData.append('&');
                }
                hashData.append(key).append('=').append(java.net.URLEncoder.encode(value, StandardCharsets.US_ASCII));
                first = false;
            }

            String calculatedHash = hmacSHA512(vnpaySecretKey, hashData.toString());
            return calculatedHash.equals(receivedHash);
        } catch (Exception e) {
            log.error("Error verifying VNPay signature: {}", e.getMessage(), e);
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

    /**
     * Get client IP address (fallback for Azure deployment)
     */
    private String getClientIpAddress() {
        try {
            // Try to get real client IP from Azure headers
            org.springframework.web.context.request.RequestAttributes requestAttributes = org.springframework.web.context.request.RequestContextHolder
                    .getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes)
                        .getRequest();

                // Check Azure-specific headers
                String clientIp = request.getHeader("X-Forwarded-For");
                if (clientIp == null || clientIp.isEmpty()) {
                    clientIp = request.getHeader("X-Real-IP");
                }
                if (clientIp == null || clientIp.isEmpty()) {
                    clientIp = request.getHeader("X-Azure-ClientIP");
                }
                if (clientIp == null || clientIp.isEmpty()) {
                    clientIp = request.getRemoteAddr();
                }

                // Handle comma-separated IPs (take first one)
                if (clientIp != null && clientIp.contains(",")) {
                    clientIp = clientIp.split(",")[0].trim();
                }

                return clientIp != null && !clientIp.isEmpty() ? clientIp : "127.0.0.1";
            }
        } catch (Exception e) {
            log.warn("Could not get client IP address: {}", e.getMessage());
        }
        return "127.0.0.1";
    }

    /**
     * Get VNPay error message by response code
     */
    private String getVNPayErrorMessage(String responseCode) {
        if (responseCode == null) {
            return "Không có mã phản hồi từ VNPay";
        }

        switch (responseCode) {
            case "00":
                return "Thanh toán thành công";
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "15":
                return "Giao dịch không thành công do: Giao dịch không hợp lệ hoặc thông tin giao dịch bị sai lệch";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65":
                return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99":
                return "Các lỗi khác (lỗi còn lại, không có trong danh sách mã lỗi đã liệt kê)";
            default:
                return "Lỗi không xác định từ VNPay (Mã lỗi: " + responseCode + ")";
        }
    }

    // Getter methods for VNPay configuration
    public String getVnpayTmnCode() {
        return vnpayTmnCode;
    }

    public String getVnpaySecretKey() {
        return vnpaySecretKey;
    }

    public String getVnpayUrl() {
        return vnpayUrl;
    }

    public String getVnpayReturnUrl() {
        return vnpayReturnUrl;
    }

    public String getVnpayNotifyUrl() {
        return vnpayNotifyUrl;
    }
}