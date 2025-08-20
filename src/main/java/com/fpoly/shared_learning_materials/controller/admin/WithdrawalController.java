package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Transaction;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.WithdrawalService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Controller
@RequestMapping("/admin/withdrawals")
public class WithdrawalController extends BaseAdminController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private UserService userService;

     public WithdrawalController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }

    /**
     * Hiển thị danh sách withdrawals với phân trang và tìm kiếm
     */
    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        // Tạo Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy dữ liệu với search và filter
        Page<Transaction> withdrawalPage;
        Transaction.TransactionStatus statusEnum = null;

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            try {
                statusEnum = Transaction.TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusEnum = null;
            }
        }

        // Parse date parameters
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        try {
            if (startDate != null && !startDate.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            }
        } catch (Exception e) {
            // Invalid date format, ignore
        }

        withdrawalPage = withdrawalService.searchAndFilterWithdrawals(keyword, statusEnum, startDateTime, endDateTime,
                pageable);

        // Thêm dữ liệu vào model
        model.addAttribute("withdrawals", withdrawalPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", withdrawalPage.getTotalPages());
        model.addAttribute("totalElements", withdrawalPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // Thêm thông tin phân trang
        model.addAttribute("hasPrevious", withdrawalPage.hasPrevious());
        model.addAttribute("hasNext", withdrawalPage.hasNext());
        model.addAttribute("isFirst", withdrawalPage.isFirst());
        model.addAttribute("isLast", withdrawalPage.isLast());

        // Thêm thống kê withdrawals
        model.addAttribute("totalWithdrawalsThisMonth", withdrawalService.getTotalWithdrawalsThisMonth());
        model.addAttribute("totalWithdrawalAmountThisMonth", withdrawalService.getTotalWithdrawalAmountThisMonth());
        model.addAttribute("pendingWithdrawals", withdrawalService.getPendingWithdrawals());
        model.addAttribute("withdrawalSuccessRate", withdrawalService.getWithdrawalSuccessRate());
        model.addAttribute("overdueWithdrawals", withdrawalService.getOverdueWithdrawals());

        // Thêm enum options
        model.addAttribute("statusOptions", Transaction.TransactionStatus.values());

        return "admin/withdrawals/index";
    }

    /**
     * Hiển thị form tạo withdrawal mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        Transaction transaction = new Transaction();
        transaction.setType(Transaction.TransactionType.WITHDRAWAL);

        model.addAttribute("transaction", transaction);
        model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
        model.addAttribute("users", userService.getAllUsers());

        // Add payment methods
        Map<String, String> paymentMethods = new LinkedHashMap<>();
        paymentMethods.put("BANK_TRANSFER", "Chuyển khoản ngân hàng");
        paymentMethods.put("E_WALLET", "Ví điện tử");
        paymentMethods.put("CREDIT_CARD", "Thẻ tín dụng/ghi nợ");
        paymentMethods.put("CASH", "Tiền mặt");
        model.addAttribute("paymentMethods", paymentMethods);

        return "admin/withdrawals/create";
    }

    /**
     * Xử lý tạo withdrawal mới
     */
    @PostMapping("/create")
    public String createWithdrawal(@ModelAttribute("transaction") Transaction transaction,
            RedirectAttributes redirectAttributes, Model model) {
        try {
            withdrawalService.createWithdrawal(transaction);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo withdrawal thành công: " + transaction.getCode());
            return "redirect:/admin/withdrawals";
        } catch (Exception e) {
            // Re-populate form data on error
            model.addAttribute("transaction", transaction);
            model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
            model.addAttribute("users", userService.getAllUsers());

            // Add payment methods
            Map<String, String> paymentMethods = new LinkedHashMap<>();
            paymentMethods.put("BANK_TRANSFER", "Chuyển khoản ngân hàng");
            paymentMethods.put("E_WALLET", "Ví điện tử");
            paymentMethods.put("CREDIT_CARD", "Thẻ tín dụng/ghi nợ");
            paymentMethods.put("CASH", "Tiền mặt");
            model.addAttribute("paymentMethods", paymentMethods);

            model.addAttribute("errorMessage", "Lỗi khi tạo withdrawal: " + e.getMessage());
            return "admin/withdrawals/create";
        }
    }

    /**
     * Hiển thị chi tiết withdrawal
     */
    @GetMapping("/{id}/detail")
    public String showDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> withdrawalOpt = withdrawalService.getWithdrawalById(id);
        if (withdrawalOpt.isPresent()) {
            model.addAttribute("withdrawal", withdrawalOpt.get());
            return "admin/withdrawals/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy withdrawal");
            return "redirect:/admin/withdrawals";
        }
    }

    /**
     * Hiển thị form chỉnh sửa withdrawal
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> withdrawalOpt = withdrawalService.getWithdrawalById(id);
        if (withdrawalOpt.isPresent()) {
            Transaction withdrawal = withdrawalOpt.get();

            // Kiểm tra xem withdrawal có thể chỉnh sửa không
            if (withdrawal.getStatus() == Transaction.TransactionStatus.COMPLETED) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Withdrawal đã hoàn thành, một số trường sẽ bị hạn chế chỉnh sửa");
            }

            model.addAttribute("withdrawal", withdrawal);
            model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("isCompleted", withdrawal.getStatus() == Transaction.TransactionStatus.COMPLETED);

            // Add payment methods
            Map<String, String> paymentMethods = new LinkedHashMap<>();
            paymentMethods.put("BANK_TRANSFER", "Chuyển khoản ngân hàng");
            paymentMethods.put("E_WALLET", "Ví điện tử");
            paymentMethods.put("CREDIT_CARD", "Thẻ tín dụng/ghi nợ");
            paymentMethods.put("CASH", "Tiền mặt");
            model.addAttribute("paymentMethods", paymentMethods);

            return "admin/withdrawals/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy withdrawal");
            return "redirect:/admin/withdrawals";
        }
    }

    /**
     * Xử lý cập nhật withdrawal
     */
    @PostMapping("/{id}/edit")
    public String updateWithdrawal(@PathVariable Long id,
            @ModelAttribute("withdrawal") Transaction withdrawal,
            RedirectAttributes redirectAttributes, Model model) {
        try {
            withdrawal.setId(id);
            withdrawalService.updateWithdrawal(withdrawal);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật withdrawal thành công: " + withdrawal.getCode());
            return "redirect:/admin/withdrawals";
        } catch (Exception e) {
            // Re-populate form data on error
            model.addAttribute("withdrawal", withdrawal);
            model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
            model.addAttribute("users", userService.getAllUsers());
            model.addAttribute("isCompleted", withdrawal.getStatus() == Transaction.TransactionStatus.COMPLETED);

            // Add payment methods
            Map<String, String> paymentMethods = new LinkedHashMap<>();
            paymentMethods.put("BANK_TRANSFER", "Chuyển khoản ngân hàng");
            paymentMethods.put("E_WALLET", "Ví điện tử");
            paymentMethods.put("CREDIT_CARD", "Thẻ tín dụng/ghi nợ");
            paymentMethods.put("CASH", "Tiền mặt");
            model.addAttribute("paymentMethods", paymentMethods);

            model.addAttribute("errorMessage", "Lỗi khi cập nhật withdrawal: " + e.getMessage());
            return "admin/withdrawals/edit";
        }
    }

    /**
     * Hiển thị trang xác nhận xóa withdrawal
     */
    @GetMapping("/{id}/delete")
    public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> withdrawalOpt = withdrawalService.getWithdrawalById(id);
        if (withdrawalOpt.isPresent()) {
            Transaction withdrawal = withdrawalOpt.get();
            model.addAttribute("withdrawal", withdrawal);
            model.addAttribute("isCompleted", withdrawal.getStatus() == Transaction.TransactionStatus.COMPLETED);
            return "admin/withdrawals/delete";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy withdrawal");
            return "redirect:/admin/withdrawals";
        }
    }

    /**
     * Xóa withdrawal (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteWithdrawal(@PathVariable Long id,
            @RequestParam(required = false) String deleteReason,
            @RequestParam(required = false) String deleteNote,
            @RequestParam(defaultValue = "false") boolean notifyUser,
            @RequestParam(defaultValue = "true") boolean backupData,
            @RequestParam(defaultValue = "false") boolean updateReports,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Transaction> withdrawalOpt = withdrawalService.getWithdrawalById(id);
            if (withdrawalOpt.isPresent()) {
                Transaction withdrawal = withdrawalOpt.get();

                // Thêm ghi chú về lý do xóa
                if (deleteReason != null && !deleteReason.isEmpty()) {
                    String note = "Xóa withdrawal - Lý do: " + deleteReason;
                    if (deleteNote != null && !deleteNote.isEmpty()) {
                        note += " - Ghi chú: " + deleteNote;
                    }
                    withdrawal.setNotes(note);
                    withdrawalService.updateWithdrawal(withdrawal);
                }

                withdrawalService.deleteWithdrawal(id);

                // Xử lý các tùy chọn bổ sung
                if (notifyUser) {
                    // TODO: Gửi email thông báo cho user
                    System.out.println("Gửi email thông báo cho user: " + withdrawal.getUser().getEmail());
                }

                if (backupData) {
                    // TODO: Sao lưu dữ liệu
                    System.out.println("Sao lưu dữ liệu withdrawal: " + withdrawal.getCode());
                }

                if (updateReports) {
                    // TODO: Cập nhật báo cáo tài chính
                    System.out.println("Cập nhật báo cáo tài chính");
                }

                redirectAttributes.addFlashAttribute("successMessage",
                        "Xóa withdrawal thành công: " + withdrawal.getCode());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy withdrawal");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi xóa withdrawal: " + e.getMessage());
        }
        return "redirect:/admin/withdrawals";
    }

    /**
     * Thêm ghi chú cho withdrawal
     */
    @PostMapping("/{id}/add-note")
    public String addNote(@PathVariable Long id,
            @RequestParam String note,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Transaction> withdrawalOpt = withdrawalService.getWithdrawalById(id);
            if (withdrawalOpt.isPresent()) {
                Transaction withdrawal = withdrawalOpt.get();
                String currentNotes = withdrawal.getNotes();
                String timestamp = LocalDateTime.now().toString();
                String newNote = currentNotes != null ? currentNotes + "\n[" + timestamp + "] " + note
                        : "[" + timestamp + "] " + note;
                withdrawal.setNotes(newNote);
                withdrawalService.updateWithdrawal(withdrawal);

                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm ghi chú thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy withdrawal");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm ghi chú: " + e.getMessage());
        }
        return "redirect:/admin/withdrawals/" + id + "/detail";
    }

    /**
     * Xử lý bulk actions
     */
    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam String action,
            @RequestParam(value = "withdrawalIds", required = false) List<Long> withdrawalIds,
            RedirectAttributes redirectAttributes) {

        if (withdrawalIds == null || withdrawalIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một withdrawal");
            return "redirect:/admin/withdrawals";
        }

        try {
            switch (action) {
                case "delete":
                    withdrawalService.deleteWithdrawals(withdrawalIds);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã xóa " + withdrawalIds.size() + " withdrawal");
                    break;
                case "status_completed":
                    withdrawalService.updateWithdrawalsStatus(withdrawalIds,
                            Transaction.TransactionStatus.COMPLETED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + withdrawalIds.size() + " withdrawal thành hoàn thành");
                    break;
                case "status_failed":
                    withdrawalService.updateWithdrawalsStatus(withdrawalIds,
                            Transaction.TransactionStatus.FAILED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + withdrawalIds.size() + " withdrawal thành thất bại");
                    break;
                case "status_cancelled":
                    withdrawalService.updateWithdrawalsStatus(withdrawalIds,
                            Transaction.TransactionStatus.CANCELLED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + withdrawalIds.size() + " withdrawal thành hủy");
                    break;
                case "status_pending":
                    withdrawalService.updateWithdrawalsStatus(withdrawalIds,
                            Transaction.TransactionStatus.PENDING);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + withdrawalIds.size() + " withdrawal thành chờ xử lý");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("errorMessage", "Hành động không hợp lệ");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi thực hiện hành động: " + e.getMessage());
        }

        return "redirect:/admin/withdrawals";
    }

    /**
     * Generate withdrawal code tự động
     */
    @PostMapping("/generate-code")
    @ResponseBody
    public Map<String, Object> generateCode() {
        Map<String, Object> response = new HashMap<>();
        try {
            String code = withdrawalService.generateWithdrawalCode();
            response.put("success", true);
            response.put("code", code);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}