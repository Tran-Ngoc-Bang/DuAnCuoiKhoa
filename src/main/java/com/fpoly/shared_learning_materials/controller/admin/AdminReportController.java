package com.fpoly.shared_learning_materials.controller.admin;

import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.dto.ReportDTO;
import com.fpoly.shared_learning_materials.service.ReportService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public String showReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String keyword,
            Model model) {

        Page<Report> reports = reportService.searchReports(keyword, status, type, page, size);

        model.addAttribute("reportsPage", reports);
        model.addAttribute("totalReports", reportService.countAllReports());
        model.addAttribute("pendingCount", reportService.countPendingReports());
        model.addAttribute("processingCount", reportService.countProcessingReports());
        model.addAttribute("resolvedCount", reportService.countApprovedReports());
        model.addAttribute("rejectedCount", reportService.countRejectedReports());

        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("keyword", keyword);
        model.addAttribute("current", page);
        model.addAttribute("currentPage", "reports");
        model.addAttribute("totalPages", reports.getTotalPages());

        return "admin/reports/index";
    }

    @PostMapping("/update")
    public String updateReport(@RequestParam("reportId") Long reportId,
            @RequestParam(name = "note", required = false) String note,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "reply", required = false) String reply,
            RedirectAttributes ra) {
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setId(reportId);
        reportDTO.setNote(note);
        reportDTO.setStatus(status);
        reportDTO.setReply(reply);
        reportService.updateReport(reportDTO);
        ra.addFlashAttribute("successMessage", "Cập nhật báo cáo thành công.");
        return "redirect:/admin/reports";
    }

    @GetMapping("/{id}/resolve")
    public String showResolvePage(@PathVariable("id") Long reportId, Model model) {
        model.addAttribute("report", reportService.findById(reportId));
        return "admin/reports/resolve";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable("id") Long reportId, RedirectAttributes ra) {
        reportService.approveReport(reportId);
        ra.addFlashAttribute("successMessage", "Đã chấp nhận báo cáo.");
        return "redirect:/admin/reports";
    }

    @GetMapping("/{id}/reject")
    public String showRejectPage(@PathVariable("id") Long reportId, Model model) {
        model.addAttribute("report", reportService.findById(reportId));
        return "admin/reports/reject";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Long reportId, RedirectAttributes ra) {
        reportService.rejectReport(reportId);
        ra.addFlashAttribute("successMessage", "Đã từ chối báo cáo.");
        return "redirect:/admin/reports";
    }

    @GetMapping("/{id}/process")
    public String showProcessPage(@PathVariable("id") Long reportId, Model model) {
        model.addAttribute("report", reportService.findById(reportId));
        return "admin/reports/process";
    }

    @PostMapping("/{id}/process")
    public String process(@PathVariable("id") Long reportId, RedirectAttributes ra) {
        reportService.processReport(reportId);
        ra.addFlashAttribute("successMessage", "Báo cáo đã chuyển sang trạng thái Đang xử lý.");
        return "redirect:/admin/reports";
    }

    @GetMapping("/{id}/reopen")
    public String showReopenPage(@PathVariable("id") Long reportId, Model model) {
        model.addAttribute("report", reportService.findById(reportId));
        return "admin/reports/reopen";
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable("id") Long reportId, RedirectAttributes ra) {
        reportService.reopenReport(reportId);
        ra.addFlashAttribute("successMessage", "Báo cáo đã được mở lại.");
        return "redirect:/admin/reports";
    }
}
