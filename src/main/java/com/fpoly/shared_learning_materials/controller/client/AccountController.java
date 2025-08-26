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
import java.time.format.DateTimeFormatter;
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
import com.fpoly.shared_learning_materials.service.DocumentService;
import com.fpoly.shared_learning_materials.domain.Document;
import java.time.format.DateTimeFormatter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;

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

    @Autowired
    private DocumentService documentService;

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

        // Balance flow stats cho dashboard
        java.util.Map<String, Object> balanceStats = transactionService.getBalanceFlowStats(currentUser);
        BigDecimal coinBalance = currentUser.getCoinBalance();

        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("documentCount", documentCount);
        model.addAttribute("totalDownloads", totalDownloads);
        model.addAttribute("totalRatings", totalRatings);
        model.addAttribute("coinBalance", coinBalance);
        model.addAttribute("balanceStats", balanceStats);

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

    @GetMapping("/documents/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDocumentsData(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "tab", defaultValue = "all") String tab,
            @RequestParam(value = "q", required = false) String keyword) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(Math.max(page, 0), Math.max(size, 1), org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        org.springframework.data.domain.Page<Document> documentPage;

        // Sử dụng DocumentService để lấy documents của user
        if (keyword != null && !keyword.trim().isEmpty()) {
            documentPage = documentService.searchDocuments(keyword.trim(), null, null, null, null, null, pageable);
            // Lọc chỉ lấy documents của user hiện tại
            List<Document> userDocuments = documentPage.getContent().stream()
                    .filter(doc -> doc.getFile() != null && currentUser.equals(doc.getFile().getUploadedBy()))
                    .collect(java.util.stream.Collectors.toList());

            // Tạo Page mới với documents đã lọc
            documentPage = new org.springframework.data.domain.PageImpl<>(
                    userDocuments, pageable, userDocuments.size());
        } else {
            // Lấy tất cả documents của user
            List<Document> allUserDocuments = documentService.getAllDocumentsForExport().stream()
                    .map(docDTO -> documentService.findById(docDTO.getId()))
                    .filter(doc -> doc != null && doc.getFile() != null
                            && currentUser.equals(doc.getFile().getUploadedBy()))
                    .collect(java.util.stream.Collectors.toList());

            // Lọc theo tab
            if (!"all".equals(tab.toLowerCase())) {
                allUserDocuments = allUserDocuments.stream()
                        .filter(doc -> tab.toLowerCase().equals(doc.getStatus().toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Phân trang
            int start = page * size;
            int end = Math.min(start + size, allUserDocuments.size());
            List<Document> pageContent = allUserDocuments.subList(start, end);

            documentPage = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, allUserDocuments.size());
        }

        List<Map<String, Object>> documents = documentPage.getContent().stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("id", doc.getId());
                    docMap.put("title", doc.getTitle());
                    docMap.put("description", doc.getDescription());
                    docMap.put("price", doc.getPrice());
                    docMap.put("status", doc.getStatus());
                    docMap.put("fileType", doc.getFile() != null ? doc.getFile().getFileType() : null);
                    docMap.put("fileSize", doc.getFile() != null ? doc.getFile().getFileSize() : null);
                    docMap.put("thumbnailUrl", "/images/default-thumbnail.png"); // Sử dụng thumbnail mặc định
                    docMap.put("viewsCount", doc.getViewsCount());
                    docMap.put("downloadsCount", doc.getDownloadsCount());
                    docMap.put("createdAt", doc.getCreatedAt());
                    docMap.put("updatedAt", doc.getUpdatedAt());
                    return docMap;
                })
                .collect(java.util.stream.Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", documents);
        response.put("page", documentPage.getNumber());
        response.put("size", documentPage.getSize());
        response.put("totalElements", documentPage.getTotalElements());
        response.put("totalPages", documentPage.getTotalPages());
        response.put("first", documentPage.isFirst());
        response.put("last", documentPage.isLast());

        return ResponseEntity.ok(response);
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

        // Balance flow stats bao gồm cả withdrawal và recharge
        java.util.Map<String, Object> balanceStats = transactionService.getBalanceFlowStats(currentUser);
        model.addAttribute("balanceStats", balanceStats);
        model.addAttribute("statTotalPurchase", balanceStats.get("totalRecharge"));
        model.addAttribute("statTotalWithdrawal", balanceStats.get("totalWithdrawal"));
        model.addAttribute("statPendingWithdrawal", balanceStats.get("totalPendingWithdrawal"));
        model.addAttribute("statAvailableBalance", balanceStats.get("availableBalance"));
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

            // Thêm thông tin hiển thị theo loại giao dịch
            if (tx.getType() == com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL) {
                dto.put("displayAmount", tx.getAmount() + " xu");
                dto.put("isWithdrawal", true);
            } else {
                dto.put("displayAmount", tx.getAmount() + " ₫");
                dto.put("isWithdrawal", false);
            }

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
                    "totalSpent", totalSpent,
                    "coinBalance",
                    currentUser.getCoinBalance() != null ? currentUser.getCoinBalance() : java.math.BigDecimal.ZERO));
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

            // Thêm thống kê bảo mật cho stats cards
            // 1. Trạng thái tài khoản
            boolean isAccountActive = "active".equalsIgnoreCase(currentUser.getStatus());
            String accountStatusText = isAccountActive ? "Hoạt động" : "Bị khóa";
            String accountStatusIcon = isAccountActive ? "success" : "danger";
            model.addAttribute("isAccountActive", isAccountActive);
            model.addAttribute("accountStatusText", accountStatusText);
            model.addAttribute("accountStatusIcon", accountStatusIcon);

            // 2. Thời gian tạo tài khoản (tuổi tài khoản)
            long accountAge = 0;
            String accountAgeText = "Mới tạo";
            String accountAgeIcon = "info";
            if (currentUser.getCreatedAt() != null) {
                accountAge = java.time.Duration.between(currentUser.getCreatedAt(), LocalDateTime.now()).toDays();
                if (accountAge >= 365) {
                    accountAgeText = (accountAge / 365) + " năm";
                    accountAgeIcon = "success";
                } else if (accountAge >= 30) {
                    accountAgeText = (accountAge / 30) + " tháng";
                    accountAgeIcon = "primary";
                } else if (accountAge > 0) {
                    accountAgeText = accountAge + " ngày";
                    accountAgeIcon = "info";
                }
            }
            model.addAttribute("accountAge", accountAge);
            model.addAttribute("accountAgeText", accountAgeText);
            model.addAttribute("accountAgeIcon", accountAgeIcon);

            // 3. Lần đăng nhập cuối
            String lastLoginText = currentUser.getLastLoginAt() != null
                    ? currentUser.getLastLoginAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "Chưa đăng nhập";
            model.addAttribute("lastLoginText", lastLoginText);
            String lastLoginIcon = currentUser.getLastLoginAt() != null ? "success" : "warning";

            // 4. Mức độ bảo mật tài khoản
            int securityScore = 0;
            if (hasEmail)
                securityScore += 25;
            if (hasPhone)
                securityScore += 25;
            if (currentUser.getEmailVerifiedAt() != null)
                securityScore += 25;
            if (currentUser.getLastLoginAt() != null)
                securityScore += 25;

            String securityScoreText = securityScore + "%";
            String securityScoreIcon;
            if (securityScore >= 75) {
                securityScoreIcon = "success";
            } else if (securityScore >= 50) {
                securityScoreIcon = "warning";
            } else {
                securityScoreIcon = "danger";
            }
            model.addAttribute("securityScore", securityScore);
            model.addAttribute("securityScoreText", securityScoreText);
            model.addAttribute("securityScoreIcon", securityScoreIcon);
            model.addAttribute("lastLoginIcon", lastLoginIcon);

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
            HttpServletRequest request,
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
                redirectAttributes.addFlashAttribute("error",
                        "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt");
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
                            java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    "system");

            // Logout user after successful password change for security
            SecurityContextHolder.clearContext();

            // Invalidate session for complete logout
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            redirectAttributes.addFlashAttribute("success",
                    "Đổi mật khẩu thành công! Vui lòng đăng nhập lại với mật khẩu mới.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/account/security";
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

    // ===== WITHDRAWAL METHODS =====

    @GetMapping("/withdraw")
    public String withdraw(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        model.addAttribute("pageTitle", "Rút tiền");

        // Stats for current user
        Long userId = null;
        if (userDetails instanceof CustomUserDetailsService.CustomUserPrincipal) {
            userId = ((CustomUserDetailsService.CustomUserPrincipal) userDetails).getUserId();
        }
        // Fallback: lấy qua SecurityContext nếu userId null
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                Optional<User> uByUsername = userRepository.findByUsernameAndDeletedAtIsNull(username);
                if (uByUsername.isPresent()) {
                    userId = uByUsername.get().getId();
                }
            }
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

                // Thêm thông tin cho form rút tiền
                model.addAttribute("minWithdrawalAmount", new java.math.BigDecimal("50")); // 50 xu tối thiểu
                model.addAttribute("exchangeRate", new java.math.BigDecimal("1000")); // 1 xu = 1000 VND

                // Payment methods
                java.util.Map<String, String> paymentMethods = new java.util.LinkedHashMap<>();
                paymentMethods.put("BANK_TRANSFER", "Chuyển khoản ngân hàng");
                paymentMethods.put("E_WALLET", "Ví điện tử");
                model.addAttribute("paymentMethods", paymentMethods);

                // Flags for UI
                boolean isPremium = withdrawalService.isUserPremium(u);
                boolean isFirst = withdrawalService.isFirstWithdrawal(u);
                boolean promoActive = withdrawalService.isPromotionActive();
                model.addAttribute("isPremium", isPremium);
                model.addAttribute("isFirstWithdrawal", isFirst);
                model.addAttribute("promoActive", promoActive);
            }
        }
        return "client/account/withdraw";
    }

    @PostMapping("/withdraw/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateWithdrawal(@RequestParam("coinAmount") String coinAmountStr) {
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

            // Parse safely
            java.math.BigDecimal coinAmount;
            try {
                if (coinAmountStr == null || coinAmountStr.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(java.util.Map.of(
                            "success", false,
                            "message", "Vui lòng nhập số xu"));
                }
                coinAmount = new java.math.BigDecimal(coinAmountStr.trim());
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số xu không hợp lệ"));
            }

            // Validate minimum amount
            java.math.BigDecimal minAmount = withdrawalService.getMinWithdrawalAmount();
            if (coinAmount.compareTo(minAmount) < 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số xu tối thiểu để rút là " + minAmount + " xu"));
            }

            // Check user balance
            java.math.BigDecimal userBalance = currentUser.getCoinBalance() != null ? currentUser.getCoinBalance()
                    : java.math.BigDecimal.ZERO;
            if (coinAmount.compareTo(userBalance) > 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số dư xu không đủ để thực hiện giao dịch"));
            }

            // Calculate
            java.math.BigDecimal basePct = withdrawalService.getBaseFeePercentage(coinAmount);
            boolean premium = withdrawalService.isUserPremium(currentUser);
            boolean first = withdrawalService.isFirstWithdrawal(currentUser);
            boolean promo = withdrawalService.isPromotionActive();
            boolean trusted = withdrawalService.isTrustedUser(currentUser);

            java.math.BigDecimal fee = withdrawalService.calculateWithdrawalFee(currentUser, coinAmount);
            java.math.BigDecimal netAmount = withdrawalService.calculateNetAmount(currentUser, coinAmount);
            java.math.BigDecimal vndAmount = withdrawalService.calculateVndAmount(currentUser, coinAmount);
            java.math.BigDecimal exchangeRate = withdrawalService.getExchangeRate();

            java.math.BigDecimal effectivePct = coinAmount.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? fee.divide(coinAmount, 4, java.math.RoundingMode.HALF_UP)
                    : java.math.BigDecimal.ZERO;

            java.util.Map<String, Object> etaInfo = withdrawalService.getEtaInfo(currentUser, coinAmount);

            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("coinAmount", coinAmount);
            result.put("fee", fee);
            result.put("netAmount", netAmount);
            result.put("vndAmount", vndAmount);
            result.put("feePercentage", effectivePct.multiply(new java.math.BigDecimal("100")));
            result.put("baseFeePercentage", basePct.multiply(new java.math.BigDecimal("100")));
            result.put("isPremium", premium);
            result.put("isFirstWithdrawal", first);
            result.put("promoActive", promo);
            result.put("trustedUser", trusted);
            result.put("exchangeRate", exchangeRate);
            result.putAll(etaInfo);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/withdraw/eta")
    @ResponseBody
    public ResponseEntity<?> getWithdrawalEta(@RequestParam java.math.BigDecimal coinAmount) {
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
        java.util.Map<String, Object> eta = withdrawalService.getEtaInfo(currentUser, coinAmount);
        return ResponseEntity.ok(eta);
    }

    @PostMapping("/bank/detect")
    @ResponseBody
    public ResponseEntity<?> detectBank(@RequestParam String accountNumber) {
        if (accountNumber == null)
            accountNumber = "";
        String sanitized = accountNumber.replaceAll("\\s+", "");
        String bankName = null;

        // Enhanced bank detection with better validation
        if (sanitized.length() >= 4) {
            if (sanitized.startsWith("9704"))
                bankName = "Vietcombank";
            else if (sanitized.startsWith("9703"))
                bankName = "BIDV";
            else if (sanitized.startsWith("9702"))
                bankName = "VietinBank";
            else if (sanitized.startsWith("9701"))
                bankName = "Agribank";
        }

        boolean detected = bankName != null;
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("detected", detected);
        resp.put("bankName", bankName != null ? bankName : "");
        resp.put("accountNumber", sanitized); // Return sanitized number for debugging
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/withdraw/request")
    @ResponseBody
    public ResponseEntity<?> requestWithdrawal(
            @RequestParam("coinAmount") String coinAmountStr,
            @RequestParam String paymentMethod,
            @RequestParam String bankAccount,
            @RequestParam String bankName,
            @RequestParam String accountHolder,
            @RequestParam(required = false) String notes) {
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

            // Parse safely
            java.math.BigDecimal coinAmount;
            try {
                if (coinAmountStr == null || coinAmountStr.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(java.util.Map.of(
                            "success", false,
                            "message", "Vui lòng nhập số xu"));
                }
                coinAmount = new java.math.BigDecimal(coinAmountStr.trim());
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số xu không hợp lệ"));
            }

            // Validate minimum amount
            java.math.BigDecimal minAmount = withdrawalService.getMinWithdrawalAmount();
            if (coinAmount.compareTo(minAmount) < 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số xu tối thiểu để rút là " + minAmount + " xu"));
            }

            // Validate payment method friendly before persist
            if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Vui lòng chọn phương thức rút tiền"));
            }

            // Check user balance
            java.math.BigDecimal userBalance = currentUser.getCoinBalance() != null ? currentUser.getCoinBalance()
                    : java.math.BigDecimal.ZERO;
            if (coinAmount.compareTo(userBalance) > 0) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Số dư xu không đủ để thực hiện giao dịch"));
            }

            // Calculate fee with all rules
            boolean premium = withdrawalService.isUserPremium(currentUser);
            boolean first = withdrawalService.isFirstWithdrawal(currentUser);
            boolean promo = withdrawalService.isPromotionActive();
            java.math.BigDecimal basePct = withdrawalService.getBaseFeePercentage(coinAmount);
            java.math.BigDecimal fee = withdrawalService.calculateWithdrawalFee(currentUser, coinAmount);
            java.math.BigDecimal netAmount = withdrawalService.calculateNetAmount(currentUser, coinAmount);

            // Create withdrawal transaction
            com.fpoly.shared_learning_materials.domain.Transaction withdrawal = new com.fpoly.shared_learning_materials.domain.Transaction();
            withdrawal.setUser(currentUser);
            withdrawal.setType(com.fpoly.shared_learning_materials.domain.Transaction.TransactionType.WITHDRAWAL);
            withdrawal.setAmount(coinAmount);
            withdrawal.setStatus(com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING);
            withdrawal.setPaymentMethod(paymentMethod);

            // Build notes with withdrawal details
            StringBuilder notesBuilder = new StringBuilder();
            notesBuilder.append("Rút tiền: ").append(coinAmount).append(" xu");
            notesBuilder.append(" | Phí: ").append(fee).append(" xu (")
                    .append(coinAmount.compareTo(java.math.BigDecimal.ZERO) > 0
                            ? fee.divide(coinAmount, 4, java.math.RoundingMode.HALF_UP)
                                    .multiply(new java.math.BigDecimal("100"))
                            : java.math.BigDecimal.ZERO)
                    .append("%)");
            notesBuilder.append(" | Số xu thực nhận: ").append(netAmount).append(" xu");
            if (first)
                notesBuilder.append(" | Miễn phí lần đầu");
            if (premium)
                notesBuilder.append(" | Premium -0.5% phí");
            if (promo)
                notesBuilder.append(" | Khuyến mãi -50% phí");
            notesBuilder.append(" | Ngân hàng: ").append(bankName);
            notesBuilder.append(" | Số tài khoản: ").append(bankAccount);
            notesBuilder.append(" | Chủ tài khoản: ").append(accountHolder);
            if (notes != null && !notes.trim().isEmpty()) {
                notesBuilder.append(" | Ghi chú: ").append(notes.trim());
            }
            withdrawal.setNotes(notesBuilder.toString());

            // Save withdrawal với balance check và trừ coin tự động
            com.fpoly.shared_learning_materials.domain.Transaction saved = withdrawalService
                    .createWithdrawalWithBalanceCheck(withdrawal);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "withdrawalCode", saved.getCode(),
                    "message", "Yêu cầu rút tiền đã được tạo thành công"));
        } catch (jakarta.validation.ConstraintViolationException ve) {
            String msg = ve.getConstraintViolations().stream().findFirst()
                    .map(cv -> cv.getMessage()).orElse("Dữ liệu không hợp lệ");
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", msg));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message", "Có lỗi xảy ra, vui lòng thử lại"));
        }
    }

    @PostMapping("/withdraw/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelWithdrawal(@PathVariable Long id) {
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

            Optional<com.fpoly.shared_learning_materials.domain.Transaction> withdrawalOpt = withdrawalService
                    .getWithdrawalById(id);
            if (withdrawalOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Withdrawal not found");
            }

            com.fpoly.shared_learning_materials.domain.Transaction withdrawal = withdrawalOpt.get();

            // Check if withdrawal belongs to current user
            if (!withdrawal.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            // Check if withdrawal can be cancelled (only PENDING status)
            if (withdrawal
                    .getStatus() != com.fpoly.shared_learning_materials.domain.Transaction.TransactionStatus.PENDING) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                        "success", false,
                        "message", "Chỉ có thể hủy yêu cầu rút tiền đang chờ xử lý"));
            }

            // Cancel withdrawal và hoàn lại coin tự động
            withdrawalService.cancelWithdrawal(withdrawal);

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Đã hủy yêu cầu rút tiền và hoàn lại xu"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ===== NOTIFICATION METHODS =====

    @GetMapping("/notifications")
    public String notifications(Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để xem thông báo");
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
        // Tạo thông báo mẫu nếu chưa có (chỉ để test)
        List<Notification> existingNotifications = notificationService.getUserNotifications(currentUser);
        if (existingNotifications.isEmpty()) {
            // Thông báo chào mừng
            notificationService.createNotification(currentUser,
                    "Chào mừng bạn đến với EduShare",
                    "Cảm ơn bạn đã đăng ký tài khoản. Hãy khám phá các tài liệu học tập phong phú trên hệ thống của chúng tôi. Bạn có thể tìm kiếm tài liệu theo danh mục, tải xuống và chia sẻ kiến thức với cộng đồng.",
                    "system");

            // Thông báo tài liệu mới
            notificationService.createNotification(currentUser,
                    "Tài liệu mới được đăng tải",
                    "Có tài liệu mới về 'Lập trình Java Spring Boot - Từ cơ bản đến nâng cao' vừa được đăng tải bởi Admin. Tài liệu bao gồm 15 chương với nhiều ví dụ thực tế và bài tập. Hãy xem ngay để nâng cao kỹ năng lập trình của bạn!",
                    "document");

            // Thông báo giao dịch
            notificationService.createNotification(currentUser,
                    "Giao dịch thành công",
                    "Bạn đã nạp thành công 100.000 VNĐ vào tài khoản EduShare. Số xu hiện tại của bạn là 500 xu. Bạn có thể sử dụng xu để tải xuống các tài liệu premium hoặc mua gói coin để tiết kiệm hơn.",
                    "transaction");

            // Thông báo bảo mật
            notificationService.createNotification(currentUser,
                    "Cập nhật bảo mật tài khoản",
                    "Chúng tôi khuyến khích bạn cập nhật thông tin bảo mật tài khoản để đảm bảo an toàn. Hãy thêm số điện thoại, xác thực email và sử dụng mật khẩu mạnh. Truy cập trang Bảo mật để cập nhật ngay.",
                    "system");

            // Thông báo khuyến mãi
            notificationService.createNotification(currentUser,
                    "Khuyến mãi đặc biệt - Giảm 20%",
                    "🎉 Chương trình khuyến mãi tháng này: Giảm 20% cho tất cả gói coin! Áp dụng từ ngày 01/12 đến 31/12. Sử dụng mã EDUSHARE20 khi thanh toán. Đây là cơ hội tuyệt vời để nạp coin với giá ưu đãi nhất.",
                    "transaction");
        }

        List<Notification> notifications = notificationService.getUserNotifications(currentUser);
        long unreadCount = notificationService.getUnreadCount(currentUser);

        // Tính toán số lượng thông báo theo loại
        long systemCount = notifications.stream().filter(n -> "system".equals(n.getType())).count();
        long readCount = notifications.size() - unreadCount;

        model.addAttribute("pageTitle", "Thông báo");
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("systemCount", systemCount);
        model.addAttribute("readCount", readCount);

        return "client/account/notifications";
    }

    @GetMapping("/notifications/{id}")
    public String notificationDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Vui lòng đăng nhập để xem thông báo");
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

        // Tìm notification và kiểm tra quyền truy cập
        Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
        if (notificationOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy thông báo");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/account/notifications";
        }

        Notification notification = notificationOpt.get();

        // Kiểm tra quyền truy cập - chỉ user sở hữu notification mới được xem
        if (notification.getUser() != null && !notification.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Bạn không có quyền xem thông báo này");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/account/notifications";
        }

        // Đánh dấu đã đọc nếu chưa đọc
        if (!notification.getIsRead()) {
            notificationService.markAsRead(id);
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        List<Notification> notificationss = notificationService.getUserNotifications(currentUser);
        long unreadCount = notificationService.getUnreadCount(currentUser);

        // Tính toán số lượng thông báo theo loại
        long systemCount = notificationss.stream().filter(n -> "system".equals(n.getType())).count();
        long readCount = notificationss.size() - unreadCount;

        model.addAttribute("pageTitle", "Chi tiết thông báo");
        model.addAttribute("notification", notification);
        model.addAttribute("notifications", notificationss);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("systemCount", systemCount);
        model.addAttribute("readCount", readCount);

        return "client/account/notification-detail";
    }

    @PostMapping("/notifications/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long id) {
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

        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
            if (notificationOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Notification not found");
            }

            Notification notification = notificationOpt.get();

            // Kiểm tra quyền truy cập
            if (notification.getUser() != null && !notification.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            notificationService.markAsRead(id);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<String> markAllNotificationsAsRead() {
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

        try {
            notificationService.markAllAsRead(currentUser);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}