package com.fpoly.shared_learning_materials.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String transactionCode;
    private BigDecimal amount;
    private String currency = "VND";
    private String paymentMethod; // "vnpay", "qr_banking"
    private String returnUrl;
    private String cancelUrl;
    private String notifyUrl;
    private String orderInfo;
    private String locale = "vn";
    private String ipAddr;
}