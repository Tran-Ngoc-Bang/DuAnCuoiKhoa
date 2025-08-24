package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.CoinPackage;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.CoinPackageService;
import com.fpoly.shared_learning_materials.service.NotificationService;

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
@RequestMapping("/admin/coin-packages")
public class CoinPackageController extends BaseAdminController {

    @Autowired
    private CoinPackageService coinPackageService;

    public CoinPackageController(NotificationService notificationService, UserRepository userRepository) {
        super(notificationService, userRepository);
    }

    /**
     * Hiển thị danh sách gói xu với phân trang và tìm kiếm
     */
    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            Model model) {

        // Tạo Pageable object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Lấy dữ liệu với search và filter
        Page<CoinPackage> packagePage;
        CoinPackage.PackageStatus statusEnum = null;

        if (status != null && !status.isEmpty() && !status.equals("all")) {
            try {
                statusEnum = CoinPackage.PackageStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusEnum = null;
            }
        }

        if ((keyword != null && !keyword.trim().isEmpty()) || statusEnum != null) {
            packagePage = coinPackageService.searchAndFilterPackages(keyword, statusEnum, pageable);
        } else {
            // Mặc định cho admin: hiển thị tất cả kể cả đã xóa mềm
            packagePage = coinPackageService.getAllPackagesIncludingDeleted(pageable);
        }

        // Thêm dữ liệu vào model
        model.addAttribute("packages", packagePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", packagePage.getTotalPages());
        model.addAttribute("totalElements", packagePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status != null ? status : "all");

        // Thêm thông tin phân trang
        model.addAttribute("hasPrevious", packagePage.hasPrevious());
        model.addAttribute("hasNext", packagePage.hasNext());
        model.addAttribute("isFirst", packagePage.isFirst());
        model.addAttribute("isLast", packagePage.isLast());

        return "admin/coin-package/index";
    }

    /**
     * Hiển thị form tạo gói xu mới
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("coinPackage", new CoinPackage());
        model.addAttribute("statusOptions", CoinPackage.PackageStatus.values());
        return "admin/coin-package/create";
    }

    /**
     * Xử lý tạo gói xu mới
     */
    @PostMapping("/create")
    public String createPackage(@ModelAttribute CoinPackage coinPackage,
            RedirectAttributes redirectAttributes) {
        try {
            // Tự động generate code nếu chưa có
            if (coinPackage.getCode() == null || coinPackage.getCode().trim().isEmpty()) {
                coinPackage.setCode(coinPackageService.generatePackageCode(coinPackage.getName()));
            }

            coinPackageService.createPackage(coinPackage);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo gói xu thành công: " + coinPackage.getName());
            return "redirect:/admin/coin-packages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi tạo gói xu: " + e.getMessage());
            return "redirect:/admin/coin-packages/create";
        }
    }

    /**
     * Hiển thị chi tiết gói xu
     */
    @GetMapping("/{id}/detail")
    public String showDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CoinPackage> packageOpt = coinPackageService.getPackageById(id);
        if (packageOpt.isPresent()) {
            model.addAttribute("coinPackage", packageOpt.get());
            // Thêm thống kê
            model.addAttribute("purchaseCount", coinPackageService.countSoldPackages(id));
            model.addAttribute("totalRevenue", coinPackageService.sumRevenueByPackage(id));
            model.addAttribute("recentBuyers", coinPackageService.findRecentBuyersByPackage(id, 10));
            return "admin/coin-package/detail";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gói xu");
            return "redirect:/admin/coin-packages";
        }
    }

    /**
     * Hiển thị form chỉnh sửa gói xu
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CoinPackage> packageOpt = coinPackageService.getPackageById(id);
        if (packageOpt.isPresent()) {
            model.addAttribute("coinPackage", packageOpt.get());
            model.addAttribute("statusOptions", CoinPackage.PackageStatus.values());
            return "admin/coin-package/edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gói xu");
            return "redirect:/admin/coin-packages";
        }
    }

    /**
     * Xử lý cập nhật gói xu
     */
    @PostMapping("/{id}/edit")
    public String updatePackage(@PathVariable Long id,
            @ModelAttribute CoinPackage coinPackage,
            RedirectAttributes redirectAttributes) {
        try {
            coinPackage.setId(id);
            coinPackageService.updatePackage(coinPackage);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật gói xu thành công: " + coinPackage.getName());
            return "redirect:/admin/coin-packages";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi cập nhật gói xu: " + e.getMessage());
            return "redirect:/admin/coin-packages/" + id + "/edit";
        }
    }

    /**
     * Hiển thị trang xác nhận xóa gói xu
     */
    @GetMapping("/{id}/delete")
    public String showDeleteForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CoinPackage> packageOpt = coinPackageService.getPackageById(id);
        if (packageOpt.isPresent()) {
            model.addAttribute("coinPackage", packageOpt.get());
            return "admin/coin-package/delete";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gói xu");
            return "redirect:/admin/coin-packages";
        }
    }

    /**
     * Xóa gói xu (soft delete)
     */
    @PostMapping("/{id}/delete")
    public String deletePackage(@PathVariable Long id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        System.out.println("=== DELETE PACKAGE START ===");
        System.out.println("Package ID: " + id);
        System.out.println("Reason: " + reason);

        try {
            System.out.println("Finding package...");
            Optional<CoinPackage> packageOpt = coinPackageService.getPackageById(id);
            if (packageOpt.isPresent()) {
                System.out.println("Package found: " + packageOpt.get().getName());
                System.out.println("Calling delete service...");
                coinPackageService.deletePackage(id);
                System.out.println("Delete service completed");

                redirectAttributes.addFlashAttribute("successMessage",
                        "Xóa gói xu thành công: " + packageOpt.get().getName());
            } else {
                System.out.println("Package not found");
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gói xu");
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi xóa gói xu: " + e.getMessage());
        }

        System.out.println("Redirecting to /admin/coin-packages");
        System.out.println("=== DELETE PACKAGE END ===");
        return "redirect:/admin/coin-packages";
    }

    /**
     * Khôi phục gói xu đã xóa mềm
     */
    @PostMapping("/{id}/restore")
    public String restorePackage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<CoinPackage> packageOpt = coinPackageService.getPackageById(id);
            if (packageOpt.isPresent()) {
                coinPackageService.restorePackage(id);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Khôi phục gói xu thành công: " + packageOpt.get().getName());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy gói xu");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Lỗi khi khôi phục gói xu: " + e.getMessage());
        }
        return "redirect:/admin/coin-packages";
    }

}