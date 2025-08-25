package com.fpoly.shared_learning_materials.controller;

import com.fpoly.shared_learning_materials.dto.PaymentResult;
import com.fpoly.shared_learning_materials.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * VNPay IPN callback (GET as per VNPAY docs)
     */
    @GetMapping("/vnpay/callback")
    @ResponseBody
    public String vnpayIPNCallback(@RequestParam Map<String, String> params) {
        // Log all received parameters
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentController.class);
        log.info("VNPay IPN callback received with parameters: {}", params);

        PaymentResult result = paymentService.processIPNCallback("vnpay", params);

        // Log the result
        log.info("VNPay IPN callback result: {}", result);

        // Return JSON response as required by VNPAY
        String response = String.format("{\"RspCode\":\"%s\",\"Message\":\"%s\"}",
                result.getErrorCode(), result.getErrorMessage());
        log.info("VNPay IPN callback response: {}", response);
        return response;
    }

    /**
     * VNPay return URL
     */
    @GetMapping("/vnpay/return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        // Log all received parameters
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentController.class);
        log.info("VNPay return URL received with parameters: {}", params);

        PaymentResult result = paymentService.processReturnCallback("vnpay", params);

        // Log the result
        log.info("VNPay return URL result: {}", result);

        if (result.isSuccess()) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("transactionCode", result.getTransactionCode());
            model.addAttribute("amount", result.getAmount());
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại: " + result.getMessage());
            model.addAttribute("errorCode", result.getErrorCode());
        }

        return "payment/return";
    }

    /**
     * QR Banking callback
     */
    @PostMapping("/qr/callback")
    public String qrCallback(@RequestParam Map<String, String> params, Model model) {
        PaymentResult result = paymentService.processIPNCallback("qr_banking", params);

        if (result.isSuccess()) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("transactionCode", result.getTransactionCode());
            model.addAttribute("amount", result.getAmount());
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại: " + result.getMessage());
            model.addAttribute("errorCode", result.getErrorCode());
        }

        return "payment/callback";
    }

    /**
     * Payment return page
     */
    @GetMapping("/return")
    public String paymentReturn(@RequestParam Map<String, String> params, Model model) {
        String paymentMethod = params.get("payment_method");
        if (paymentMethod == null) {
            paymentMethod = "unknown";
        }

        PaymentResult result = paymentService.processReturnCallback(paymentMethod, params);

        if (result.isSuccess()) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("transactionCode", result.getTransactionCode());
            model.addAttribute("amount", result.getAmount());
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message", "Thanh toán thất bại: " + result.getMessage());
            model.addAttribute("errorCode", result.getErrorCode());
        }

        return "payment/return";
    }

    /**
     * Test VNPay configuration
     */
    @GetMapping("/vnpay/test")
    @ResponseBody
    public String testVNPayConfig() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentController.class);

        StringBuilder result = new StringBuilder();
        result.append("VNPay Configuration Test:\n");
        result.append("TMN Code: ").append(paymentService.getVnpayTmnCode()).append("\n");
        result.append("Secret Key: ").append(paymentService.getVnpaySecretKey().substring(0, 10)).append("...\n");
        result.append("URL: ").append(paymentService.getVnpayUrl()).append("\n");
        result.append("Return URL: ").append(paymentService.getVnpayReturnUrl()).append("\n");
        result.append("Notify URL: ").append(paymentService.getVnpayNotifyUrl()).append("\n");

        log.info("VNPay configuration test: {}", result.toString());
        return result.toString();
    }
}