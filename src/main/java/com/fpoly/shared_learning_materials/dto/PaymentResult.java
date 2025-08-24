package com.fpoly.shared_learning_materials.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String message;
    private String paymentUrl;
    private String qrCode;
    private String transactionCode;
    private BigDecimal amount;
    private String paymentMethod;
    private String errorCode;
    private String errorMessage;
}