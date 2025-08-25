package com.fpoly.shared_learning_materials.dto;

import com.fpoly.shared_learning_materials.domain.Transaction;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for withdrawal form validation
 */
public class WithdrawalFormDTO {

    private Long id;

    @NotBlank(message = "Mã withdrawal không được để trống")
    @Pattern(regexp = "^WD[A-Z0-9]{6,20}$", message = "Mã withdrawal không đúng định dạng (WD + 6-20 ký tự)")
    private String code;

    @NotNull(message = "Người dùng không được để trống")
    private Long userId;

    @NotNull(message = "Số xu không được để trống")
    @DecimalMin(value = "50", message = "Số xu rút tối thiểu là 50 xu")
    @DecimalMax(value = "50000", message = "Số xu rút tối đa là 50,000 xu")
    @Digits(integer = 10, fraction = 0, message = "Số xu phải là số nguyên")
    private BigDecimal amount;

    @NotNull(message = "Trạng thái không được để trống")
    private Transaction.TransactionStatus status;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;

    @NotNull(message = "Ngày tạo không được để trống")
    @PastOrPresent(message = "Ngày tạo không được trong tương lai")
    private LocalDateTime createdAt;

    // Constructors
    public WithdrawalFormDTO() {
    }

    public WithdrawalFormDTO(Transaction transaction) {
        if (transaction != null) {
            this.id = transaction.getId();
            this.code = transaction.getCode();
            this.userId = transaction.getUser() != null ? transaction.getUser().getId() : null;
            this.amount = transaction.getAmount();
            this.status = transaction.getStatus();
            this.paymentMethod = transaction.getPaymentMethod();
            this.notes = transaction.getNotes();
            this.createdAt = transaction.getCreatedAt();
        }
    }

    // Convert to Transaction entity
    public Transaction toTransaction() {
        Transaction transaction = new Transaction();
        transaction.setId(this.id);
        transaction.setCode(this.code);
        transaction.setAmount(this.amount);
        transaction.setStatus(this.status);
        transaction.setPaymentMethod(this.paymentMethod);
        transaction.setNotes(this.notes);
        transaction.setCreatedAt(this.createdAt);
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);
        return transaction;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Transaction.TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "WithdrawalFormDTO{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", userId=" + userId +
                ", amount=" + amount +
                ", status=" + status +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}