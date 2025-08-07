package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    // // Lấy tất cả báo cáo
    // public List<Report> getAllReports() {
    // return reportRepository.findAll();
    // }

    // Lấy báo cáo theo ID
    public Report getReportById(Long id) {
        Optional<Report> report = reportRepository.findById(id);
        return report.orElse(null); // Trả về null nếu không tìm thấy báo cáo
    }

    public long getCount() {
        return reportRepository.countAllReports();
    }

    // Cập nhật trạng thái của báo cáo
    public Report updateReportStatus(Long id, String status) {
        Optional<Report> report = reportRepository.findById(id);
        if (report.isPresent()) {
            Report existingReport = report.get();
            existingReport.setStatus(status);
            return reportRepository.save(existingReport);
        }
        return null; // Nếu báo cáo không tồn tại
    }

    public long getNewReportsCount() {
        return reportRepository.countNewReports();
    }

    public long getProcessingReportsCount() {
        return reportRepository.countProcessingReports();
    }

    public long getResolvedReportsCount() {
        return reportRepository.countResolvedReports();
    }

    public long getRejectedReportsCount() {
        return reportRepository.countRejectedReports();
    }

    // public Report getReportDetails(Long id) {
    // return reportRepository.findReportWithDetailsById(id)
    // .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    // }

    public List<Map<String, Object>> getAllReports() {
        List<Object[]> results = reportRepository.findAllReportsWithDetails();
        System.out.println("Số bản ghi lấy được: " + results.size()); // ← debug dòng này

        List<Map<String, Object>> reports = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> report = new HashMap<>();
            report.put("id", row[0]);
            report.put("documentTitle", row[1]);
            report.put("reporterName", row[2]);
            report.put("violationType", row[3]);
            report.put("reportDate", row[4]);
            report.put("status", row[5]);

            reports.add(report);
        }

        return reports;
    }

    public List<Map<String, Object>> searchReports(String keyword, String violationType, String status) {
        return reportRepository.searchReportsWithFilters(keyword, violationType, status);
    }

    public Map<String, Object> getReportDetails(Long id) {
        return reportRepository.findReportDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }

    public Map<String, Object> processReport(Long id, String status, String documentAction, String adminNote,
            String responseToReporter) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Report not found with ID: " + id);
        }

        Report report = reportOpt.get();

        // Cập nhật trạng thái báo cáo
        report.setStatus(status);
        report.setReviewedAt(LocalDateTime.now());

        // TODO: Set reviewer (cần lấy từ session hoặc authentication context)
        // report.setReviewer(currentUser);

        // Lưu báo cáo
        reportRepository.save(report);

        // TODO: Xử lý hành động với tài liệu (documentAction)
        // - hide: Ẩn tài liệu
        // - delete: Xóa tài liệu
        // - warning: Cảnh báo người đăng

        // TODO: Lưu ghi chú admin (adminNote) - có thể cần tạo bảng riêng

        // TODO: Gửi phản hồi tới người báo cáo (responseToReporter)
        // - Có thể gửi email hoặc notification

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Báo cáo đã được xử lý thành công");
        result.put("reportId", id);
        result.put("newStatus", status);

        return result;
    }

}
