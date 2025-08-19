package com.fpoly.shared_learning_materials.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fpoly.shared_learning_materials.domain.Report;
import com.fpoly.shared_learning_materials.dto.ReportDTO;
import com.fpoly.shared_learning_materials.repository.ReportRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ReportService {

	@Autowired
	private ReportRepository reportRepository;

	public Page<Report> getReportsByStatus(String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		if ("all".equalsIgnoreCase(status)) {
			return reportRepository.findAll(pageable);
		}
		return reportRepository.findByStatusIgnoreCase(status, pageable);
	}

	public Page<Report> searchReports(String keyword, String status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		if ("all".equalsIgnoreCase(status)) {
			return reportRepository.searchByKeyword(keyword, pageable);
		}
		return reportRepository.searchByKeywordAndStatus(keyword, status, pageable);
	}

	public Report findById(Long id) {
		return reportRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo với ID: " + id));
	}

	public void approveReport(Long reportId) {
		reportRepository.findById(reportId).ifPresent(report -> {
			report.setStatus("resolved");
			report.setReviewedAt(LocalDateTime.now());
			reportRepository.save(report);
		});
	}

	public void rejectReport(Long reportId) {
		reportRepository.findById(reportId).ifPresent(report -> {
			report.setStatus("rejected");
			report.setReviewedAt(LocalDateTime.now());
			reportRepository.save(report);
		});
	}

	public void processReport(Long reportId) {
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new RuntimeException("Report not found"));
		report.setStatus("processing");
		reportRepository.save(report);
	}

	public void reopenReport(Long reportId) {
		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new RuntimeException("Report not found"));
		report.setStatus("pending");
		reportRepository.save(report);
	}

	public long countAllReports() {
		return reportRepository.count();
	}

	public long countPendingReports() {
		return reportRepository.countByStatusIgnoreCase("pending");
	}

	public long countProcessingReports() {
		return reportRepository.countByStatusIgnoreCase("processing");
	}

	public long countApprovedReports() {
		return reportRepository.countByStatusIgnoreCase("resolved");
	}

	public long countRejectedReports() {
		return reportRepository.countByStatusIgnoreCase("rejected");
	}

	public void updateReport(ReportDTO dto) {
		Report report = reportRepository.findById(dto.getId())
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy báo cáo với ID: " + dto.getId()));

		report.setStatus(dto.getStatus());
		report.setNote(dto.getNote());
		report.setReply(dto.getReply());
		report.setReviewedAt(LocalDateTime.now());
		reportRepository.save(report);
	}

	public Page<Report> getReportsFiltered(String status, String type, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		if ("all".equals(status) && "all".equals(type)) {
			return reportRepository.findAll(pageable);
		} else if (!"all".equals(status) && "all".equals(type)) {
			return reportRepository.findByStatus(status, pageable);
		} else if ("all".equals(status) && !"all".equals(type)) {
			return reportRepository.findByType(type, pageable);
		} else {
			return reportRepository.findByStatusAndType(status, type, pageable);
		}
	}

	public Page<Report> searchReports(String keyword, String status, String type, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return reportRepository.searchReports(keyword, status, type, pageable);
	}

}
