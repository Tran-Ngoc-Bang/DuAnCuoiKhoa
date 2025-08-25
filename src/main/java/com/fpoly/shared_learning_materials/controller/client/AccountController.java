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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fpoly.shared_learning_materials.domain.Favorite;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.CommentRepository;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;
import com.fpoly.shared_learning_materials.repository.FavoriteRepository;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.CoinPackageService;

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
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CommentRepository commentRepository;

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
    public String transactions(Model model) {
        model.addAttribute("pageTitle", "Lịch sử giao dịch");
        return "client/account/transactions";
    }

    @GetMapping("/recharge")
    public String recharge(Model model) {
        model.addAttribute("pageTitle", "Nạp tiền");
        model.addAttribute("coinPackages", coinPackageService.getActivePackagesForClient());
        return "client/account/recharge";
    }

    @GetMapping("/security")
    public String security(Model model) {
        model.addAttribute("pageTitle", "Bảo mật tài khoản");
        return "client/account/security";
    }

    @GetMapping("/notifications")
    public String notifications(Model model) {
        model.addAttribute("pageTitle", "Thông báo");
        return "client/account/notifications";
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
}