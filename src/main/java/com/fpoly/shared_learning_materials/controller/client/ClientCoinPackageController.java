package com.fpoly.shared_learning_materials.controller.client;

import com.fpoly.shared_learning_materials.config.CustomUserDetailsService;
import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.domain.TransactionDetail;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.PaymentRequest;
import com.fpoly.shared_learning_materials.dto.PaymentResult;
import com.fpoly.shared_learning_materials.repository.TransactionDetailRepository;
import com.fpoly.shared_learning_materials.service.CoinPackageService;
import com.fpoly.shared_learning_materials.service.PaymentService;
import com.fpoly.shared_learning_materials.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;
import java.io.UnsupportedEncodingException;

@Controller
@RequestMapping("/coin-packages")
@PreAuthorize("isAuthenticated()")
public class ClientCoinPackageController {

    private static final Logger log = LoggerFactory.getLogger(ClientCoinPackageController.class);

    @Autowired
    private CoinPackageService coinPackageService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/purchase")
    public String purchaseFromForm(@RequestParam("coinPackageId") Long coinPackageId,
            @RequestParam("paymentMethod") String paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {
        return purchase(coinPackageId, paymentMethod, userDetails, redirectAttributes, model);
    }

    @PostMapping("/{id}/purchase")
    public String purchase(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {
        return purchase(id, "VNPAY", userDetails, redirectAttributes, model);
    }

    private String purchase(Long coinPackageId, String paymentMethod,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {
        log.info("Starting purchase process: coinPackageId={}, paymentMethod={}", coinPackageId, paymentMethod);

        // Validate authentication
        if (userDetails == null) {
            log.warn("User not authenticated");
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện giao dịch.");
            return "redirect:/auth/login";
        }

        // Validate coin package
        log.info("Validating coin package: {}", coinPackageId);
        Optional<CoinPackage> coinPackageOpt = coinPackageService.getPackageById(coinPackageId);
        if (!coinPackageOpt.isPresent()) {
            log.warn("Coin package not found: {}", coinPackageId);
            redirectAttributes.addFlashAttribute("error", "Gói xu không tồn tại hoặc đã bị xóa.");
            return "redirect:/account/recharge";
        }
        CoinPackage coinPackage = coinPackageOpt.get();
        log.info("Coin package found: {} - {}", coinPackage.getName(), coinPackage.getSalePrice());

        // Create transaction (PENDING)
        Transaction transaction = new Transaction();
        transaction.setType(Transaction.TransactionType.PURCHASE);
        transaction.setAmount(coinPackage.getSalePrice());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setPaymentMethod(paymentMethod);

        User user = new User();
        user.setId(getAuthenticatedUserId(userDetails));
        transaction.setUser(user);

        try {
            log.info("Creating transaction for user: {}", getAuthenticatedUserId(userDetails));
            transaction = transactionService.createTransaction(transaction);
            log.info("Transaction created: {}", transaction.getCode());
        } catch (IllegalArgumentException ex) {
            log.error("Failed to create transaction: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account/recharge";
        }

        // Create transaction detail referencing coin package with enhanced fields
        TransactionDetail detail = new TransactionDetail();
        detail.setTransaction(transaction);
        detail.setDetailType("coin_package");
        detail.setReferenceId(coinPackage.getId());
        detail.setCoinPackage(coinPackage);
        detail.setQuantity(1);
        detail.setUnitPrice(coinPackage.getSalePrice());
        detail.setAmount(coinPackage.getSalePrice().multiply(BigDecimal.valueOf(detail.getQuantity())));

        int baseCoins = coinPackage.getCoinAmount() * detail.getQuantity();
        int bonusCoins = coinPackage.getBonusCoins() != null ? coinPackage.getBonusCoins() * detail.getQuantity() : 0;
        detail.setBonusCoins(bonusCoins);
        detail.setCoinsReceived(baseCoins + bonusCoins);

        transactionDetailRepository.save(detail);

        // Initiate payment according to method
        log.info("Initiating payment: method={}, transactionCode={}", paymentMethod, transaction.getCode());
        PaymentRequest req = new PaymentRequest();
        req.setTransactionCode(transaction.getCode());
        req.setAmount(transaction.getAmount());
        req.setPaymentMethod("QR".equalsIgnoreCase(paymentMethod) ? "qr_banking" : "vnpay");

        if ("QR".equalsIgnoreCase(paymentMethod)) {
            log.info("Processing QR payment");
            PaymentResult result = paymentService.processQRPayment(req);
            if (!result.isSuccess()) {
                log.error("QR payment failed: {}", result.getMessage());
                redirectAttributes.addFlashAttribute("error", result.getMessage());
                return "redirect:/account/recharge?transaction=" + transaction.getCode();
            }
            log.info("QR payment successful, redirecting to recharge page");
            log.info("QR Code from result: {}", result.getQrCode());
            redirectAttributes.addFlashAttribute("qrData", result.getQrCode());
            redirectAttributes.addFlashAttribute("transactionCode", transaction.getCode());
            try {
                return "redirect:/account/recharge?transaction=" + transaction.getCode() + "&qr=1&qrdata="
                        + java.net.URLEncoder.encode(result.getQrCode(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding QR data", e);
                return "redirect:/account/recharge?transaction=" + transaction.getCode() + "&qr=1";
            }
        } else {
            log.info("Processing VNPay payment");
            PaymentResult result = paymentService.processVNPayPayment(req);
            if (!result.isSuccess() || result.getPaymentUrl() == null) {
                log.error("VNPay payment failed: {}", result.getMessage());
                redirectAttributes.addFlashAttribute("error", result.getMessage());
                return "redirect:/account/recharge?transaction=" + transaction.getCode();
            }
            log.info("VNPay payment successful, redirecting to: {}", result.getPaymentUrl());
            return "redirect:" + result.getPaymentUrl();
        }
    }

    private Long getAuthenticatedUserId(UserDetails userDetails) {
        if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) userDetails).getUserId();
        }
        throw new IllegalStateException("Không xác định được người dùng hiện tại");
    }
}