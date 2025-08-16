package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Transaction;
// import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.service.TransactionService;
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

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    /**
     * Hiển thị danh sách giao dịch với phân trang và tìm kiếm
     */
    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String amountRange,
            Model model) {

        // Tạo Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy dữ liệu với search và filter
        Page<Transaction> transactionPage;
        Transaction.TransactionStatus statusEnum = null;
        Transaction.TransactionType typeEnum = null;

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            try {
                statusEnum = Transaction.TransactionStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusEnum = null;
            }
        }

        if (type != null && !type.isEmpty() && !type.equals("all")) {
            try {
                typeEnum = Transaction.TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                typeEnum = null;
            }
        }

        transactionPage = transactionService.searchAndFilterTransactions(keyword, statusEnum, typeEnum, pageable);

        // Thêm dữ liệu vào model
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("totalElements", transactionPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("amountRange", amountRange);

        // Thêm thông tin phân trang
        model.addAttribute("hasPrevious", transactionPage.hasPrevious());
        model.addAttribute("hasNext", transactionPage.hasNext());
        model.addAttribute("isFirst", transactionPage.isFirst());
        model.addAttribute("isLast", transactionPage.isLast());

        // Thêm thống kê
        model.addAttribute("totalTransactions", transactionService.getTotalTransactions());
        model.addAttribute("totalRevenue", transactionService.getTotalRevenue());
        model.addAttribute("successRate", transactionService.getSuccessRate());
        model.addAttribute("pendingTransactions", transactionService.getPendingTransactions());

        // Thêm enum options
        model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
        model.addAttribute("typeOptions", Transaction.TransactionType.values());

        return "admin/transactions/index";
    }

    /**
     * Hiển thị form tạo giao dịch mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
        model.addAttribute("typeOptions", Transaction.TransactionType.values());
        model.addAttribute("users", userService.getAllUsers());
        return "admin/transactions/create";
    }

    /**
     * Xử lý tạo giao dịch mới
     */
    @PostMapping("/create")
    public String createTransaction(@ModelAttribute Transaction transaction,
            RedirectAttributes redirectAttributes) {
        try {
            transactionService.createTransaction(transaction);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo giao dịch thành công: " + transaction.getCode());
            return "redirect:/admin/transactions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi tạo giao dịch: " + e.getMessage());
            return "redirect:/admin/transactions/create";
        }
    }

    /**
     * Hiển thị chi tiết giao dịch
     */
    @GetMapping("/{id}/detail")
    public String showDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isPresent()) {
            model.addAttribute("transaction", transactionOpt.get());
            return "admin/transactions/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch");
            return "redirect:/admin/transactions";
        }
    }

    /**
     * Hiển thị form chỉnh sửa giao dịch
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isPresent()) {
            model.addAttribute("transaction", transactionOpt.get());
            model.addAttribute("statusOptions", Transaction.TransactionStatus.values());
            model.addAttribute("typeOptions", Transaction.TransactionType.values());
            model.addAttribute("users", userService.getAllUsers());
            return "admin/transactions/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch");
            return "redirect:/admin/transactions";
        }
    }

    /**
     * Xử lý cập nhật giao dịch
     */
    @PostMapping("/{id}/edit")
    public String updateTransaction(@PathVariable Long id,
            @ModelAttribute Transaction transaction,
            RedirectAttributes redirectAttributes) {
        try {
            transaction.setId(id);
            transactionService.updateTransaction(transaction);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật giao dịch thành công: " + transaction.getCode());
            return "redirect:/admin/transactions";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi cập nhật giao dịch: " + e.getMessage());
            return "redirect:/admin/transactions/" + id + "/edit";
        }
    }

    /**
     * Hiển thị trang xác nhận xóa giao dịch
     */
    @GetMapping("/{id}/delete")
    public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
        if (transactionOpt.isPresent()) {
            model.addAttribute("transaction", transactionOpt.get());
            return "admin/transactions/delete";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch");
            return "redirect:/admin/transactions";
        }
    }

    /**
     * Xóa giao dịch (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deleteTransaction(@PathVariable Long id,
            @RequestParam(required = false) String deleteReason,
            @RequestParam(required = false) String deleteNote,
            @RequestParam(defaultValue = "false") boolean notifyUser,
            @RequestParam(defaultValue = "true") boolean backupData,
            @RequestParam(defaultValue = "false") boolean updateReports,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();

                // Thêm ghi chú về lý do xóa
                if (deleteReason != null && !deleteReason.isEmpty()) {
                    String note = "Xóa giao dịch - Lý do: " + deleteReason;
                    if (deleteNote != null && !deleteNote.isEmpty()) {
                        note += " - Ghi chú: " + deleteNote;
                    }
                    transaction.setNotes(note);
                }

                transactionService.deleteTransaction(id);

                // Xử lý các tùy chọn bổ sung
                if (notifyUser) {
                    // TODO: Gửi email thông báo cho user
                    System.out.println("Gửi email thông báo cho user: " + transaction.getUser().getEmail());
                }

                if (backupData) {
                    // TODO: Sao lưu dữ liệu
                    System.out.println("Sao lưu dữ liệu giao dịch: " + transaction.getCode());
                }

                if (updateReports) {
                    // TODO: Cập nhật báo cáo tài chính
                    System.out.println("Cập nhật báo cáo tài chính");
                }

                redirectAttributes.addFlashAttribute("successMessage",
                        "Xóa giao dịch thành công: " + transaction.getCode());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi xóa giao dịch: " + e.getMessage());
        }
        return "redirect:/admin/transactions";
    }

    /**
     * Thêm ghi chú cho giao dịch
     */
    @PostMapping("/{id}/add-note")
    public String addNote(@PathVariable Long id,
            @RequestParam String note,
            RedirectAttributes redirectAttributes) {
        try {
            Optional<Transaction> transactionOpt = transactionService.getTransactionById(id);
            if (transactionOpt.isPresent()) {
                Transaction transaction = transactionOpt.get();
                String currentNotes = transaction.getNotes();
                String newNote = currentNotes != null ? currentNotes + "\n" + note : note;
                transaction.setNotes(newNote);
                transactionService.updateTransaction(transaction);

                redirectAttributes.addFlashAttribute("successMessage", "Đã thêm ghi chú thành công");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy giao dịch");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm ghi chú: " + e.getMessage());
        }
        return "redirect:/admin/transactions/" + id + "/detail";
    }

    /**
     * Xử lý bulk actions
     */
    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam String action,
            @RequestParam(value = "transactionIds", required = false) List<Long> transactionIds,
            RedirectAttributes redirectAttributes) {

        if (transactionIds == null || transactionIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn ít nhất một giao dịch");
            return "redirect:/admin/transactions";
        }

        try {
            switch (action) {
                case "delete":
                    transactionService.deleteTransactions(transactionIds);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã xóa " + transactionIds.size() + " giao dịch");
                    break;
                case "status_completed":
                    transactionService.updateTransactionsStatus(transactionIds,
                            Transaction.TransactionStatus.COMPLETED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + transactionIds.size() + " giao dịch thành hoàn thành");
                    break;
                case "status_failed":
                    transactionService.updateTransactionsStatus(transactionIds,
                            Transaction.TransactionStatus.FAILED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + transactionIds.size() + " giao dịch thành thất bại");
                    break;
                case "status_cancelled":
                    transactionService.updateTransactionsStatus(transactionIds,
                            Transaction.TransactionStatus.CANCELLED);
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Đã cập nhật " + transactionIds.size() + " giao dịch thành hủy");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("errorMessage", "Hành động không hợp lệ");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi thực hiện hành động: " + e.getMessage());
        }

        return "redirect:/admin/transactions";
    }

    /**
     * Generate transaction code tự động
     */
    @PostMapping("/generate-code")
    @ResponseBody
    public String generateCode() {
        return transactionService.generateTransactionCode();
    }
}