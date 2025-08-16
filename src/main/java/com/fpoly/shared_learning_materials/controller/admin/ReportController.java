package com.fpoly.shared_learning_materials.controller.admin;

// import javax.validation.Valid;
import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.repository.ReportRepository;
import com.fpoly.shared_learning_materials.service.ReportService;

import jakarta.validation.Valid;

import com.fpoly.shared_learning_materials.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;
    private final DocumentService documentService;

    @Autowired
    public ReportController(ReportService reportService, ReportRepository reportRepository,
            DocumentService documentService) {
        this.reportService = reportService;
        this.reportRepository = reportRepository;
        this.documentService = documentService;
    }

    // List reports - admin/reports/list.html
    @GetMapping
    public String listReports(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String violationType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        List<Map<String, Object>> reports;
        
        // Kiểm tra xem có filter nào được áp dụng không
        boolean hasFilters = (keyword != null && !keyword.isBlank()) ||
                           (violationType != null && !violationType.isBlank()) ||
                           (status != null && !status.isBlank());
        
        if (hasFilters) {
            // Sử dụng search với filters
            reports = reportService.searchReports(
                keyword == null || keyword.isBlank() ? null : keyword,
                violationType == null || violationType.isBlank() ? null : violationType,
                status == null || status.isBlank() ? null : status);
        } else {
            // Lấy tất cả báo cáo với format đúng
            reports = reportService.getAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("keyword", keyword);
        model.addAttribute("violationType", violationType);
        model.addAttribute("status", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", (reports.size() + size - 1) / size);

        // Summary counts
        model.addAttribute("totalReports", reportRepository.countAllReports());
        model.addAttribute("newReportsCount", reportService.getNewReportsCount());
        model.addAttribute("processingReportsCount", reportService.getProcessingReportsCount());
        model.addAttribute("resolvedReportsCount", reportService.getResolvedReportsCount());
        model.addAttribute("rejectedReportsCount", reportService.getRejectedReportsCount());

        return "admin/reports/list";
    }

    // Show create form - admin/reports/create.html
    @GetMapping("/create")
    public String createReportForm(Model model) {
        model.addAttribute("report", new Report());
        model.addAttribute("documents", documentService.getAllDocuments(PageRequest.of(0, 10)));
        return "admin/reports/create";
    }

    // Handle create form submission
    @PostMapping
    public String createReport(@Valid @ModelAttribute Report report,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("documents", documentService.getAllDocuments(PageRequest.of(0, 10)));
            return "admin/reports/create";
        }

        try {
            reportService.createReport(report);
            redirectAttributes.addFlashAttribute("success", "Báo cáo đã được tạo thành công!");
            return "redirect:/admin/reports";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tạo báo cáo: " + e.getMessage());
            model.addAttribute("documents", documentService.getAllDocuments(PageRequest.of(0, 10)));
            return "admin/reports/create";
        }
    }

    // Show report details - admin/reports/details.html
    @GetMapping("/details/{id}")
    public String showReportDetails(@PathVariable Long id, Model model) {
        try {
            Report report = reportService.getReportById(id);
            model.addAttribute("report", report);

            // // Get document details if exists
            // if (report.getDocumentId() != null) {
            // model.addAttribute("document",
            // documentService.getDocumentById(report.getDocumentId()));
            // }

            return "admin/reports/details";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
            return "redirect:/admin/reports";
        }
    }

    // Show simple report view - admin/reports/show.html
    @GetMapping("/show/{id}")
    public String showReport(@PathVariable Long id, Model model) {
        try {
            Report report = reportService.getReportById(id);
            model.addAttribute("report", report);

            // // Get document details if exists
            // if (report.getDocumentId() != null) {
            // model.addAttribute("document",
            // documentService.getDocumentById(report.getDocumentId()));
            // }

            return "admin/reports/show";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
            return "redirect:/admin/reports";
        }
    }

    // Show edit form - admin/reports/edit.html
    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        try {
            Report report = reportService.getReportById(id);
            model.addAttribute("report", report);
            model.addAttribute("documents", documentService.getAllDocuments(PageRequest.of(0, 10)));
            return "admin/reports/edit";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
            return "redirect:/admin/reports";
        }
    }

    // Handle edit form submission - chỉ cập nhật trạng thái
    @PostMapping("/edit/{id}")
    public ResponseEntity<?> updateReport(@PathVariable Long id,
            @RequestParam("status") String status,
            HttpServletRequest request) {
        try {
            // Validate status
            if (!isValidStatus(status)) {
                return ResponseEntity.badRequest().body("Trạng thái không hợp lệ!");
            }

            reportService.updateStatus(id, status);

            // Kiểm tra nếu là AJAX request
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                return ResponseEntity.ok("Trạng thái báo cáo đã được cập nhật thành công!");
            } else {
                // Nếu không phải AJAX, redirect như cũ
                return ResponseEntity.ok().header("Location", "/admin/reports/details/" + id).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Có lỗi xảy ra khi cập nhật báo cáo: " + e.getMessage());
        }
    }

    // Validate status helper method
    private boolean isValidStatus(String status) {
        return status != null && (status.equals("new") ||
                status.equals("pending") ||
                status.equals("resolved") ||
                status.equals("rejected"));
    }

    // Show delete confirmation - admin/reports/delete.html
    @GetMapping("/delete/{id}")
    public String deleteReportForm(@PathVariable Long id, Model model) {
        try {
            Report report = reportService.getReportById(id);
            if (report == null) {
                model.addAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
                return "redirect:/admin/reports";
            }
            model.addAttribute("report", report);
            return "admin/reports/delete";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
            return "redirect:/admin/reports";
        }
    }

    // Handle delete
    @PostMapping("/delete/{id}")
    public String deleteReport(@PathVariable Long id,
            @RequestParam(required = false) String deleteReason,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra báo cáo có tồn tại không
            Report report = reportService.getReportById(id);
            if (report == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy báo cáo với ID: " + id);
                return "redirect:/admin/reports";
            }
            
            // Thực hiện xóa
            reportService.deleteReport(id, deleteReason);
            redirectAttributes.addFlashAttribute("success", 
                "Báo cáo #RPT" + id + " đã được xóa thành công!" + 
                (deleteReason != null && !deleteReason.trim().isEmpty() ? 
                    " Lý do: " + deleteReason.trim() : ""));
            return "redirect:/admin/reports";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Có lỗi xảy ra khi xóa báo cáo #RPT" + id + ": " + e.getMessage());
            return "redirect:/admin/reports/delete/" + id;
        }
    }



    
    // API endpoint for deleting report (AJAX)
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteReportApi(@PathVariable Long id,
            @RequestParam(required = false) String deleteReason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            reportService.deleteReport(id, deleteReason);
            
            response.put("success", true);
            response.put("message", "Báo cáo đã được xóa thành công!");
            response.put("reportId", id);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa báo cáo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API endpoint for getting report details (for AJAX calls)
    @GetMapping("/{id}/api")
    @ResponseBody
    public ResponseEntity<?> getReportDetailApi(@PathVariable Long id) {
        try {
            Map<String, Object> reportDetails = reportService.getReportDetails(id);
            return ResponseEntity.ok(reportDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // API endpoint for processing reports (for AJAX calls)
    @PostMapping("/{id}/process")
    @ResponseBody
    public ResponseEntity<?> processReport(@PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "documentAction", required = false) String documentAction,
            @RequestParam(value = "adminNote", required = false) String adminNote,
            @RequestParam(value = "responseToReporter", required = false) String responseToReporter) {
        try {
            Map<String, Object> result = reportService.processReport(id, status, documentAction, adminNote,
                    responseToReporter);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // API endpoint for updating status only
    @PostMapping("/{id}/update-status")
    @ResponseBody
    public ResponseEntity<?> updateReportStatus(@PathVariable Long id,
            @RequestParam("status") String status) {
        try {
            if (!isValidStatus(status)) {
                return ResponseEntity.badRequest().body("Trạng thái không hợp lệ!");
            }

            reportService.updateStatus(id, status);
            return ResponseEntity.ok().body("Trạng thái đã được cập nhật thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // API endpoint for bulk actions
    @PostMapping("/bulk-action")
    @ResponseBody
    public ResponseEntity<?> bulkAction(@RequestParam("action") String action,
            @RequestParam("reportIds") List<Long> reportIds) {
        try {
            Map<String, Object> result = reportService.bulkAction(action, reportIds);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Export reports to CSV
    @GetMapping("/export")
    public ResponseEntity<String> exportReports(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String violationType,
            @RequestParam(required = false) String status) {
        try {
            List<Map<String, Object>> reports;
            
            // Get reports based on filters
            boolean hasFilters = (keyword != null && !keyword.isBlank()) ||
                               (violationType != null && !violationType.isBlank()) ||
                               (status != null && !status.isBlank());
            
            if (hasFilters) {
                reports = reportService.searchReports(
                    keyword == null || keyword.isBlank() ? null : keyword,
                    violationType == null || violationType.isBlank() ? null : violationType,
                    status == null || status.isBlank() ? null : status);
            } else {
                reports = reportService.getAllReports();
            }

            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Tài liệu,Người báo cáo,Loại vi phạm,Ngày báo cáo,Trạng thái\n");
            
            for (Map<String, Object> report : reports) {
                csv.append("RPT").append(report.get("id")).append(",");
                csv.append("\"").append(report.get("documentTitle")).append("\",");
                csv.append("\"").append(report.get("reporterName")).append("\",");
                csv.append("\"").append(report.get("violationType")).append("\",");
                csv.append("\"").append(report.get("reportDate")).append("\",");
                csv.append("\"").append(report.get("status")).append("\"\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=\"reports_" + 
                            java.time.LocalDate.now() + ".csv\"")
                    .body(csv.toString());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Có lỗi xảy ra khi xuất dữ liệu: " + e.getMessage());
        }
    }
}

