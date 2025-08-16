package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.repository.ReportRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public void updateStatus(Long id, String status) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));

        String oldStatus = report.getStatus();
        report.setStatus(status);

        // Tự động cập nhật reviewedAt nếu chuyển từ new/pending sang resolved/rejected
        if (("new".equals(oldStatus) || "pending".equals(oldStatus)) &&
                ("resolved".equals(status) || "rejected".equals(status))) {
            report.setReviewedAt(LocalDateTime.now());
        }

        reportRepository.save(report);
    }

    @Transactional
    public void updateReason(Long id, String reason) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy báo cáo"));
        report.setReason(reason);
        reportRepository.save(report);
    }
    // // Lấy tất cả báo cáo
    // public List<Report> getAllReports() {
    // return reportRepository.findAll();
    // }

    // Lấy báo cáo theo ID
    public Report getReportById(Long id) {
        Optional<Report> report = reportRepository.findReportWithDetailsById(id);
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

    // Thêm method đếm theo trạng thái cụ thể
    public long getReportCountByStatus(String status) {
        return reportRepository.countByStatus(status);
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
            // Debug dữ liệu thô từ database
            System.out.println("Raw data - ID: " + row[0] + 
                             ", Title: " + row[1] + 
                             ", Reporter: " + row[2] + 
                             ", Type: " + row[3] + 
                             ", Date: " + row[4] + 
                             ", Status: " + row[5]);
            
            Map<String, Object> report = new HashMap<>();
            report.put("id", row[0]);
            report.put("documentTitle", row[1] != null ? row[1].toString() : "Bình luận");
            report.put("reporterName", row[2] != null ? row[2].toString() : "Người dùng không xác định");
            
            // Xử lý loại vi phạm với encoding đúng
            String rawType = row[3] != null ? row[3].toString() : "other";
            String violationType = formatViolationType(rawType);
            report.put("violationType", violationType);
            
            report.put("reportDate", row[4]);
            
            // Xử lý trạng thái với encoding đúng
            String rawStatus = row[5] != null ? row[5].toString() : "pending";
            String status = formatStatus(rawStatus);
            report.put("status", status);
            
            System.out.println("Processed - Report ID: " + row[0] + 
                             ", Raw Type: '" + rawType + "'" +
                             ", Formatted Type: '" + violationType + "'" +
                             ", Raw Status: '" + rawStatus + "'" +
                             ", Formatted Status: '" + status + "'");

            reports.add(report);
        }

        return reports;
    }

    public Page<Map<String, Object>> getAllReportsWithPagination(Pageable pageable) {
        Page<Object[]> results = reportRepository.findAllReportsWithDetails(pageable);

        List<Map<String, Object>> reports = new ArrayList<>();
        for (Object[] row : results.getContent()) {
            Map<String, Object> report = new HashMap<>();
            report.put("id", row[0]);
            report.put("documentTitle", row[1] != null ? row[1].toString() : "Bình luận");
            report.put("reporterName", row[2] != null ? row[2].toString() : "Người dùng không xác định");
            
            // Xử lý loại vi phạm với encoding đúng
            String violationType = formatViolationType(row[3] != null ? row[3].toString() : "other");
            report.put("violationType", violationType);
            
            report.put("reportDate", row[4]);
            
            // Xử lý trạng thái với encoding đúng
            String status = formatStatus(row[5] != null ? row[5].toString() : "pending");
            report.put("status", status);
            
            reports.add(report);
        }

        return new PageImpl<>(reports, pageable, results.getTotalElements());
    }

    public List<Map<String, Object>> searchReports(String keyword, String violationType, String status) {
        List<Object[]> results = reportRepository.searchReportsWithFilters(keyword, violationType, status);
        
        List<Map<String, Object>> reports = new ArrayList<>();
        for (Object[] row : results) {
            // Debug dữ liệu thô từ search
            System.out.println("Search Raw data - ID: " + row[0] + 
                             ", Title: " + row[1] + 
                             ", Reporter: " + row[2] + 
                             ", Type: " + row[3] + 
                             ", Date: " + row[4] + 
                             ", Status: " + row[5]);
            
            Map<String, Object> report = new HashMap<>();
            report.put("id", row[0]);
            report.put("documentTitle", row[1] != null ? row[1].toString() : "Bình luận");
            report.put("reporterName", row[2] != null ? row[2].toString() : "Người dùng không xác định");
            
            // Xử lý loại vi phạm với encoding đúng
            String rawType = row[3] != null ? row[3].toString() : "other";
            String formattedViolationType = formatViolationType(rawType);
            report.put("violationType", formattedViolationType);
            
            report.put("reportDate", row[4]);
            
            // Xử lý trạng thái với encoding đúng
            String rawStatus = row[5] != null ? row[5].toString() : "pending";
            String formattedStatus = formatStatus(rawStatus);
            report.put("status", formattedStatus);
            
            System.out.println("Search Processed - Report ID: " + row[0] + 
                             ", Raw Type: '" + rawType + "'" +
                             ", Formatted Type: '" + formattedViolationType + "'" +
                             ", Raw Status: '" + rawStatus + "'" +
                             ", Formatted Status: '" + formattedStatus + "'");

            reports.add(report);
        }
        
        return reports;
    }

    public Map<String, Object> getReportDetails(Long id) {
        return reportRepository.findReportDetailsById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with ID: " + id));
    }

    // Tạo báo cáo mới
    public Report createReport(Report report) {
        // createdAt sẽ được set tự động bởi @PrePersist
        if (report.getStatus() == null || report.getStatus().isEmpty()) {
            report.setStatus("pending");
        }
        return reportRepository.save(report);
    }

    // Cập nhật báo cáo
    public Report updateReport(Report report) {
        Optional<Report> existingReportOpt = reportRepository.findById(report.getId());
        if (existingReportOpt.isEmpty()) {
            throw new RuntimeException("Report not found with ID: " + report.getId());
        }

        Report existingReport = existingReportOpt.get();
        String oldStatus = existingReport.getStatus();

        // Cập nhật các trường theo Report domain hiện tại
        if (report.getDocument() != null) {
            existingReport.setDocument(report.getDocument());
        }
        if (report.getComment() != null) {
            existingReport.setComment(report.getComment());
        }
        existingReport.setType(report.getType());
        existingReport.setReason(report.getReason());
        existingReport.setStatus(report.getStatus());
        if (report.getReviewer() != null) {
            existingReport.setReviewer(report.getReviewer());
        }

        // Tự động cập nhật reviewedAt nếu status thay đổi từ pending sang
        // approved/rejected
        if (!report.getStatus().equals(oldStatus) &&
                ("approved".equals(report.getStatus()) || "rejected".equals(report.getStatus()))) {
            if (report.getReviewedAt() == null) {
                existingReport.setReviewedAt(LocalDateTime.now());
            } else {
                existingReport.setReviewedAt(report.getReviewedAt());
            }
        } else if (report.getReviewedAt() != null) {
            existingReport.setReviewedAt(report.getReviewedAt());
        }

        return reportRepository.save(existingReport);
    }

    // Xóa báo cáo
    public void deleteReport(Long id, String deleteReason) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isEmpty()) {
            throw new RuntimeException("Report not found with ID: " + id);
        }

        // TODO: Log delete reason if needed
        // Có thể lưu lý do xóa vào audit log

        reportRepository.deleteById(id);
    }

    // Bulk actions
    public Map<String, Object> bulkAction(String action, List<Long> reportIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Long reportId : reportIds) {
            try {
                Optional<Report> reportOpt = reportRepository.findById(reportId);
                if (reportOpt.isPresent()) {
                    Report report = reportOpt.get();

                    switch (action) {
                        case "new":
                            report.setStatus("new");
                            break;
                        case "pending":
                            report.setStatus("pending");
                            break;
                        case "resolve":
                        case "resolved":
                            report.setStatus("resolved");
                            report.setReviewedAt(LocalDateTime.now());
                            break;
                        case "reject":
                        case "rejected":
                            report.setStatus("rejected");
                            report.setReviewedAt(LocalDateTime.now());
                            break;
                        default:
                            throw new RuntimeException("Unknown action: " + action);
                    }

                    reportRepository.save(report);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                failCount++;
            }
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message",
                String.format("Đã xử lý %d báo cáo thành công, %d báo cáo thất bại", successCount, failCount));

        return result;
    }



    public boolean deleteReportById(Long id, String deleteReason) {
        Optional<Report> optional = reportRepository.findById(id);
        if (optional.isPresent()) {
            // Có thể lưu lại lý do xoá vào log hoặc bảng khác
            reportRepository.deleteById(id);
            return true;
        }
        return false;
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
    
    private String formatViolationType(String type) {
        switch (type) {
            case "inappropriate":
                return "Nội dung không phù hợp";
            case "spam":
                return "Spam";
            case "copyright":
                return "Vi phạm bản quyền";
            case "fake":
                return "Thông tin sai lệch";
            case "other":
                return "Khác";
            default:
                return "Không xác định";
        }
    }
    
    private String formatStatus(String status) {
        switch (status) {
            case "pending":
                return "Đang chờ xử lý";
            case "resolved":
                return "Đã xử lý";
            case "rejected":
                return "Đã từ chối";
            case "new":
                return "Mới";
            default:
                return "Không có";
        }
    }

}
