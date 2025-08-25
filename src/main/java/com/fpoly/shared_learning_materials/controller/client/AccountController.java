package com.fpoly.shared_learning_materials.controller.client;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.fpoly.shared_learning_materials.service.CoinPackageService;
import com.fpoly.shared_learning_materials.service.NotificationService;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.domain.Notification;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fpoly.shared_learning_materials.domain.Favorite;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.CommentRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.FavoriteRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.CoinPackageService;
import com.fpoly.shared_learning_materials.service.WithdrawalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.fpoly.shared_learning_materials.repository.TransactionDetailRepository;
import com.fpoly.shared_learning_materials.config.CustomUserDetailsService;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


/**
 * Controller for user account pages
 */
@Controller
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    @Autowired
    private CoinPackageService coinPackageService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private TransactionDetailRepository transactionDetailRepository;

    @Autowired
    private com.fpoly.shared_learning_materials.service.TransactionService transactionService;

    @Autowired
    private WithdrawalService withdrawalService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để xem trang quản lý");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        Long documentCount = documentRepository.countByUser(currentUser);

        Long totalDownloads = documentRepository.sumDownloadsByUser(currentUser);

        Integer totalRatings = commentRepository.sumRatingsByUserDocuments(currentUser.getId());

        BigDecimal coinBalance = currentUser.getCoinBalance();

        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("documentCount", documentCount);
        model.addAttribute("totalDownloads", totalDownloads);
        model.addAttribute("totalRatings", totalRatings);
        model.addAttribute("coinBalance", coinBalance);

        return "client/account/dashboard";
    }

    @GetMapping("/stats")
    @ResponseBody
    public ResponseEntity<?> getStatsByRange(@RequestParam String range) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Map<String, Object> response = new HashMap<>();

        List<String> categories = new ArrayList<>();
        List<Integer> downloads = new ArrayList<>();
        List<Integer> views = new ArrayList<>();
        List<Integer> coins = new ArrayList<>();

        LocalDate now = LocalDate.now();

        switch (range) {
            case "day":
                for (int hour = 8; hour <= 20; hour += 2) {
                    LocalDateTime from = now.atTime(hour, 0);
                    LocalDateTime to = from.plusHours(2);

                    categories.add(String.format("%02d:00", hour));
                    downloads.add(documentRepository.countDownloadsBetween(currentUser.getId(), from, to));
                    views.add(documentRepository.countViewsBetween(currentUser.getId(), from, to));
                    coins.add(documentRepository.sumCoinsBetween(currentUser.getId(), from, to));
                }
                break;

            case "week":
                for (int i = 0; i < 7; i++) {
                    LocalDate day = now.with(DayOfWeek.MONDAY).plusDays(i);
                    LocalDateTime start = day.atStartOfDay();
                    LocalDateTime end = start.plusDays(1);

                    categories.add("T" + (i + 2));
                    downloads.add(documentRepository.countDownloadsBetween(currentUser.getId(), start, end));
                    views.add(documentRepository.countViewsBetween(currentUser.getId(), start, end));
                    coins.add(documentRepository.sumCoinsBetween(currentUser.getId(), start, end));
                }
                break;

            case "month":
                for (int i = 0; i < 4; i++) {
                    LocalDateTime start = now.withDayOfMonth(1).plusWeeks(i).atStartOfDay();
                    LocalDateTime end = start.plusWeeks(1);
                    categories.add("Tuần " + (i + 1));
                    downloads.add(documentRepository.countDownloadsBetween(currentUser.getId(), start, end));
                    views.add(documentRepository.countViewsBetween(currentUser.getId(), start, end));
                    coins.add(documentRepository.sumCoinsBetween(currentUser.getId(), start, end));
                }
                break;

            case "year":
                for (int i = 1; i <= 12; i++) {
                    YearMonth ym = YearMonth.of(now.getYear(), i);
                    LocalDateTime start = ym.atDay(1).atStartOfDay();
                    LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
                    categories.add("T" + i);
                    downloads.add(documentRepository.countDownloadsBetween(currentUser.getId(), start, end));
                    views.add(documentRepository.countViewsBetween(currentUser.getId(), start, end));
                    coins.add(documentRepository.sumCoinsBetween(currentUser.getId(), start, end));
                }
                break;
        }

        response.put("categories", categories);
        response.put("downloads", downloads);
        response.put("views", views);
        response.put("coins", coins);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("pageTitle", "Thông tin cá nhân");
        return "client/account/profile";
    }

    @GetMapping("/documents")
    public String myDocuments(Model model) {
        model.addAttribute("pageTitle", "Tài liệu của tôi");
        return "client/account/documents";
    }

    @GetMapping("/favorites")
    public String favorites(Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("pageTitle", "Tài liệu yêu thích");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để xem trang quản lý");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông tin người dùng");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/";
        }

        Set<Favorite> favorites = favoriteRepository.findByUser(currentUser);
        model.addAttribute("favorites", favorites);
        return "client/account/favorites";
    }

    @GetMapping("/transactions")
    public String transactions(Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "from", required = false) String fromDate,
            @RequestParam(value = "to", required = false) String toDate,
            @RequestParam(value = "min", required = false) java.math.BigDecimal minAmount,
            @RequestParam(value = "max", required = false) java.math.BigDecimal maxAmount) {
        model.addAttribute("pageTitle", "Lịch sử giao dịch");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return "redirect:/";
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(Math.max(page, 0), Math.max(size, 1), org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus statusEnum = null;
        com.fpoly.shared_learning_materials.domain.Transaction.TransactionType typeEnum = null;
        try {
            if (status != null && !status.isBlank())
                statusEnum = com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus
                        .valueOf(status.toUpperCase());
        } catch (Exception ignored) {
        }
        try {
            if (type != null && !type.isBlank())
                typeEnum = com.fpoly.shared_learning_materials.domain.Transaction.TransactionType
                        .valueOf(type.toUpperCase());
        } catch (Exception ignored) {
        }

        java.time.LocalDateTime startDate = null, endDate = null;
        try {
            if (fromDate != null && !fromDate.isBlank()) {
                startDate = java.time.LocalDate.parse(fromDate).atStartOfDay();
            }
        } catch (Exception ignored) {
        }
        try {
            if (toDate != null && !toDate.isBlank()) {
                endDate = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception ignored) {
        }

        org.springframework.data.domain.Page<com.fpoly.shared_learning_materials.domain.Transaction> transactions = transactionService
                .searchUserTransactions(currentUser, keyword, statusEnum, typeEnum, startDate, endDate, minAmount,
                        maxAmount, pageable);

        BigDecimal coinBalance = currentUser.getCoinBalance() != null ? currentUser.getCoinBalance()
                : BigDecimal.ZERO;

        model.addAttribute("coinBalance", coinBalance);
        model.addAttribute("transactionsPage", transactions);
        model.addAttribute("q", keyword);
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("from", fromDate);
        model.addAttribute("to", toDate);
        model.addAttribute("min", minAmount);
        model.addAttribute("max", maxAmount);

        // Stats: totals by type for the user (only completed)
        BigDecimal totalPurchase = transactionService.getUserTotalByType(currentUser,
                com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.PURCHASE,
                com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.COMPLETED);
        BigDecimal totalWithdrawal = transactionService.getUserTotalByType(currentUser,
                com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL,
                com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING)
                .add(transactionService.getUserTotalByType(currentUser,
                        com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL,
                        com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.COMPLETED));
        BigDecimal totalRefund = transactionService.getUserTotalByType(currentUser,
                com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.REFUND,
                null);
        model.addAttribute("statTotalPurchase", totalPurchase);
        model.addAttribute("statTotalWithdrawal", totalWithdrawal);
        model.addAttribute("statTotalRefund", totalRefund);
        return "client/account/transactions";
    }

    @GetMapping("/transactions/json")
    @ResponseBody
    public ResponseEntity<?> listMyTransactionsJson(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "from", required = false) String fromDate,
            @RequestParam(value = "to", required = false) String toDate,
            @RequestParam(value = "min", required = false) java.math.BigDecimal minAmount,
            @RequestParam(value = "max", required = false) java.math.BigDecimal maxAmount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(Math.max(page, 0), Math.max(size, 1), org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus statusEnum = null;
        com.fpoly.shared_learning_materials.domain.Transaction.TransactionType typeEnum = null;
        try {
            if (status != null && !status.isBlank())
                statusEnum = com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus
                        .valueOf(status.toUpperCase());
        } catch (Exception ignored) {
        }
        try {
            if (type != null && !type.isBlank())
                typeEnum = com.fpoly.shared_learning_materials.domain.Transaction.TransactionType
                        .valueOf(type.toUpperCase());
        } catch (Exception ignored) {
        }

        java.time.LocalDateTime startDate = null, endDate = null;
        try {
            if (fromDate != null && !fromDate.isBlank()) {
                startDate = java.time.LocalDate.parse(fromDate).atStartOfDay();
            }
        } catch (Exception ignored) {
        }
        try {
            if (toDate != null && !toDate.isBlank()) {
                endDate = java.time.LocalDate.parse(toDate).atTime(23, 59, 59);
            }
        } catch (Exception ignored) {
        }

        org.springframework.data.domain.Page<com.fpoly.shared_learning_materials.domain.Transaction> transactions = transactionService
                .searchUserTransactions(currentUser, keyword, statusEnum, typeEnum, startDate, endDate, minAmount,
                        maxAmount, pageable);

        java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
        for (com.fpoly.shared_learning_materials.domain.Transaction tx : transactions.getContent()) {
            java.util.Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", tx.getId());
            dto.put("code", tx.getCode());
            dto.put("type", tx.getType() != null ? tx.getType().name() : null);
            dto.put("amount", tx.getAmount());
            dto.put("status", tx.getStatus() != null ? tx.getStatus().name() : null);
            dto.put("paymentMethod", tx.getPaymentMethod());
            dto.put("createdAt", tx.getCreatedAt());
            dto.put("notes", tx.getNotes());
            items.add(dto);
        }
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("content", items);
        resp.put("page", transactions.getNumber());
        resp.put("size", transactions.getSize());
        resp.put("totalElements", transactions.getTotalElements());
        resp.put("totalPages", transactions.getTotalPages());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/transactions/stats")
    @ResponseBody
    public ResponseEntity<?> getTransactionStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            System.out.println("Getting stats for user: " + username);

            // Stats cards data
            long totalTransactions = transactionService.getUserTotalTransactions(currentUser);
            long completedTransactions = transactionService.getUserTransactionsByStatus(currentUser,
                    com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.COMPLETED);
            long pendingTransactions = transactionService.getUserTransactionsByStatus(currentUser,
                    com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING);
            java.math.BigDecimal totalSpent = transactionService.getUserTotalSpent(currentUser);

            System.out.println("Stats calculated: total=" + totalTransactions + ", completed=" + completedTransactions
                    + ", pending=" + pendingTransactions + ", spent=" + totalSpent);

            // Chart data - last 30 days
            java.time.LocalDateTime endDate = java.time.LocalDateTime.now();
            java.time.LocalDateTime startDate = endDate.minusDays(30);
            java.util.List<java.util.Map<String, Object>> dailyData = transactionService.getUserTransactionStatsByDate(
                    currentUser, startDate, endDate, "day");
            java.util.List<java.util.Map<String, Object>> typeData = transactionService.getUserTransactionStatsByType(
                    currentUser, startDate, endDate);

            System.out.println("Chart data: daily=" + dailyData.size() + " items, type=" + typeData.size() + " items");

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("stats", java.util.Map.of(
                    "totalTransactions", totalTransactions,
                    "completedTransactions", completedTransactions,
                    "pendingTransactions", pendingTransactions,
                    "totalSpent", totalSpent));
            response.put("dailyData", dailyData);
            response.put("typeData", typeData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getTransactionStats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/transactions/{code}")
    @ResponseBody
    public ResponseEntity<?> getMyTransaction(@PathVariable String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<com.fpoly.shared_learning_materials.domain.Transaction> txOpt = transactionService
                .getTransactionByCode(code);
        if (txOpt.isEmpty() || txOpt.get().getDeletedAt() != null
                || !txOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        }
        com.fpoly.shared_learning_materials.domain.Transaction tx = txOpt.get();
        java.util.Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("code", tx.getCode());
        dto.put("type", tx.getType());
        dto.put("amount", tx.getAmount());
        dto.put("status", tx.getStatus());
        dto.put("paymentMethod", tx.getPaymentMethod());
        dto.put("createdAt", tx.getCreatedAt());
        dto.put("notes", tx.getNotes());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/transactions/{code}/refund")
    @ResponseBody
    public ResponseEntity<?> requestRefund(@PathVariable String code, @RequestParam(required = false) String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<com.fpoly.shared_learning_materials.domain.Transaction> txOpt = transactionService
                .getTransactionByCode(code);
        if (txOpt.isEmpty() || txOpt.get().getDeletedAt() != null
                || !txOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        }
        try {
            com.fpoly.shared_learning_materials.domain.Transaction original = txOpt.get();
            com.fpoly.shared_learning_materials.domain.Transaction withdrawal = new com.fpoly.shared_learning_materials.domain.Transaction();
            withdrawal.setUser(original.getUser());
            withdrawal.setType(com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL);
            withdrawal.setAmount(original.getAmount());
            withdrawal.setStatus(com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING);
            withdrawal.setPaymentMethod(original.getPaymentMethod());
            withdrawal.setNotes((reason != null && !reason.trim().isEmpty() ? (reason.trim() + " | ") : "")
                    + "Refund request for " + original.getCode());
            com.fpoly.shared_learning_materials.domain.Transaction saved = withdrawalService
                    .createWithdrawal(withdrawal);
            return ResponseEntity.ok(java.util.Map.of("success", true, "withdrawalCode", saved.getCode()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/recharge")
    public String recharge(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("pageTitle", "Nạp tiền");
        model.addAttribute("coinPackages", coinPackageService.getActivePackagesForClient());

        // Stats for current user
        Long userId = null;
        if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal) {
            userId = ((CustomUserDetailsService.CustomUserPrincipal) userDetails).getUserId();
        }
        if (userId != null) {
            final Long currentUserId = userId;
            Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(currentUserId);
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                java.math.BigDecimal coinBalance = u.getCoinBalance() != null ? u.getCoinBalance()
                        : java.math.BigDecimal.ZERO;
                java.math.BigDecimal totalSpent = u.getTotalSpent() != null ? u.getTotalSpent()
                        : java.math.BigDecimal.ZERO;
                java.time.LocalDateTime lastLoginAt = u.getLastLoginAt();
                Long totalCoinsPurchased = transactionDetailRepository.sumTotalCoinsPurchasedByUser(currentUserId);
                if (totalCoinsPurchased == null)
                    totalCoinsPurchased = 0L;

                model.addAttribute("statCoinBalance", coinBalance);
                model.addAttribute("statTotalCoinsPurchased", totalCoinsPurchased);
                model.addAttribute("statTotalSpent", totalSpent);
                model.addAttribute("statLastLoginAt", lastLoginAt);
                String lastLoginAtText = lastLoginAt != null
                        ? lastLoginAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "Chưa đăng nhập";
                model.addAttribute("statLastLoginAtText", lastLoginAtText);
            }
        }
        return "client/account/recharge";
    }

    @GetMapping("/security")
    public String security(Model model, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            model.addAttribute("pageTitle", "Bảo mật tài khoản");
            model.addAttribute("currentUser", currentUser);
            
            // Check contact info availability (not verification)
            boolean hasEmail = currentUser.getEmail() != null && !currentUser.getEmail().trim().isEmpty();
            boolean hasPhone = currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().trim().isEmpty();
            
            // Security level based on contact info
            String securityLevel;
            String securityIcon;
            if (hasEmail && hasPhone) {
                securityLevel = "Cao";
                securityIcon = "success";
            } else if (hasEmail) {
                securityLevel = "Trung bình";
                securityIcon = "warning";
            } else {
                securityLevel = "Thấp";
                securityIcon = "danger";
            }
            
            // Account status based on status field
            String accountStatus;
            String statusIcon;
            switch (currentUser.getStatus().toLowerCase()) {
                case "active":
                    accountStatus = "Hoạt động";
                    statusIcon = "success";
                    break;
                case "inactive":
                    accountStatus = "Khóa";
                    statusIcon = "danger";
                    break;
                case "pending":
                    accountStatus = "Đang chờ";
                    statusIcon = "warning";
                    break;
                default:
                    accountStatus = "Không xác định";
                    statusIcon = "warning";
            }
            
            model.addAttribute("hasEmail", hasEmail);
            model.addAttribute("hasPhone", hasPhone);
            model.addAttribute("securityLevel", securityLevel);
            model.addAttribute("securityIcon", securityIcon);
            model.addAttribute("accountStatus", accountStatus);
            model.addAttribute("statusIcon", statusIcon);
            
            // Thêm thông tin liên hệ
            model.addAttribute("userEmail", currentUser.getEmail());
            model.addAttribute("userPhone", currentUser.getPhoneNumber());
            
            // Thêm thông tin hoạt động đăng nhập
            model.addAttribute("lastLoginAt", currentUser.getLastLoginAt());
            model.addAttribute("lastLoginIp", currentUser.getLastLoginIp());
            model.addAttribute("failedLoginAttempts", currentUser.getFailedLoginAttempts());
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin bảo mật: " + e.getMessage());
        }
        
        return "client/account/security";
    }
    
    @PostMapping("/security/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Validate input
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu hiện tại");
                return "redirect:/account/security";
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng nhập mật khẩu mới");
                return "redirect:/account/security";
            }
            
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "redirect:/account/security";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
                return "redirect:/account/security";
            }
            
            // Get current user
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/account/security";
            }
            
            // Check if new password is different from current password
            if (passwordEncoder.matches(newPassword, currentUser.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải khác với mật khẩu hiện tại");
                return "redirect:/account/security";
            }
            
            // Validate password strength
            if (!isPasswordStrong(newPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
                return "redirect:/account/security";
            }
            
            // Update password
            currentUser.setPasswordHash(passwordEncoder.encode(newPassword));
            currentUser.setUpdatedAt(java.time.LocalDateTime.now());
            userRepository.save(currentUser);
            
            // Create notification
            notificationService.createNotification(currentUser, 
                "Mật khẩu đã được thay đổi", 
                "Mật khẩu tài khoản của bạn đã được thay đổi thành công vào lúc " + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), 
                "system");
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/account/security";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/account/security";
        }
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Authentication authentication) {
        try {
            // Lấy user hiện tại
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Tạo thông báo mẫu nếu chưa có (chỉ để test)
            List<Notification> existingNotifications = notificationService.getUserNotifications(currentUser);
            if (existingNotifications.isEmpty()) {
                notificationService.createNotification(currentUser, 
                    "Chào mừng bạn đến với EduShare", 
                    "Cảm ơn bạn đã đăng ký tài khoản. Hãy khám phá các tài liệu học tập phong phú trên hệ thống của chúng tôi.", 
                    "system");
                    
                // notificationService.createNotification(currentUser, 
                //     "Tài liệu mới được đăng tải", 
                //     "Có tài liệu mới về 'Lập trình Java Spring Boot' vừa được đăng tải. Hãy xem ngay!", 
                //     "document");
                    
                // notificationService.createNotification(currentUser, 
                //     "Giao dịch thành công", 
                //     "Bạn đã nạp thành công 100.000 VNĐ vào tài khoản. Số xu hiện tại: 500 xu.", 
                //     "transaction");
            }
            
            // Lấy danh sách thông báo
            List<Notification> notifications = notificationService.getUserNotifications(currentUser);
            List<Notification> unreadNotifications = notificationService.getUnreadNotifications(currentUser);
            long unreadCount = notificationService.getUnreadCount(currentUser);
            
            // Tính toán số lượng thông báo theo loại
            long systemCount = notifications.stream().filter(n -> "system".equals(n.getType())).count();
            long readCount = notifications.size() - unreadCount;
            
            model.addAttribute("pageTitle", "Thông báo");
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotifications", unreadNotifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("systemCount", systemCount);
            model.addAttribute("readCount", readCount);
            model.addAttribute("currentUser", currentUser);
            
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông báo: " + e.getMessage());
        }
        
        return "client/account/notifications";
    }
    
    @PostMapping("/notifications/{id}/mark-read")
    @ResponseBody
    public String markNotificationAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Kiểm tra notification có thuộc về user hiện tại không
            List<Notification> userNotifications = notificationService.getUserNotifications(currentUser);
            boolean hasPermission = userNotifications.stream()
                    .anyMatch(n -> n.getId().equals(id));
            
            if (!hasPermission) {
                return "error: Không có quyền truy cập thông báo này";
            }
            
            notificationService.markAsRead(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
    
    @PostMapping("/notifications/mark-all-read")
    @ResponseBody
    public String markAllNotificationsAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            notificationService.markAllAsRead(currentUser);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/options")
    public String options(Model model) {
        model.addAttribute("pageTitle", "Tùy chọn");
        return "client/account/options";
    }

    @GetMapping("/support")
    public String support(Model model) {
        model.addAttribute("pageTitle", "Hỗ trợ");
        return "client/account/support";
    }

    
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^A-Za-z0-9].*");
        
        // Require ALL criteria for strong password
        return hasLowercase && hasUppercase && hasNumber && hasSpecial;

    }

    @GetMapping("/transactions/id/{id}")
    @ResponseBody
    public ResponseEntity<?> getMyTransactionById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<com.fpoly.shared_learning_materials.domain.Transaction> txOpt = transactionService
                .getTransactionById(id);
        if (txOpt.isEmpty() || txOpt.get().getDeletedAt() != null
                || !txOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        }
        com.fpoly.shared_learning_materials.domain.Transaction tx = txOpt.get();
        java.util.Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("id", tx.getId());
        dto.put("code", tx.getCode());
        dto.put("type", tx.getType());
        dto.put("amount", tx.getAmount());
        dto.put("status", tx.getStatus());
        dto.put("paymentMethod", tx.getPaymentMethod());
        dto.put("createdAt", tx.getCreatedAt());
        dto.put("notes", tx.getNotes());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/transactions/id/{id}/refund")
    @ResponseBody
    public ResponseEntity<?> requestRefundById(@PathVariable Long id, @RequestParam(required = false) String reason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        Optional<com.fpoly.shared_learning_materials.domain.Transaction> txOpt = transactionService
                .getTransactionById(id);
        if (txOpt.isEmpty() || txOpt.get().getDeletedAt() != null
                || !txOpt.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
        }
        try {
            com.fpoly.shared_learning_materials.domain.Transaction original = txOpt.get();
            com.fpoly.shared_learning_materials.domain.Transaction withdrawal = new com.fpoly.shared_learning_materials.domain.Transaction();
            withdrawal.setUser(original.getUser());
            withdrawal.setType(com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL);
            withdrawal.setAmount(original.getAmount());
            withdrawal.setStatus(com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING);
            withdrawal.setPaymentMethod(original.getPaymentMethod());
            withdrawal.setNotes((reason != null && !reason.trim().isEmpty() ? (reason.trim() + " | ") : "")
                    + "Refund request for " + original.getCode());
            com.fpoly.shared_learning_materials.domain.Transaction saved = withdrawalService
                    .createWithdrawal(withdrawal);
            return ResponseEntity.ok(java.util.Map.of("success", true, "withdrawalCode", saved.getCode()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }

    }
}