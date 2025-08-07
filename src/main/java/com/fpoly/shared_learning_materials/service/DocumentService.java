package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Document;
import com.fpoly.shared_learning_materials.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class DocumentService {

    public List<Long> getMonthlyUploadCounts() {
        int year = LocalDate.now().getYear();
        List<Object[]> results = documentRepository.countMonthlyUploads(year);

        Map<Integer, Long> monthCountMap = new HashMap<>();
        for (Object[] result : results) {
            Integer month = (Integer) result[0];
            Long count = (Long) result[1];
            monthCountMap.put(month, count);
        }

        // Trả về danh sách 12 tháng, nếu không có dữ liệu thì là 0
        List<Long> uploadCounts = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            uploadCounts.add(monthCountMap.getOrDefault(i, 0L));
        }

        return uploadCounts;
    }

    private final DocumentRepository documentRepository;

    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public long getNewDocumentsBetween(LocalDateTime start, LocalDateTime end) {
        return documentRepository.countNewDocumentsBetween(start, end);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public long getTotalDocuments() {
        return documentRepository.count();
    }

    public long getNewDocumentsThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return documentRepository.countByCreatedAtAfter(startOfMonth);
    }

    public List<Document> getTopDocumentsByDownloads(int limit) {
        return documentRepository.findTopByOrderByDownloadsCountDesc(limit);
    }

    public long getTotalDownloads() {
        return documentRepository.sumDownloadsCount();
    }

}
