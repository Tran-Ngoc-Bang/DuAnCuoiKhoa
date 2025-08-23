package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.*;
import com.fpoly.shared_learning_materials.dto.CommentDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.repository.*;
import com.fpoly.shared_learning_materials.config.UploadConfig;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentCategoryRepository documentCategoryRepository;

    @Autowired
    private DocumentTagRepository documentTagRepository;

    @Autowired
    private DocumentOwnerRepository documentOwnerRepository;

    @Autowired
    private UploadConfig uploadConfig;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private FileService fileService;

    // Get total documents count (excluding deleted ones)
    public long getTotalDocuments() {
        return documentRepository.countByDeletedAtIsNull();
    }

    // Export all documents as JSON string for backup

    // Import documents from backup file

    // Helper method to escape JSON strings
    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Get document by ID
    public DocumentDTO getDocumentById(Long id) {
        // System.out.println("=== GET DOCUMENT BY ID SERVICE ===");
        // System.out.println("Getting document with ID: " + id);

        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            System.out.println("Document not found with ID: " + id);
            return null;
        }

        Document document = documentOpt.get();
        // System.out.println("Found document: " + document.getTitle());
        // System.out.println("Document deletedAt: " + document.getDeletedAt());

        // Convert to DTO
        DocumentDTO dto = convertToDTO(document);

        // Set additional fields - using DocumentOwner relationship
        List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(document.getId());
        if (!owners.isEmpty()) {
            dto.setAuthorName(owners.get(0).getUser().getFullName() != null ? owners.get(0).getUser().getFullName()
                    : owners.get(0).getUser().getUsername());
        }

        // Get category names
        List<DocumentCategory> documentCategories = documentCategoryRepository.findByDocumentId(id);
        List<String> categoryNames = documentCategories.stream()
                .map(dc -> dc.getCategory().getName())
                .collect(Collectors.toList());
        dto.setCategoryNames(categoryNames);

        // Get category IDs
        List<Long> categoryIds = documentCategories.stream()
                .map(dc -> dc.getCategory().getId())
                .collect(Collectors.toList());
        dto.setCategoryIds(categoryIds);

        // Get tag names
        List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(id);
        List<String> tagNames = documentTags.stream()
                .map(dt -> dt.getTag().getName())
                .collect(Collectors.toList());
        dto.setTagNames(tagNames);

        // Load comments for this document from database
        try {
            List<Comment> comments = commentRepository.findByDocumentIdAndStatus(id, "active");
            List<CommentDTO> commentDTOs = new ArrayList<>();

            for (Comment comment : comments) {
                CommentDTO commentDTO = new CommentDTO();
                commentDTO.setId(comment.getId());
                commentDTO.setContent(comment.getContent());
                commentDTO.setStatus(comment.getStatus());
                commentDTO.setCreatedAt(comment.getCreatedAt());
                commentDTO.setRating(comment.getRating());

                // Set user info
                if (comment.getUser() != null) {
                    commentDTO.setUserName(comment.getUser().getFullName() != null ? comment.getUser().getFullName()
                            : comment.getUser().getUsername());
                    // Set avatar if available (placeholder for now)
                    commentDTO.setUserAvatar("https://via.placeholder.com/40");
                } else {
                    commentDTO.setUserName("Người dùng ẩn danh");
                    commentDTO.setUserAvatar("https://via.placeholder.com/40");
                }

                // Check if comment has been reported
                try {
                    List<Report> reports = reportRepository.findByCommentId(comment.getId());
                    if (!reports.isEmpty()) {
                        // Get the latest report status
                        commentDTO.setReportStatus(reports.get(reports.size() - 1).getStatus());
                    }
                } catch (Exception e) {
                    System.out.println(
                            "Could not load report status for comment " + comment.getId() + ": " + e.getMessage());
                }

                commentDTOs.add(commentDTO);
            }

            dto.setComments(commentDTOs);
            // System.out.println("Comments loaded: " + commentDTOs.size() + " (from
            // database)");
        } catch (Exception e) {
            System.out.println("Could not load comments: " + e.getMessage());
            e.printStackTrace();
            dto.setComments(new ArrayList<>());
        }

        System.out.println("Document DTO prepared with categories: " + categoryNames + ", tags: " + tagNames);
        return dto;
    }

    // Convert Document entity to DTO
    private DocumentDTO convertToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setSlug(document.getSlug());
        dto.setDescription(document.getDescription());
        dto.setPrice(document.getPrice());
        dto.setStatus(document.getStatus());
        dto.setVisibility(document.getVisibility());
        dto.setDownloadsCount(document.getDownloadsCount());
        dto.setViewsCount(document.getViewsCount());
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setPublishedAt(document.getPublishedAt());
        dto.setDeletedAt(document.getDeletedAt()); // Thêm deletedAt

        // Set file info if exists
        if (document.getFile() != null) {
            dto.setFileId(document.getFile().getId());
            dto.setFileName(document.getFile().getFileName());
            dto.setFilePath(document.getFile().getFilePath()); // Add filePath to DTO
            // Convert bytes to KB (divide by 1024)
            double fileSizeInKB = document.getFile().getFileSize().doubleValue() / 1024.0;
            dto.setFileSize(Math.round(fileSizeInKB * 10.0) / 10.0); // Round to 1 decimal place
            dto.setFileType(document.getFile().getFileType());

            // Debug: log file info
            // System.out.println("File info from DB:");
            // System.out.println("- File ID: " + document.getFile().getId());
            // System.out.println("- File Name: " + document.getFile().getFileName());
            // System.out.println("- File Path: " + document.getFile().getFilePath());
            // System.out.println("- File Type: " + document.getFile().getFileType());
        }

        // Calculate average rating and rating distribution
        try {
            List<Comment> comments = commentRepository.findByDocumentIdAndStatus(document.getId(), "active");
            if (!comments.isEmpty()) {
                // Calculate average rating
                double totalRating = comments.stream()
                        .mapToDouble(comment -> comment.getRating() != null ? comment.getRating() : 0.0)
                        .sum();
                double averageRating = totalRating / comments.size();
                dto.setAverageRating(Math.round(averageRating * 10.0) / 10.0); // Round to 1 decimal

                // Calculate rating distribution
                Map<String, Integer> distribution = new HashMap<>();
                for (int i = 1; i <= 5; i++) {
                    final int rating = i;
                    long count = comments.stream()
                            .filter(comment -> comment.getRating() != null && comment.getRating() == rating)
                            .count();
                    int percentage = (int) Math.round((double) count / comments.size() * 100);
                    distribution.put(String.valueOf(i), percentage);
                }
                dto.setRatingDistribution(distribution);
            } else {
                dto.setAverageRating(0.0);
                dto.setRatingDistribution(new HashMap<>());
            }
        } catch (Exception e) {
            System.out.println("Error calculating ratings: " + e.getMessage());
            dto.setAverageRating(0.0);
            dto.setRatingDistribution(new HashMap<>());
        }

        return dto;
    }

    // Soft delete document
    @Transactional
    public void deleteDocument(Long id) {
        System.out.println("=== SOFT DELETE DOCUMENT SERVICE ===");
        System.out.println("Soft deleting document with ID: " + id);

        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + id);
        }

        Document document = documentOpt.get();
        System.out.println("Found document: " + document.getTitle());

        // Check if document is already deleted
        if (document.getDeletedAt() != null) {
            System.out.println("Document is already deleted at: " + document.getDeletedAt());
            throw new IllegalStateException("Tài liệu đã được xóa trước đó");
        }

        try {
            // Soft delete: set deleted_at và status thành DELETED
            LocalDateTime deleteTime = LocalDateTime.now();
            System.out.println("Setting deletedAt to: " + deleteTime);

            // Check current state
            System.out.println("Current document state:");
            System.out.println("- ID: " + document.getId());
            System.out.println("- Title: " + document.getTitle());
            System.out.println("- Current status: " + document.getStatus());
            System.out.println("- Current deletedAt: " + document.getDeletedAt());

            document.setDeletedAt(deleteTime);
            document.setStatus("DELETED"); // Cập nhật status thành DELETED
            System.out.println("Before save - deletedAt: " + document.getDeletedAt());
            System.out.println("Before save - status: " + document.getStatus());

            // Force flush to database
            Document savedDocument = documentRepository.saveAndFlush(document);
            System.out.println("After saveAndFlush - deletedAt: " + savedDocument.getDeletedAt());

            // Verify in database with fresh query
            documentRepository.flush();
            Optional<Document> verifyDoc = documentRepository.findById(id);
            if (verifyDoc.isPresent()) {
                System.out.println("Verification - deletedAt in DB: " + verifyDoc.get().getDeletedAt());
            } else {
                System.out.println("ERROR: Document not found after save!");
            }

            System.out.println("Document soft deleted successfully at: " + document.getDeletedAt());
        } catch (Exception e) {
            System.err.println("Error during document soft deletion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa tài liệu: " + e.getMessage(), e);
        }
    }

    // Restore document
    @Transactional
    public void restoreDocument(Long id) {
        System.out.println("=== RESTORE DOCUMENT SERVICE ===");
        System.out.println("Restoring document with ID: " + id);

        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + id);
        }

        Document document = documentOpt.get();
        System.out.println("Found document: " + document.getTitle());

        try {
            // Restore: xóa deleted_at và khôi phục status về APPROVED
            document.setDeletedAt(null);
            document.setStatus("APPROVED"); // Khôi phục status về APPROVED
            documentRepository.save(document);

            System.out.println("Document restored successfully - Status: " + document.getStatus());
        } catch (Exception e) {
            System.err.println("Error during document restoration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi khôi phục tài liệu: " + e.getMessage(), e);
        }
    }

    // Permanent delete document
    @Transactional
    public void permanentDeleteDocument(Long id) {
        System.out.println("=== PERMANENT DELETE DOCUMENT SERVICE ===");
        System.out.println("Permanently deleting document with ID: " + id);

        Optional<Document> documentOpt = documentRepository.findById(id);
        if (!documentOpt.isPresent()) {
            throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + id);
        }

        Document document = documentOpt.get();
        System.out.println("Found document: " + document.getTitle());

        // Kiểm tra xem tài liệu đã bị xóa mềm chưa
        if (document.getDeletedAt() == null) {
            throw new IllegalArgumentException("Chỉ có thể xóa vĩnh viễn tài liệu đã bị xóa mềm");
        }

        try {
            System.out.println("Step 1: Finding and deleting document categories...");
            // Tìm và xóa các liên kết với categories bằng JPA
            List<DocumentCategory> documentCategories = documentCategoryRepository.findByDocumentId(id);
            System.out.println("Found " + documentCategories.size() + " document categories to delete");
            for (DocumentCategory dc : documentCategories) {
                documentCategoryRepository.delete(dc);
                System.out.println("Deleted document category: " + dc.getId());
            }
            System.out.println("Step 1: Completed - Categories deleted");

            System.out.println("Step 2: Finding and deleting document tags...");
            // Tìm và xóa các liên kết với tags bằng JPA
            List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(id);
            System.out.println("Found " + documentTags.size() + " document tags to delete");
            for (DocumentTag dt : documentTags) {
                documentTagRepository.delete(dt);
                System.out.println("Deleted document tag: " + dt.getId());
            }
            System.out.println("Step 2: Completed - Tags deleted");

            System.out.println("Step 3: Deleting document owners...");
            // Xóa document owners
            List<DocumentOwner> documentOwners = documentOwnerRepository.findByDocumentId(id);
            System.out.println("Found " + documentOwners.size() + " document owners to delete");
            for (DocumentOwner owner : documentOwners) {
                documentOwnerRepository.delete(owner);
                System.out.println("Deleted document owner: " + owner.getId());
            }
            System.out.println("Step 3: Completed - Document owners deleted");

            System.out.println("Step 4: Deleting comments...");
            // Xóa comments liên quan (tất cả status)
            List<Comment> comments = commentRepository.findByDocumentIdAndStatusIn(id,
                    Arrays.asList("APPROVED", "PENDING", "REJECTED"), PageRequest.of(0, 1000)).getContent();
            System.out.println("Found " + comments.size() + " comments to delete");
            for (Comment comment : comments) {
                commentRepository.delete(comment);
                System.out.println("Deleted comment: " + comment.getId());
            }
            System.out.println("Step 4: Completed - Comments deleted");

            System.out.println("Step 5: Deleting reports...");
            // Xóa reports liên quan - tìm và xóa thủ công
            try {
                List<Report> reports = reportRepository.findAll().stream()
                        .filter(r -> r.getDocument() != null && r.getDocument().getId().equals(id))
                        .collect(Collectors.toList());
                System.out.println("Found " + reports.size() + " reports to delete");
                for (Report report : reports) {
                    reportRepository.delete(report);
                    System.out.println("Deleted report: " + report.getId());
                }
                System.out.println("Step 5: Completed - Reports deleted");
            } catch (Exception e) {
                System.out.println("Step 5: No reports to delete or error: " + e.getMessage());
            }

            // Xóa file vật lý nếu có
            if (document.getFile() != null && document.getFile().getFilePath() != null
                    && !document.getFile().getFilePath().isEmpty()) {
                try {
                    System.out.println("Step 6: Deleting physical file...");
                    fileService.deleteFile(document.getFile().getFilePath());
                    System.out
                            .println("Step 6: Completed - Physical file deleted: " + document.getFile().getFilePath());
                } catch (Exception e) {
                    System.err.println("Warning: Could not delete physical file: " + e.getMessage());
                    // Không throw exception vì việc xóa file có thể thất bại nhưng vẫn muốn xóa
                    // record
                }
            }

            System.out.println("Step 7: Deleting document from database...");
            System.out.println("Document ID before delete: " + document.getId());
            System.out.println("Document title before delete: " + document.getTitle());

            // Xóa document khỏi database
            documentRepository.delete(document);
            documentRepository.flush();

            System.out.println("Step 7: Completed - Document deleted from database");

            // Kiểm tra xem document có còn tồn tại không
            Optional<Document> checkDocument = documentRepository.findById(id);
            if (checkDocument.isPresent()) {
                System.err.println("ERROR: Document still exists after deletion!");
                throw new RuntimeException("Document was not deleted from database");
            } else {
                System.out.println("VERIFIED: Document successfully deleted from database");
            }

            System.out.println("Transaction completed - Document permanently deleted successfully");
        } catch (Exception e) {
            System.err.println("Error during permanent document deletion: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa vĩnh viễn tài liệu: " + e.getMessage(), e);
        }
    }

    // Get deleted documents
    public Page<DocumentDTO> getDeletedDocuments(Pageable pageable) {
        System.out.println("=== GET DELETED DOCUMENTS SERVICE ===");
        System.out.println("Pageable - Page: " + pageable.getPageNumber() + ", Size: " + pageable.getPageSize());
        System.out.println("Sort: " + pageable.getSort());

        // Use the provided Pageable with its sort settings
        Page<Document> deletedDocuments = documentRepository.findByDeletedAtIsNotNull(pageable);

        List<DocumentDTO> dtoList = deletedDocuments.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Set additional fields for each DTO
        for (DocumentDTO dto : dtoList) {
            setAdditionalFields(dto);
        }

        System.out.println("Found " + dtoList.size() + " deleted documents on page " + pageable.getPageNumber() +
                ", total: " + deletedDocuments.getTotalElements());
        return new PageImpl<>(dtoList, pageable, deletedDocuments.getTotalElements());
    }

    // Get filtered deleted documents (with all filters)
    public Page<DocumentDTO> getFilteredDeletedDocuments(
            Pageable pageable, String search, String status, Long categoryId,
            String type, String price, String author, String tags,
            String dateFrom, String dateTo, String size, String views) {

        System.out.println("=== GET FILTERED DELETED DOCUMENTS SERVICE ===");
        System.out.println("Filters - Search: " + search + ", Status: " + status + ", CategoryId: " + categoryId);
        System.out.println("Pageable - Page: " + pageable.getPageNumber() + ", Size: " + pageable.getPageSize());

        // For deleted documents, always use in-memory filtering if there are filters or
        // sorting
        // This ensures all sort fields work properly
        boolean hasFiltersOrSort = !isNoFiltersApplied(search, status, categoryId, type, price, author, tags, dateFrom,
                dateTo, size, views)
                || !pageable.getSort().isUnsorted();

        if (!hasFiltersOrSort) {
            System.out.println(
                    "No filters and no sorting applied, using direct database pagination for deleted documents");
            return getDeletedDocuments(pageable);
        }

        // Nếu có filter, lấy tất cả deleted documents rồi filter
        System.out.println("Filters applied, using in-memory filtering for deleted documents");

        // Debug: Check total count first
        long totalDeletedCount = documentRepository.countByDeletedAtIsNotNull();
        System.out.println("Total deleted documents in DB: " + totalDeletedCount);

        // Use query without hardcoded sort to avoid conflict with Pageable sort
        List<Document> allDeletedDocuments = documentRepository.findByDeletedAtIsNotNull();
        System.out.println("Retrieved deleted documents from query: " + allDeletedDocuments.size());

        List<DocumentDTO> allDtoList = allDeletedDocuments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Set additional fields for each DTO
        for (DocumentDTO dto : allDtoList) {
            setAdditionalFields(dto);
        }

        // Apply filters to the list (sử dụng cùng logic filter như active documents)
        List<DocumentDTO> filteredList = allDtoList.stream()
                .filter(dto -> applyFilters(dto, search, status, categoryId, type, price, author, tags, dateFrom,
                        dateTo, size, views))
                .collect(Collectors.toList());

        System.out.println(
                "Found " + allDtoList.size() + " deleted documents, " + filteredList.size() + " after filtering");

        // Apply sorting (including author sort)
        filteredList = applySortingFromPageable(filteredList, pageable);

        // Create pageable result from filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());

        // Handle case where start is beyond the filtered list size
        if (start >= filteredList.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredList.size());
        }

        List<DocumentDTO> pageContent = filteredList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredList.size());
    }

    // Get filtered documents (active only)
    public Page<DocumentDTO> getFilteredDocuments(
            Pageable pageable, String search, String status, Long categoryId,
            String type, String price, String author, String tags,
            String dateFrom, String dateTo, String size, String views) {

        System.out.println("=== GET FILTERED DOCUMENTS SERVICE (ACTIVE ONLY) ===");
        System.out.println("Filters - Search: " + search + ", Status: " + status + ", CategoryId: " + categoryId);
        System.out.println("Pageable - Page: " + pageable.getPageNumber() + ", Size: " + pageable.getPageSize());

        // Check if we need special handling for author sort
        boolean needsAuthorSort = pageable.getSort().stream()
                .anyMatch(order -> "author".equalsIgnoreCase(order.getProperty()));

        // Nếu không có filter nào và không sort by author, sử dụng pagination trực tiếp
        // từ database
        if (isNoFiltersApplied(search, status, categoryId, type, price, author, tags, dateFrom, dateTo, size, views)
                && !needsAuthorSort) {
            System.out.println("No filters applied and no author sort, using direct database pagination");
            Page<Document> activeDocuments = documentRepository.findByDeletedAtIsNull(pageable);

            List<DocumentDTO> dtoList = activeDocuments.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Set additional fields for each DTO
            for (DocumentDTO dto : dtoList) {
                setAdditionalFields(dto);
            }

            System.out.println("Found " + dtoList.size() + " documents on page " + pageable.getPageNumber() +
                    ", total: " + activeDocuments.getTotalElements());

            return new PageImpl<>(dtoList, pageable, activeDocuments.getTotalElements());
        }

        // Nếu có filter, lấy tất cả rồi filter (tạm thời - cần optimize sau)
        System.out.println("Filters applied, using in-memory filtering");
        List<Document> allActiveDocuments = documentRepository.findByDeletedAtIsNullOrderByCreatedAtDesc();

        List<DocumentDTO> allDtoList = allActiveDocuments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Set additional fields for each DTO
        for (DocumentDTO dto : allDtoList) {
            setAdditionalFields(dto);
        }

        // Apply filters to the list
        List<DocumentDTO> filteredList = allDtoList.stream()
                .filter(dto -> applyFilters(dto, search, status, categoryId, type, price, author, tags, dateFrom,
                        dateTo, size, views))
                .collect(Collectors.toList());

        System.out.println(
                "Found " + allDtoList.size() + " active documents, " + filteredList.size() + " after filtering");

        // Apply sorting (including author sort)
        filteredList = applySortingFromPageable(filteredList, pageable);

        // Create pageable result from filtered list
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());

        // Handle case where start is beyond the filtered list size
        if (start >= filteredList.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredList.size());
        }

        List<DocumentDTO> pageContent = filteredList.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filteredList.size());
    }

    // Helper method to check if no filters are applied
    private boolean isNoFiltersApplied(String search, String status, Long categoryId,
            String type, String price, String author, String tags,
            String dateFrom, String dateTo, String size, String views) {
        return (search == null || search.trim().isEmpty()) &&
                (status == null || status.trim().isEmpty()) &&
                categoryId == null &&
                (type == null || type.trim().isEmpty()) &&
                (price == null || price.trim().isEmpty()) &&
                (author == null || author.trim().isEmpty()) &&
                (tags == null || tags.trim().isEmpty()) &&
                (dateFrom == null || dateFrom.trim().isEmpty()) &&
                (dateTo == null || dateTo.trim().isEmpty()) &&
                (size == null || size.trim().isEmpty()) &&
                (views == null || views.trim().isEmpty());
    }

    // Helper method to set additional fields
    private void setAdditionalFields(DocumentDTO dto) {
        // Get author name from DocumentOwner relationship
        List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(dto.getId());
        if (!owners.isEmpty()) {
            dto.setAuthorName(
                    owners.get(0).getUser().getFullName() != null ? owners.get(0).getUser().getFullName()
                            : owners.get(0).getUser().getUsername());
        } else {
            dto.setAuthorName("Admin"); // Fallback
        }

        // Get category names
        List<DocumentCategory> documentCategories = documentCategoryRepository.findByDocumentId(dto.getId());
        List<String> categoryNames = documentCategories.stream()
                .map(dc -> dc.getCategory().getName())
                .collect(Collectors.toList());
        dto.setCategoryNames(categoryNames);

        // Get category IDs
        List<Long> categoryIds = documentCategories.stream()
                .map(dc -> dc.getCategory().getId())
                .collect(Collectors.toList());
        dto.setCategoryIds(categoryIds);

        // Get tag names
        List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(dto.getId());
        List<String> tagNames = documentTags.stream()
                .map(dt -> dt.getTag().getName())
                .collect(Collectors.toList());
        dto.setTagNames(tagNames);
    }

    // Helper method to apply filters
    private boolean applyFilters(DocumentDTO dto, String search, String status, Long categoryId,
            String type, String price, String author, String tags,
            String dateFrom, String dateTo, String size, String views) {

        // Search filter (title, description, author)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            boolean matchesSearch = false;

            if (dto.getTitle() != null && dto.getTitle().toLowerCase().contains(searchLower)) {
                matchesSearch = true;
            }
            if (dto.getDescription() != null && dto.getDescription().toLowerCase().contains(searchLower)) {
                matchesSearch = true;
            }
            if (dto.getAuthorName() != null && dto.getAuthorName().toLowerCase().contains(searchLower)) {
                matchesSearch = true;
            }

            if (!matchesSearch)
                return false;
        }

        // Status filter
        if (status != null && !status.trim().isEmpty()) {
            if (!status.equalsIgnoreCase(dto.getStatus())) {
                return false;
            }
        }

        // Category filter
        if (categoryId != null) {
            if (dto.getCategoryIds() == null || !dto.getCategoryIds().contains(categoryId)) {
                return false;
            }
        }

        // File type filter
        if (type != null && !type.trim().isEmpty()) {
            if (dto.getFileType() == null || !dto.getFileType().equalsIgnoreCase(type)) {
                return false;
            }
        }

        // Price filter - handle both free/paid and price ranges
        if (price != null && !price.trim().isEmpty()) {
            if ("free".equals(price)) {
                if (dto.getPrice() == null || dto.getPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    return false;
                }
            } else if ("paid".equals(price)) {
                if (dto.getPrice() == null || dto.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    return false;
                }
            } else {
                // Handle price ranges like "1-10", "11-50", "51-100", "101+"
                if (!matchesPriceRange(dto.getPrice(), price)) {
                    return false;
                }
            }
        }

        // Author filter
        if (author != null && !author.trim().isEmpty()) {
            if (dto.getAuthorName() == null
                    || !dto.getAuthorName().toLowerCase().contains(author.toLowerCase().trim())) {
                return false;
            }
        }

        // Tags filter
        if (tags != null && !tags.trim().isEmpty()) {
            String[] tagArray = tags.split(",");
            boolean hasMatchingTag = false;
            for (String tag : tagArray) {
                String tagTrimmed = tag.trim().toLowerCase();
                if (dto.getTagNames() != null) {
                    for (String docTag : dto.getTagNames()) {
                        if (docTag.toLowerCase().contains(tagTrimmed)) {
                            hasMatchingTag = true;
                            break;
                        }
                    }
                }
                if (hasMatchingTag)
                    break;
            }
            if (!hasMatchingTag)
                return false;
        }

        // Date range filter (created date)
        if ((dateFrom != null && !dateFrom.trim().isEmpty()) || (dateTo != null && !dateTo.trim().isEmpty())) {
            if (!matchesDateRange(dto.getCreatedAt(), dateFrom, dateTo)) {
                return false;
            }
        }

        // File size filter
        if (size != null && !size.trim().isEmpty()) {
            if (!matchesFileSizeRange(dto.getFileSize(), size)) {
                return false;
            }
        }

        // Views filter
        if (views != null && !views.trim().isEmpty()) {
            if (!matchesViewsRange(dto.getViewsCount(), views)) {
                return false;
            }
        }

        return true;
    }

    // Helper method to check price range
    private boolean matchesPriceRange(java.math.BigDecimal price, String priceRange) {
        if (price == null) {
            price = java.math.BigDecimal.ZERO;
        }

        try {
            if (priceRange.contains("-")) {
                String[] parts = priceRange.split("-");
                if (parts.length == 2) {
                    double min = Double.parseDouble(parts[0].trim());
                    double max = Double.parseDouble(parts[1].trim());
                    double priceValue = price.doubleValue();
                    return priceValue >= min && priceValue <= max;
                }
            } else if (priceRange.endsWith("+")) {
                String minStr = priceRange.replace("+", "").trim();
                double min = Double.parseDouble(minStr);
                return price.doubleValue() >= min;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid price range format: " + priceRange);
        }
        return false;
    }

    // Helper method to check date range
    private boolean matchesDateRange(java.time.LocalDateTime createdAt, String dateFrom, String dateTo) {
        if (createdAt == null) {
            return false;
        }

        try {
            java.time.LocalDate docDate = createdAt.toLocalDate();

            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                java.time.LocalDate fromDate = java.time.LocalDate.parse(dateFrom.trim());
                if (docDate.isBefore(fromDate)) {
                    return false;
                }
            }

            if (dateTo != null && !dateTo.trim().isEmpty()) {
                java.time.LocalDate toDate = java.time.LocalDate.parse(dateTo.trim());
                if (docDate.isAfter(toDate)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error parsing date range: " + e.getMessage());
            return false;
        }
    }

    // Helper method to check file size range
    private boolean matchesFileSizeRange(Double fileSize, String sizeRange) {
        if (fileSize == null) {
            return false;
        }

        try {
            // Convert KB to MB for comparison
            double fileSizeMB = fileSize / 1024.0;

            if (sizeRange.contains("-")) {
                String[] parts = sizeRange.split("-");
                if (parts.length == 2) {
                    double min = Double.parseDouble(parts[0].trim());
                    double max = Double.parseDouble(parts[1].trim());
                    return fileSizeMB >= min && fileSizeMB <= max;
                }
            } else if (sizeRange.endsWith("+")) {
                String minStr = sizeRange.replace("+", "").trim();
                double min = Double.parseDouble(minStr);
                return fileSizeMB >= min;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid file size range format: " + sizeRange);
        }
        return false;
    }

    // Helper method to check views range
    private boolean matchesViewsRange(Long viewsCount, String viewsRange) {
        if (viewsCount == null) {
            viewsCount = 0L;
        }

        try {
            if (viewsRange.contains("-")) {
                String[] parts = viewsRange.split("-");
                if (parts.length == 2) {
                    long min = Long.parseLong(parts[0].trim());
                    long max = Long.parseLong(parts[1].trim());
                    return viewsCount >= min && viewsCount <= max;
                }
            } else if (viewsRange.endsWith("+")) {
                String minStr = viewsRange.replace("+", "").trim();
                long min = Long.parseLong(minStr);
                return viewsCount >= min;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid views range format: " + viewsRange);
        }
        return false;
    }

    // Get recent documents for email notifications
    public List<Document> getRecentDocuments(int limit) {
        try {
            PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Document> documentsPage = documentRepository.findByDeletedAtIsNull(pageRequest);
            return documentsPage.getContent();
        } catch (Exception e) {
            System.err.println("Error getting recent documents: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Find document by ID for notifications
    public Document findById(Long id) {
        try {
            return documentRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.err.println("Error finding document by ID: " + e.getMessage());
            return null;
        }
    }

    // Get document author name for notifications
    public String getDocumentAuthorName(Long documentId) {
        try {
            List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(documentId);
            if (!owners.isEmpty() && owners.get(0).getUser() != null) {
                User user = owners.get(0).getUser();
                return user.getFullName() != null ? user.getFullName() : user.getUsername();
            }
            return "Không xác định";
        } catch (Exception e) {
            System.err.println("Error getting document author name: " + e.getMessage());
            return "Không xác định";
        }
    }

    // Get ALL documents for export (both active and deleted)
    public List<DocumentDTO> getAllDocumentsForExport() {
        System.out.println("=== GET ALL DOCUMENTS FOR EXPORT ===");

        // Lấy TẤT CẢ documents (cả active và deleted)
        List<Document> allDocuments = documentRepository.findAll();

        List<DocumentDTO> dtoList = allDocuments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Set additional fields for each DTO
        for (DocumentDTO dto : dtoList) {
            // Get author name from DocumentOwner relationship
            List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(dto.getId());
            if (!owners.isEmpty()) {
                dto.setAuthorName(
                        owners.get(0).getUser().getFullName() != null ? owners.get(0).getUser().getFullName()
                                : owners.get(0).getUser().getUsername());
            } else {
                dto.setAuthorName("Admin"); // Fallback
            }

            // Get category names
            List<DocumentCategory> documentCategories = documentCategoryRepository.findByDocumentId(dto.getId());
            List<String> categoryNames = documentCategories.stream()
                    .map(dc -> dc.getCategory().getName())
                    .collect(Collectors.toList());
            dto.setCategoryNames(categoryNames);

            // Get tag names
            List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(dto.getId());
            List<String> tagNames = documentTags.stream()
                    .map(dt -> dt.getTag().getName())
                    .collect(Collectors.toList());
            dto.setTagNames(tagNames);
        }

        System.out.println("Found " + dtoList.size() + " total documents for export");
        return dtoList;
    }

    // Get filtered statistics - TỔNG TẤT CẢ DOCUMENTS (cả đã xóa và chưa xóa)
    public FilteredStatistics getFilteredStatistics(
            String search, String status, Long categoryId, String type, String price,
            String author, String tags, String dateFrom, String dateTo, String size, String views) {

        System.out.println("=== GET FILTERED STATISTICS (TOTAL ALL) ===");

        // Tính tổng TẤT CẢ documents (cả đã xóa và chưa xóa)
        long totalAll = documentRepository.count();

        // Tính tổng views và downloads của TẤT CẢ documents
        List<Document> allDocuments = documentRepository.findAll();
        long totalViews = allDocuments.stream()
                .mapToLong(doc -> doc.getViewsCount() != null ? doc.getViewsCount() : 0)
                .sum();
        long totalDownloads = allDocuments.stream()
                .mapToLong(doc -> doc.getDownloadsCount() != null ? doc.getDownloadsCount() : 0)
                .sum();
        long pendingDocuments = allDocuments.stream()
                .filter(dto -> "PENDING".equalsIgnoreCase(dto.getStatus()))
                .count();

        System.out.println("Total documents (all): " + totalAll);
        System.out.println("Total views (all): " + totalViews);
        System.out.println("Total downloads (all): " + totalDownloads);

        return new FilteredStatistics(totalAll, 0, 0, pendingDocuments, totalViews, totalDownloads);
    }

    // Create document
    @Transactional
    public DocumentDTO createDocument(DocumentDTO documentDTO, MultipartFile file, List<Long> categoryIds,
            List<String> tagNames, Long userId) {
        System.out.println("=== CREATE DOCUMENT SERVICE ===");
        System.out.println("Creating document: " + documentDTO.getTitle());

        try {
            // Create Document entity
            Document document = new Document();
            document.setTitle(documentDTO.getTitle());
            document.setDescription(documentDTO.getDescription());
            if (documentDTO.getPrice() == null) {
                document.setPrice(java.math.BigDecimal.ZERO); // Default price if not provided
            } else {
                document.setPrice(documentDTO.getPrice());
            }
            // document.setPrice(documentDTO.getPrice());
            document.setStatus(documentDTO.getStatus() != null ? documentDTO.getStatus() : "draft");
            document.setVisibility(documentDTO.getVisibility() != null ? documentDTO.getVisibility() : "public");
            document.setViewsCount(0L);
            document.setDownloadsCount(0L);

            // Generate unique slug
            String baseSlug = generateSlug(documentDTO.getTitle());
            String uniqueSlug = ensureUniqueSlug(baseSlug);
            document.setSlug(uniqueSlug);

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                System.out.println("File uploaded: " + file.getOriginalFilename());
                System.out.println("File size: " + file.getSize());
                System.out.println("File content type: " + file.getContentType());

                // Create File entity
                File fileEntity = new File();
                fileEntity.setFileName(file.getOriginalFilename());
                fileEntity.setFileSize(file.getSize());
                fileEntity.setFileType(getFileType(file.getOriginalFilename()));
                fileEntity.setMimeType(file.getContentType());
                // Generate unique file path
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = "uploads/documents/" + fileName;
                fileEntity.setFileName(fileName);
                fileEntity.setFilePath(filePath);

                // Save file entity first
                File savedFile = fileRepository.save(fileEntity);
                System.out.println("File entity saved with ID: " + savedFile.getId());

                // Set file reference to document
                document.setFile(savedFile);

                // Save actual file to disk
                try {
                    java.nio.file.Path uploadPath = java.nio.file.Paths
                            .get("src/main/resources/static/uploads/documents");
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                        System.out.println("Created directory: " + uploadPath.toAbsolutePath());
                    }

                    java.nio.file.Path targetPath = uploadPath.resolve(fileName);
                    java.nio.file.Files.copy(file.getInputStream(), targetPath,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("File saved successfully to: " + targetPath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Error saving file to disk: " + e.getMessage());
                    e.printStackTrace();
                    // File entity is already saved, but physical file failed
                    // You might want to handle this case (delete file entity or mark as failed)
                }

                System.out.println("File metadata saved. Path: " + filePath);
            }

            // Save document
            Document savedDocument = documentRepository.save(document);
            System.out.println("Document saved with ID: " + savedDocument.getId());

            // Create DocumentOwner relationship
            if (userId != null) {
                DocumentOwner owner = new DocumentOwner();
                DocumentOwnerId ownerId = new DocumentOwnerId();
                ownerId.setDocumentId(savedDocument.getId());
                ownerId.setUserId(userId);
                owner.setId(ownerId);

                // Set references
                owner.setDocument(savedDocument);
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    owner.setUser(user);
                    documentOwnerRepository.save(owner);
                    System.out.println("DocumentOwner created for user: " + userId);
                }
            }

            // Handle categories
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Long categoryId : categoryIds) {
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    if (category != null) {
                        DocumentCategory docCategory = new DocumentCategory();
                        DocumentCategoryId docCatId = new DocumentCategoryId();
                        docCatId.setDocumentId(savedDocument.getId());
                        docCatId.setCategoryId(categoryId);
                        docCategory.setId(docCatId);
                        docCategory.setDocument(savedDocument);
                        docCategory.setCategory(category);
                        documentCategoryRepository.save(docCategory);
                    }
                }
                System.out.println("Categories assigned: " + categoryIds.size());
            }

            // Handle tags
            if (tagNames != null && !tagNames.isEmpty()) {
                for (String tagName : tagNames) {
                    if (tagName != null && !tagName.trim().isEmpty()) {
                        // Find or create tag
                        Tag tag = tagRepository.findByName(tagName.trim()).orElse(null);
                        if (tag == null) {
                            tag = new Tag();
                            tag.setName(tagName.trim());
                            tag.setSlug(generateSlug(tagName.trim()));
                            tag = tagRepository.save(tag);
                        }

                        // Create DocumentTag relationship
                        DocumentTag docTag = new DocumentTag();
                        DocumentTagId docTagId = new DocumentTagId();
                        docTagId.setDocumentId(savedDocument.getId());
                        docTagId.setTagId(tag.getId());
                        docTag.setId(docTagId);
                        docTag.setDocument(savedDocument);
                        docTag.setTag(tag);
                        documentTagRepository.save(docTag);
                    }
                }
                System.out.println("Tags assigned: " + tagNames.size());
            }

            // Convert to DTO and return
            DocumentDTO result = convertToDTO(savedDocument);
            result.setCategoryIds(categoryIds);
            result.setTagNames(tagNames);
            result.setUserId(userId);

            System.out.println("Document created successfully: " + result.getId());
            return result;

        } catch (Exception e) {
            System.err.println("Error creating document: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo tài liệu: " + e.getMessage(), e);
        }
    }

    // Generate slug from title
    private String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "document-" + System.currentTimeMillis();
        }

        String slug = title.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        return slug.isEmpty() ? "document-" + System.currentTimeMillis() : slug;
    }

    // Ensure unique slug
    @Transactional
    private String ensureUniqueSlug(String slug) {
        String baseSlug = slug;
        int counter = 1;
        while (documentRepository.findBySlugWithLock(baseSlug).isPresent()) {
            baseSlug = slug + "-" + counter++;
            if (counter > 1000) {
                throw new IllegalStateException("Không thể tạo slug duy nhất sau 1000 lần thử");
            }
        }
        return baseSlug;
    }

    // Get file type from filename
    private String getFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "UNKNOWN";
        }

        String extension = "";
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            extension = filename.substring(lastDotIndex + 1).toLowerCase();
        }

        switch (extension) {
            case "pdf":
                return "PDF";
            case "doc":
            case "docx":
                return "DOC";
            case "ppt":
            case "pptx":
                return "PPT";
            case "xls":
            case "xlsx":
                return "XLS";
            case "txt":
                return "TXT";
            case "zip":
                return "ZIP";
            case "rar":
                return "RAR";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return "IMAGE";
            default:
                return "OTHER";
        }
    }

    // Update document
    @Transactional
    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO, MultipartFile file, List<Long> categoryIds,
            List<String> tagNames) {
        System.out.println("=== UPDATE DOCUMENT SERVICE ===");
        System.out.println("Updating document with ID: " + id);

        try {
            // Find existing document
            Optional<Document> documentOpt = documentRepository.findById(id);
            if (!documentOpt.isPresent()) {
                throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + id);
            }

            Document document = documentOpt.get();
            System.out.println("Found document: " + document.getTitle());

            // Update basic fields
            document.setTitle(documentDTO.getTitle());
            document.setDescription(documentDTO.getDescription());
            document.setPrice(documentDTO.getPrice());
            document.setStatus(documentDTO.getStatus() != null ? documentDTO.getStatus() : document.getStatus());
            document.setVisibility(
                    documentDTO.getVisibility() != null ? documentDTO.getVisibility() : document.getVisibility());

            // Update slug if title changed
            if (!document.getTitle().equals(documentDTO.getTitle())) {
                String baseSlug = generateSlug(documentDTO.getTitle());
                String uniqueSlug = ensureUniqueSlug(baseSlug);
                document.setSlug(uniqueSlug);
            }

            // Handle file upload if provided
            if (file != null && !file.isEmpty()) {
                System.out.println("New file uploaded: " + file.getOriginalFilename());

                // Create new File entity
                File fileEntity = new File();
                fileEntity.setFileName(file.getOriginalFilename());
                fileEntity.setFileSize(file.getSize());
                fileEntity.setFileType(getFileType(file.getOriginalFilename()));
                fileEntity.setMimeType(file.getContentType());

                // Generate unique file path
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                String filePath = "uploads/documents/" + fileName;
                fileEntity.setFilePath(filePath);

                // Save file entity
                File savedFile = fileRepository.save(fileEntity);

                // Update document file reference
                document.setFile(savedFile);

                // Save actual file to disk
                try {
                    java.nio.file.Path uploadPath = java.nio.file.Paths
                            .get("src/main/resources/static/uploads/documents");
                    if (!java.nio.file.Files.exists(uploadPath)) {
                        java.nio.file.Files.createDirectories(uploadPath);
                    }

                    java.nio.file.Path targetPath = uploadPath.resolve(fileName);
                    java.nio.file.Files.copy(file.getInputStream(), targetPath,
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("New file saved to: " + targetPath.toAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Error saving new file: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Save updated document
            Document savedDocument = documentRepository.save(document);
            System.out.println("Document updated with ID: " + savedDocument.getId());

            // Update categories
            if (categoryIds != null) {
                // Remove existing categories
                documentCategoryRepository.deleteByDocumentId(id);

                // Add new categories
                for (Long categoryId : categoryIds) {
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    if (category != null) {
                        DocumentCategory docCategory = new DocumentCategory();
                        DocumentCategoryId docCatId = new DocumentCategoryId();
                        docCatId.setDocumentId(savedDocument.getId());
                        docCatId.setCategoryId(categoryId);
                        docCategory.setId(docCatId);
                        docCategory.setDocument(savedDocument);
                        docCategory.setCategory(category);
                        documentCategoryRepository.save(docCategory);
                    }
                }
                System.out.println("Categories updated: " + categoryIds.size());
            }

            // Update tags
            if (tagNames != null) {
                // Remove existing tags
                documentTagRepository.deleteByDocumentId(id);

                // Add new tags
                for (String tagName : tagNames) {
                    if (tagName != null && !tagName.trim().isEmpty()) {
                        // Find or create tag
                        Tag tag = tagRepository.findByName(tagName.trim()).orElse(null);
                        if (tag == null) {
                            tag = new Tag();
                            tag.setName(tagName.trim());
                            tag.setSlug(generateSlug(tagName.trim()));
                            tag = tagRepository.save(tag);
                        }

                        // Create DocumentTag relationship
                        DocumentTag docTag = new DocumentTag();
                        DocumentTagId docTagId = new DocumentTagId();
                        docTagId.setDocumentId(savedDocument.getId());
                        docTagId.setTagId(tag.getId());
                        docTag.setId(docTagId);
                        docTag.setDocument(savedDocument);
                        docTag.setTag(tag);
                        documentTagRepository.save(docTag);
                    }
                }
                System.out.println("Tags updated: " + tagNames.size());
            }

            // Convert to DTO and return
            DocumentDTO result = convertToDTO(savedDocument);
            result.setCategoryIds(categoryIds);
            result.setTagNames(tagNames);

            System.out.println("Document updated successfully: " + result.getId());
            return result;

        } catch (Exception e) {
            System.err.println("Error updating document: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật tài liệu: " + e.getMessage(), e);
        }
    }

    // Count methods
    public long countAll() {
        return documentRepository.count();
    }

    public long countDeletedDocuments() {
        return documentRepository.countByDeletedAtIsNotNull();
    }

    public long countActiveDocuments() {
        return documentRepository.countByDeletedAtIsNull();
    }

    // Count methods for debugging
    public long countAllDocuments() {
        return documentRepository.count();
    }

    // Statistics class
    public static class FilteredStatistics {
        private final long totalDocuments;
        private final long publishedDocuments;
        private final long draftDocuments;
        private final long pendingDocuments;
        private final long totalViews;
        private final long totalDownloads;

        public FilteredStatistics(long totalDocuments, long publishedDocuments, long draftDocuments,
                long pendingDocuments) {
            this.totalDocuments = totalDocuments;
            this.publishedDocuments = publishedDocuments;
            this.draftDocuments = draftDocuments;
            this.pendingDocuments = pendingDocuments;
            this.totalViews = 0; // Default value
            this.totalDownloads = 0; // Default value
        }

        public FilteredStatistics(long totalDocuments, long publishedDocuments, long draftDocuments,
                long pendingDocuments, long totalViews, long totalDownloads) {
            this.totalDocuments = totalDocuments;
            this.publishedDocuments = publishedDocuments;
            this.draftDocuments = draftDocuments;
            this.pendingDocuments = pendingDocuments;
            this.totalViews = totalViews;
            this.totalDownloads = totalDownloads;
        }

        public long getTotalDocuments() {
            return totalDocuments;
        }

        public long getPublishedDocuments() {
            return publishedDocuments;
        }

        public long getDraftDocuments() {
            return draftDocuments;
        }

        public long getPendingDocuments() {
            return pendingDocuments;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public long getTotalDownloads() {
            return totalDownloads;
        }
    }

    // Helper method to apply sorting from Pageable (including author sort)
    private List<DocumentDTO> applySortingFromPageable(List<DocumentDTO> documents, Pageable pageable) {
        if (documents == null || documents.isEmpty() || pageable.getSort().isUnsorted()) {
            return documents;
        }

        Comparator<DocumentDTO> comparator = null;

        for (Sort.Order order : pageable.getSort()) {
            Comparator<DocumentDTO> fieldComparator = null;

            switch (order.getProperty().toLowerCase()) {
                case "title":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getTitle() != null ? doc.getTitle().toLowerCase() : "");
                    break;
                case "author":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getAuthorName() != null ? doc.getAuthorName().toLowerCase() : "");
                    break;
                case "price":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getPrice() != null ? doc.getPrice() : BigDecimal.ZERO);
                    break;
                case "status":
                    fieldComparator = Comparator.comparing(doc -> doc.getStatus() != null ? doc.getStatus() : "");
                    break;
                case "viewscount":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getViewsCount() != null ? doc.getViewsCount() : 0L);
                    break;
                case "downloadscount":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getDownloadsCount() != null ? doc.getDownloadsCount() : 0L);
                    break;
                case "deletedat":
                case "deleted_at":
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getDeletedAt() != null ? doc.getDeletedAt() : LocalDateTime.MIN);
                    break;
                case "createdat":
                case "created_at":
                default:
                    fieldComparator = Comparator
                            .comparing(doc -> doc.getCreatedAt() != null ? doc.getCreatedAt() : LocalDateTime.MIN);
                    break;
            }

            // Apply sort direction
            if (order.getDirection() == Sort.Direction.DESC) {
                fieldComparator = fieldComparator.reversed();
            }

            // Chain comparators if multiple sort fields
            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }

        return documents.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    // Update document status (for archive and suspend actions)
    @Transactional
    public void updateDocumentStatus(Long documentId, String newStatus) {
        System.out.println("=== UPDATE DOCUMENT STATUS ===");
        System.out.println("Document ID: " + documentId + ", New Status: " + newStatus);

        try {
            Optional<Document> documentOpt = documentRepository.findById(documentId);
            if (!documentOpt.isPresent()) {
                throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + documentId);
            }

            Document document = documentOpt.get();
            String oldStatus = document.getStatus();

            // Update status
            document.setStatus(newStatus);
            document.setUpdatedAt(LocalDateTime.now());

            // Save document
            documentRepository.save(document);

            System.out.println("Document status updated successfully: " + oldStatus + " -> " + newStatus);

        } catch (Exception e) {
            System.err.println("Error updating document status: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái tài liệu: " + e.getMessage());
        }
    }

    // Submit review for a document
    @Transactional
    public Comment submitReview(Long documentId, Long userId, Integer rating, String content) {
        System.out.println("=== SUBMIT REVIEW ===");
        System.out.println("Document ID: " + documentId + ", User ID: " + userId + ", Rating: " + rating + ", Content: "
                + content);

        try {
            // Validate document exists
            Optional<Document> documentOpt = documentRepository.findById(documentId);
            if (!documentOpt.isPresent()) {
                throw new IllegalArgumentException("Tài liệu không tồn tại với ID: " + documentId);
            }

            Document document = documentOpt.get();

            // Validate user exists
            Optional<User> userOpt = userRepository.findById(userId);
            if (!userOpt.isPresent()) {
                throw new IllegalArgumentException("Người dùng không tồn tại với ID: " + userId);
            }

            User user = userOpt.get();

            // Create new comment/review
            Comment comment = new Comment();
            comment.setDocument(document);
            comment.setUser(user);
            comment.setContent(content);
            comment.setRating(rating);
            comment.setStatus("active");
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());

            // Save comment
            Comment savedComment = commentRepository.save(comment);

            System.out.println("Review submitted successfully with ID: " + savedComment.getId());

            return savedComment;

        } catch (Exception e) {
            System.err.println("Error submitting review: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi gửi đánh giá: " + e.getMessage());
        }
    }

    /**
     * Get related documents based on categories and tags
     */
    public List<DocumentDTO> getRelatedDocuments(Long documentId, int limit) {
        try {
            DocumentDTO currentDocument = getDocumentById(documentId);
            if (currentDocument == null) {
                return new ArrayList<>();
            }

            // Get all documents except current one
            List<Document> allDocuments = documentRepository.findAll();
            List<DocumentDTO> relatedDocuments = new ArrayList<>();

            for (Document doc : allDocuments) {
                if (doc.getId().equals(documentId) || doc.getDeletedAt() != null) {
                    continue;
                }

                DocumentDTO docDto = convertToDTO(doc);
                setAdditionalFields(docDto);

                // Calculate similarity score based on categories and tags
                int similarityScore = 0;

                // Category similarity
                if (currentDocument.getCategoryIds() != null && docDto.getCategoryIds() != null) {
                    for (Long currentCategoryId : currentDocument.getCategoryIds()) {
                        if (docDto.getCategoryIds().contains(currentCategoryId)) {
                            similarityScore += 3; // Higher weight for category matches
                        }
                    }
                }

                // Tag similarity
                if (currentDocument.getTagNames() != null && docDto.getTagNames() != null) {
                    for (String currentTag : currentDocument.getTagNames()) {
                        if (docDto.getTagNames().contains(currentTag)) {
                            similarityScore += 1; // Lower weight for tag matches
                        }
                    }
                }

                // Only include documents with some similarity
                if (similarityScore > 0) {
                    relatedDocuments.add(docDto);
                }
            }

            // Sort by similarity score and limit results
            relatedDocuments.sort((a, b) -> {
                int scoreA = calculateSimilarityScore(currentDocument, a);
                int scoreB = calculateSimilarityScore(currentDocument, b);
                return Integer.compare(scoreB, scoreA);
            });

            return relatedDocuments.stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting related documents: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private int calculateSimilarityScore(DocumentDTO current, DocumentDTO other) {
        int score = 0;

        // Category similarity
        if (current.getCategoryIds() != null && other.getCategoryIds() != null) {
            for (Long currentCategoryId : current.getCategoryIds()) {
                if (other.getCategoryIds().contains(currentCategoryId)) {
                    score += 3;
                }
            }
        }

        // Tag similarity
        if (current.getTagNames() != null && other.getTagNames() != null) {
            for (String currentTag : current.getTagNames()) {
                if (other.getTagNames().contains(currentTag)) {
                    score += 1;
                }
            }
        }

        return score;
    }


    // Get featured documents for homepage
    public List<DocumentDTO> getFeaturedDocuments(int limit) {
        try {
            System.out.println("=== GET FEATURED DOCUMENTS ===");
            System.out.println("Limit: " + limit);

            // Create simple Pageable without sorting since we'll use @Query with custom
            // ORDER BY
            PageRequest pageRequest = PageRequest.of(0, limit);

            // Get documents with status APPROVED and not deleted
            Page<Document> featuredDocuments = documentRepository.findFeaturedDocuments("APPROVED", pageRequest);

            List<DocumentDTO> dtoList = featuredDocuments.getContent().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            // Set additional fields for each DTO
            for (DocumentDTO dto : dtoList) {
                setAdditionalFields(dto);
            }

            System.out.println("Found " + dtoList.size() + " featured documents");
            return dtoList;

        } catch (Exception e) {
            System.err.println("Error getting featured documents: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public String importDocuments(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File backup trống!");
            }
            
            // Read file content
            String content = new String(file.getBytes());
            
            // Parse JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>> typeRef = 
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {};
            Map<String, Object> backupData = mapper.readValue(content, typeRef);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> documents = (List<Map<String, Object>>) backupData.get("documents");
            
            int importedCount = 0;
            int skippedCount = 0;
            
            for (Map<String, Object> docData : documents) {
                try {
                    // Check if document already exists by slug
                    String slug = (String) docData.get("slug");
                    if (slug != null && documentRepository.findBySlug(slug).isPresent()) {
                        skippedCount++;
                        continue; // Skip existing documents
                    }
                    
                    // Create new document (simplified import - without file upload)
                    Document document = new Document();
                    document.setTitle((String) docData.get("title"));
                    document.setDescription((String) docData.get("description"));
                    document.setSlug(slug);
                    
                    // Handle price
                    Object priceObj = docData.get("price");
                    if (priceObj != null) {
                        if (priceObj instanceof Number) {
                            document.setPrice(BigDecimal.valueOf(((Number) priceObj).doubleValue()));
                        }
                    }
                    
                    document.setStatus((String) docData.get("status"));
                    document.setVisibility((String) docData.get("visibility"));
                    document.setViewsCount(0L); // Reset counters for imported docs
                    document.setDownloadsCount(0L);
                    
                    // Save document
                    Document savedDoc = documentRepository.save(document);
                    importedCount++;
                    
                    // Note: Categories, tags, and file associations would need more complex logic
                    // This is a simplified import focusing on basic document data
                    
                } catch (Exception e) {
                    System.err.println("Error importing document: " + docData.get("title") + " - " + e.getMessage());
                    skippedCount++;
                }
            }
            
            return String.format("Import hoàn thành: %d tài liệu được import, %d bị bỏ qua",  importedCount, skippedCount);
            
        } catch (Exception e) {
            System.err.println("Error importing documents: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi import tài liệu: " + e.getMessage());
        }
    }

 public String exportAllDocuments() {
        try {
            List<Document> allDocuments = documentRepository.findAll();
            
            // Convert to a simplified format for export
            List<Map<String, Object>> exportData = new ArrayList<>();
            
            for (Document doc : allDocuments) {
                Map<String, Object> docData = new HashMap<>();
                docData.put("id", doc.getId());
                docData.put("title", doc.getTitle());
                docData.put("description", doc.getDescription());
                docData.put("slug", doc.getSlug());
                docData.put("price", doc.getPrice());
                docData.put("status", doc.getStatus());
                docData.put("visibility", doc.getVisibility());
                docData.put("viewsCount", doc.getViewsCount());
                docData.put("downloadsCount", doc.getDownloadsCount());
                docData.put("createdAt", doc.getCreatedAt());
                docData.put("updatedAt", doc.getUpdatedAt());
                
                // Add file info
                if (doc.getFile() != null) {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("fileName", doc.getFile().getFileName());
                    fileData.put("fileSize", doc.getFile().getFileSize());
                    fileData.put("fileType", doc.getFile().getFileType());
                    fileData.put("mimeType", doc.getFile().getMimeType());
                    docData.put("file", fileData);
                }
                
                // Add categories
                List<DocumentCategory> categories = documentCategoryRepository.findByDocumentId(doc.getId());
                List<String> categoryNames = categories.stream()
                    .map(dc -> dc.getCategory().getName())
                    .collect(Collectors.toList());
                docData.put("categories", categoryNames);
                
                // Add tags
                List<DocumentTag> tags = documentTagRepository.findByDocumentId(doc.getId());
                List<String> tagNames = tags.stream()
                    .map(dt -> dt.getTag().getName())
                    .collect(Collectors.toList());
                docData.put("tags", tagNames);
                
                // Add author
                List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(doc.getId());
                if (!owners.isEmpty()) {
                    docData.put("authorName", owners.get(0).getUser().getFullName());
                    docData.put("authorUsername", owners.get(0).getUser().getUsername());
                }
                
                exportData.add(docData);
            }
            
            // Convert to JSON string
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            Map<String, Object> result = new HashMap<>();
            result.put("exportDate", LocalDateTime.now());
            result.put("totalDocuments", exportData.size());
            result.put("documents", exportData);
            
            return mapper.writeValueAsString(result);
            
        } catch (Exception e) {
            System.err.println("Error exporting documents: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi export tài liệu: " + e.getMessage());
        }
    }
    @Transactional
  public DocumentDTO createDocument(DocumentDTO documentDTO, byte[] fileContent, String fileName, List<Long> categoryIds,
          List<String> tagNames, Long userId) {
      System.out.println("=== CREATE DOCUMENT SERVICE ===");
      System.out.println("Creating document: " + documentDTO.getTitle());

      try {
          // Create Document entity
          Document document = new Document();
          document.setTitle(documentDTO.getTitle());
          document.setDescription(documentDTO.getDescription());
          if (documentDTO.getPrice() == null) {
              document.setPrice(BigDecimal.ZERO);
          } else {
              document.setPrice(documentDTO.getPrice());
          }
          document.setStatus(documentDTO.getStatus() != null ? documentDTO.getStatus() : "draft");
          document.setVisibility(documentDTO.getVisibility() != null ? documentDTO.getVisibility() : "public");
          document.setViewsCount(0L);
          document.setDownloadsCount(0L);

          // Generate unique slug
          String baseSlug = generateSlug(documentDTO.getTitle());
          String uniqueSlug = ensureUniqueSlug(baseSlug);
          document.setSlug(uniqueSlug);

          // Handle file upload if provided
          if (fileContent != null && fileContent.length > 0) {
              System.out.println("File uploaded: " + fileName);
              System.out.println("File size: " + fileContent.length);

              // Create File entity
              File fileEntity = new File();
              fileEntity.setFileName(fileName);
              fileEntity.setFileSize((long) fileContent.length);
              fileEntity.setFileType(getFileType(fileName));
              fileEntity.setMimeType("application/octet-stream"); // Có thể xác định MIME type chính xác hơn
              String fileNameWithTimestamp = System.currentTimeMillis() + "_" + fileName;
              String filePath = "uploads/documents/" + fileNameWithTimestamp;
              fileEntity.setFilePath(filePath);

              // Save file entity first
              File savedFile = fileRepository.save(fileEntity);
              System.out.println("File entity saved with ID: " + savedFile.getId());

              // Save actual file to disk
              java.nio.file.Path uploadPath = java.nio.file.Paths.get("src/main/resources/static/uploads/documents");
              if (!java.nio.file.Files.exists(uploadPath)) {
                  java.nio.file.Files.createDirectories(uploadPath);
                  System.out.println("Created directory: " + uploadPath.toAbsolutePath());
              }
              java.nio.file.Path targetPath = uploadPath.resolve(fileNameWithTimestamp);
              java.nio.file.Files.write(targetPath, fileContent);
              System.out.println("File saved successfully to: " + targetPath.toAbsolutePath());
              document.setFile(savedFile);
          }

          // Save document
          Document savedDocument = documentRepository.save(document);
          System.out.println("Document saved with ID: " + savedDocument.getId());

            // Create DocumentOwner relationship
            if (userId != null) {
                DocumentOwner owner = new DocumentOwner();
                DocumentOwnerId ownerId = new DocumentOwnerId();
                ownerId.setDocumentId(savedDocument.getId());
                ownerId.setUserId(userId);
                owner.setId(ownerId);

                // Set references
                owner.setDocument(savedDocument);
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    owner.setUser(user);
                    documentOwnerRepository.save(owner);
                    System.out.println("DocumentOwner created for user: " + userId);
                }
            }

            // Handle categories
            if (categoryIds != null && !categoryIds.isEmpty()) {
                for (Long categoryId : categoryIds) {
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    if (category != null) {
                        DocumentCategory docCategory = new DocumentCategory();
                        DocumentCategoryId docCatId = new DocumentCategoryId();
                        docCatId.setDocumentId(savedDocument.getId());
                        docCatId.setCategoryId(categoryId);
                        docCategory.setId(docCatId);
                        docCategory.setDocument(savedDocument);
                        docCategory.setCategory(category);
                        documentCategoryRepository.save(docCategory);
                    }
                }
                System.out.println("Categories assigned: " + categoryIds.size());
            }

            // Handle tags
            if (tagNames != null && !tagNames.isEmpty()) {
                for (String tagName : tagNames) {
                    if (tagName != null && !tagName.trim().isEmpty()) {
                        // Find or create tag
                        Tag tag = tagRepository.findByName(tagName.trim()).orElse(null);
                        if (tag == null) {
                            tag = new Tag();
                            tag.setName(tagName.trim());
                            tag.setSlug(generateSlug(tagName.trim()));
                            tag = tagRepository.save(tag);
                        }

                        // Create DocumentTag relationship
                        DocumentTag docTag = new DocumentTag();
                        DocumentTagId docTagId = new DocumentTagId();
                        docTagId.setDocumentId(savedDocument.getId());
                        docTagId.setTagId(tag.getId());
                        docTag.setId(docTagId);
                        docTag.setDocument(savedDocument);
                        docTag.setTag(tag);
                        documentTagRepository.save(docTag);
                    }
                }
                System.out.println("Tags assigned: " + tagNames.size());
            }


          // Convert to DTO and return
          DocumentDTO result = convertToDTO(savedDocument);
          result.setCategoryIds(categoryIds);
          result.setTagNames(tagNames);
          result.setUserId(userId);
          System.out.println("Document created successfully: " + result.getId());
          return result;

      } catch (Exception e) {
          System.err.println("Error creating document: " + e.getMessage());
          e.printStackTrace();
          throw new RuntimeException("Lỗi khi tạo tài liệu: " + e.getMessage(), e);
      }
  }

}