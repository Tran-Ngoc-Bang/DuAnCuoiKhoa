package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.repository.ReportRepository;
import com.fpoly.shared_learning_materials.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ReportController {

    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @Autowired
    public ReportController(ReportService reportService, ReportRepository reportRepository) {
        this.reportService = reportService;

        this.reportRepository = reportRepository;
    }

    @GetMapping("/admin/reports")
    public String viewReports(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) String violationType,
            @RequestParam(required = false) String status, Model model) {

        List<Map<String, Object>> reports = reportRepository.searchReportsWithFilters(
                keyword == null || keyword.isBlank() ? null : keyword,
                violationType == null || violationType.isBlank() ? null : violationType,
                status == null || status.isBlank() ? null : status);
        model.addAttribute("reports", reports);
        model.addAttribute("keyword", keyword);
        model.addAttribute("violationType", violationType);
        model.addAttribute("status", status);

        model.addAttribute("totalReports", reportRepository.countAllReports());

        model.addAttribute("newReportsCount", reportService.getNewReportsCount());
        model.addAttribute("processingReportsCount", reportService.getProcessingReportsCount());
        model.addAttribute("resolvedReportsCount", reportService.getResolvedReportsCount());
        model.addAttribute("rejectedReportsCount", reportService.getRejectedReportsCount());

        // model.addAttribute("reports", reportService.getAllReports());

        System.out.println("Keyword: " + keyword);
        System.out.println("Violation Type: " + violationType);
        System.out.println("Status: " + status);
        System.out.println("Số lượng kết quả: " + reports.size());

        return "admin/reports"; // Trang hiển thị danh sách báo cáo
        // List<Report> reports = reportService.getAllReports();
        // model.addAttribute("reports", reports);

    }

    @GetMapping("/admin/reports/{id}")
    @ResponseBody
    public ResponseEntity<?> getReportDetail(@PathVariable("id") Long id) {
        try {
            Map<String, Object> reportDetails = reportService.getReportDetails(id);
            return ResponseEntity.ok(reportDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/admin/reports/{id}/process")
    @ResponseBody
    public ResponseEntity<?> processReport(@PathVariable("id") Long id,
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
}
