
package com.fpoly.shared_learning_materials.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fpoly.shared_learning_materials.domain.*;
import com.fpoly.shared_learning_materials.dto.CommentDTO;
import com.fpoly.shared_learning_materials.dto.DocumentDTO;
import com.fpoly.shared_learning_materials.repository.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private CommentRepository commentRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/Uploads/documents/";
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB

    @PostConstruct
    public void initUploadDir() {
        java.io.File dir = new java.io.File(UPLOAD_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + dir.getAbsolutePath());
        }
        if (!dir.canWrite()) {
            throw new RuntimeException("No write permission for directory: " + dir.getAbsolutePath());
        }
        System.out.println("Upload dir: " + dir.getAbsolutePath() + ", Ready: " + dir.exists());
    }

    public File saveFile(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is null or empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds limit: 100 MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!extension.matches("pdf|doc|docx|ppt|pptx|xls|xlsx")) {
            throw new IllegalArgumentException("File must be PDF, Word, PowerPoint, or Excel");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        java.io.File targetFile = new java.io.File(UPLOAD_DIR + fileName);

        try {
            file.transferTo(targetFile);
            if (!targetFile.exists()) {
                throw new RuntimeException("File not saved to: " + targetFile.getAbsolutePath());
            }
            System.out.println("Saved file: " + file.getOriginalFilename() + " to " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + e.getMessage(), e);
        }

        File fileEntity = new File();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFilePath("/Uploads/documents/" + fileName);
        fileEntity.setFileType(extension);
        fileEntity.setFileSize((long) (file.getSize() / 1024.0)); // KB
        fileEntity.setMimeType(file.getContentType());
        fileEntity.setStatus("active");
        fileEntity.setUploadedBy(userId != null ? userRepository.findById(userId).orElse(null) : null);

        return fileRepository.save(fileEntity);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    @Transactional
    public DocumentDTO createDocument(DocumentDTO documentDTO, MultipartFile file, List<Long> categoryIds,
            List<String> tagNames, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }

        if (documentDTO.getTitle() == null || documentDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }

        String slug = documentDTO.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(documentDTO.getTitle());
        }
        slug = ensureUniqueSlug(slug);
        if (slug == null) {
            throw new IllegalStateException("Unable to generate a unique slug for the document");
        }

        Document document = new Document();
        document.setTitle(documentDTO.getTitle());
        document.setSlug(slug);
        document.setDescription(documentDTO.getDescription());
        document.setPrice(documentDTO.getPrice());
        document.setStatus(documentDTO.getStatus() != null ? documentDTO.getStatus() : "DRAFT");
        document.setVisibility(documentDTO.getVisibility() != null ? documentDTO.getVisibility() : "public");
        document.setDownloadsCount(0L);
        document.setViewsCount(0L);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            File savedFile = saveFile(file, userId);
            document.setFile(savedFile);
            documentDTO.setFileId(savedFile.getId());
            documentDTO.setFileType(savedFile.getFileType());
            documentDTO.setFileSize(savedFile.getFileSize() != null ? savedFile.getFileSize().doubleValue() : 0.0);
        }

        Document savedDocument;
        try {
            savedDocument = documentRepository.saveAndFlush(document);
        } catch (Exception e) {
            if (e.getMessage().contains("Violation of UNIQUE KEY constraint")) {
                slug = ensureUniqueSlug(
                        generateSlug(documentDTO.getTitle() + "-" + UUID.randomUUID().toString().substring(0, 8)));
                if (slug == null) {
                    throw new IllegalStateException("Unable to generate a unique slug after retry");
                }
                document.setSlug(slug);
                savedDocument = documentRepository.saveAndFlush(document);
            } else {
                throw e;
            }
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (!category.isPresent()) {
                    throw new IllegalArgumentException("Category with ID " + categoryId + " does not exist");
                }
                DocumentCategory documentCategory = new DocumentCategory();
                documentCategory.setId(new DocumentCategoryId(savedDocument.getId(), categoryId));
                documentCategory.setDocument(savedDocument);
                documentCategory.setCategory(category.get());
                documentCategory.setCreatedAt(LocalDateTime.now());
                documentCategoryRepository.save(documentCategory);
            }
        }

        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }
                Optional<Tag> existingTag = tagRepository.findByName(tagName.trim());
                Tag tag;
                if (existingTag.isPresent()) {
                    tag = existingTag.get();
                } else {
                    tag = new Tag();
                    tag.setName(tagName.trim());
                    tag.setSlug(generateSlug(tagName.trim()));
                    tag.setCreatedAt(LocalDateTime.now());
                    tag = tagRepository.save(tag);
                }
                DocumentTagId tagId = new DocumentTagId(savedDocument.getId(), tag.getId());
                Optional<DocumentTag> existingDocumentTag = documentTagRepository.findById(tagId);
                if (!existingDocumentTag.isPresent()) {
                    DocumentTag documentTag = new DocumentTag();
                    documentTag.setId(tagId);
                    documentTag.setDocument(savedDocument);
                    documentTag.setTag(tag);
                    documentTag.setCreatedBy(user.get());
                    documentTag.setCreatedAt(LocalDateTime.now());
                    documentTagRepository.save(documentTag);
                }
            }
        }

        DocumentOwner documentOwner = new DocumentOwner();
        documentOwner.setId(new DocumentOwnerId(savedDocument.getId(), userId));
        documentOwner.setDocument(savedDocument);
        documentOwner.setUser(user.get());
        documentOwner.setOwnershipType("owner");
        documentOwner.setCreatedAt(LocalDateTime.now());
        documentOwnerRepository.save(documentOwner);

        DocumentDTO resultDTO = convertToDTO(savedDocument);
        resultDTO.setCategoryIds(categoryIds);
        resultDTO.setCategoryNames(categoryIds != null ? categoryIds.stream()
                .map(id -> categoryRepository.findById(id).map(Category::getName).orElse(""))
                .collect(Collectors.toList()) : new ArrayList<>());
        resultDTO.setTagNames(tagNames != null ? tagNames : new ArrayList<>());
        resultDTO.setUserId(userId);
        resultDTO.setAuthorName(user.get().getFullName());
        return resultDTO;
    }

    @Transactional
    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO, MultipartFile file, List<Long> categoryIds,
            List<String> tagNames) {
        Optional<Document> optionalDocument = documentRepository.findById(id);
        if (!optionalDocument.isPresent()) {
            System.out.println("Document not found for id: " + id);
            return null;
        }

        Document document = optionalDocument.get();
        document.setTitle(documentDTO.getTitle());
        String slug = documentDTO.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(documentDTO.getTitle());
        }
        if (!slug.equals(document.getSlug())) {
            slug = ensureUniqueSlug(slug);
            if (slug == null) {
                throw new IllegalStateException("Unable to generate a unique slug for the document");
            }
        }
        document.setSlug(slug);
        document.setDescription(documentDTO.getDescription());
        document.setPrice(documentDTO.getPrice());
        document.setStatus(documentDTO.getStatus() != null ? documentDTO.getStatus() : document.getStatus());
        document.setVisibility(
                documentDTO.getVisibility() != null ? documentDTO.getVisibility() : document.getVisibility());
        document.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            File savedFile = saveFile(file, documentDTO.getUserId());
            document.setFile(savedFile);
            documentDTO.setFileId(savedFile.getId());
            documentDTO.setFileType(savedFile.getFileType());
            documentDTO.setFileSize(savedFile.getFileSize() != null ? savedFile.getFileSize().doubleValue() : 0.0);
        }

        // Xử lý danh mục
        if (categoryIds != null && !categoryIds.isEmpty()) {
            System.out.println("Processing categories: " + categoryIds);
            documentCategoryRepository.deleteByDocumentId(id);
            List<DocumentCategory> newCategories = new ArrayList<>();
            for (Long categoryId : categoryIds) {
                Optional<Category> category = categoryRepository.findById(categoryId);
                if (!category.isPresent()) {
                    throw new IllegalArgumentException("Category with ID " + categoryId + " does not exist");
                }
                DocumentCategory documentCategory = new DocumentCategory();
                documentCategory.setId(new DocumentCategoryId(id, categoryId));
                documentCategory.setDocument(document);
                documentCategory.setCategory(category.get());
                documentCategory.setCreatedAt(LocalDateTime.now());
                newCategories.add(documentCategory);
            }
            documentCategoryRepository.saveAll(newCategories);
        } else {
            documentCategoryRepository.deleteByDocumentId(id);
        }

        // Xử lý tag
        if (tagNames != null && !tagNames.isEmpty()) {
            System.out.println("Processing tags: " + tagNames);
            documentTagRepository.deleteByDocumentId(id);
            List<DocumentTag> newTags = new ArrayList<>();
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) {
                    continue;
                }
                Optional<Tag> existingTag = tagRepository.findByName(tagName.trim());
                Tag tag;
                if (existingTag.isPresent()) {
                    tag = existingTag.get();
                } else {
                    tag = new Tag();
                    tag.setName(tagName.trim());
                    tag.setSlug(generateSlug(tagName.trim()));
                    tag.setCreatedAt(LocalDateTime.now());
                    tag = tagRepository.save(tag);
                }
                DocumentTagId tagId = new DocumentTagId(id, tag.getId());
                Optional<DocumentTag> existingDocumentTag = documentTagRepository.findById(tagId);
                if (!existingDocumentTag.isPresent()) {
                    DocumentTag documentTag = new DocumentTag();
                    documentTag.setId(tagId);
                    documentTag.setDocument(document);
                    documentTag.setTag(tag);
                    documentTag.setCreatedBy(userRepository.findById(documentDTO.getUserId()).orElse(null));
                    documentTag.setCreatedAt(LocalDateTime.now());
                    newTags.add(documentTag);
                }
            }
            documentTagRepository.saveAll(newTags);
        } else {
            documentTagRepository.deleteByDocumentId(id);
        }

        Document updatedDocument = documentRepository.saveAndFlush(document);
        DocumentDTO resultDTO = convertToDTO(updatedDocument);
        resultDTO.setCategoryIds(categoryIds);
        resultDTO.setCategoryNames(categoryIds != null ? categoryIds.stream()
                .map(catId -> categoryRepository.findById(catId).map(Category::getName).orElse(""))
                .collect(Collectors.toList()) : new ArrayList<>());
        resultDTO.setTagNames(tagNames != null ? tagNames : new ArrayList<>());
        resultDTO.setUserId(documentDTO.getUserId());
        resultDTO.setAuthorName(userRepository.findById(documentDTO.getUserId()).map(User::getFullName).orElse(null));
        return resultDTO;
    }

    public DocumentDTO getDocumentById(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent()) {
            System.out.println("Document not found for id: " + id);
            return null;
        }
        DocumentDTO dto = convertToDTO(document.get());
        List<DocumentCategory> categories = documentCategoryRepository.findByDocumentId(id);
        dto.setCategoryIds(categories.stream().map(dc -> dc.getCategory().getId()).collect(Collectors.toList()));
        dto.setCategoryNames(categories.stream().map(dc -> dc.getCategory().getName()).collect(Collectors.toList()));
        List<DocumentTag> tags = documentTagRepository.findByDocumentId(id);
        dto.setTagNames(tags.stream().map(dt -> dt.getTag().getName()).collect(Collectors.toList()));
        if (dto.getTagNames() == null) {
            dto.setTagNames(new ArrayList<>());
        }
        List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(id);
        if (!owners.isEmpty()) {
            dto.setUserId(owners.get(0).getUser().getId());
            dto.setAuthorName(owners.get(0).getUser().getFullName());
            dto.setAuthorAvatar(owners.get(0).getUser().getAvatarUrl());
        }
        // Ánh xạ comments
        List<Comment> comments = commentRepository.findByDocumentIdAndStatus(id, "active");
        List<CommentDTO> commentDTOs = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            commentDTO.setId(comment.getId());
            commentDTO.setContent(comment.getContent());
            commentDTO.setUserName(comment.getUser() != null ? comment.getUser().getFullName() : "Không xác định");
            commentDTO.setUserAvatar(comment.getUser() != null && comment.getUser().getAvatarUrl() != null
                    ? comment.getUser().getAvatarUrl()
                    : "https://via.placeholder.com/40");
            commentDTO.setStatus(comment.getStatus());
            commentDTO.setCreatedAt(comment.getCreatedAt());
            return commentDTO;
        }).collect(Collectors.toList());
        dto.setComments(commentDTOs);

        return dto;
    }

    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        // Tạo Pageable với sắp xếp theo createdAt DESC để hiển thị mới nhất trước
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Document> documentPage = documentRepository.findAll(sortedPageable);
        List<DocumentDTO> dtos = documentPage.getContent().stream().map(document -> {
            DocumentDTO dto = convertToDTO(document);
            List<DocumentCategory> categories = documentCategoryRepository.findByDocumentId(document.getId());
            dto.setCategoryIds(categories.stream().map(dc -> dc.getCategory().getId()).collect(Collectors.toList()));
            dto.setCategoryNames(
                    categories.stream().map(dc -> dc.getCategory().getName()).collect(Collectors.toList()));
            List<DocumentTag> tags = documentTagRepository.findByDocumentId(document.getId());
            dto.setTagNames(tags.stream().map(dt -> dt.getTag().getName()).collect(Collectors.toList()));
            if (dto.getTagNames() == null) {
                dto.setTagNames(new ArrayList<>()); // Ensure tagNames is never null
            }
            List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(document.getId());
            if (!owners.isEmpty()) {
                dto.setUserId(owners.get(0).getUser().getId());
                dto.setAuthorName(owners.get(0).getUser().getFullName());
                dto.setAuthorAvatar(owners.get(0).getUser().getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, documentPage.getTotalElements());
    }

    public Page<DocumentDTO> getFilteredDocuments(Pageable pageable, String search, String status, Long categoryId,
            String type, String price, String author, String tags, String dateFrom, String dateTo, String size,
            String views) {

        // Tạo Pageable với sắp xếp theo createdAt DESC
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        // Lấy tất cả documents (không phân trang) để áp dụng filter
        List<Document> allDocuments = documentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<DocumentDTO> allDtos = allDocuments.stream().map(document -> {
            DocumentDTO dto = convertToDTO(document);
            List<DocumentCategory> categories = documentCategoryRepository.findByDocumentId(document.getId());
            dto.setCategoryIds(categories.stream().map(dc -> dc.getCategory().getId()).collect(Collectors.toList()));
            dto.setCategoryNames(
                    categories.stream().map(dc -> dc.getCategory().getName()).collect(Collectors.toList()));
            List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(document.getId());
            dto.setTagNames(documentTags.stream().map(dt -> dt.getTag().getName()).collect(Collectors.toList()));
            if (dto.getTagNames() == null) {
                dto.setTagNames(new ArrayList<>());
            }
            List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(document.getId());
            if (!owners.isEmpty()) {
                dto.setUserId(owners.get(0).getUser().getId());
                dto.setAuthorName(owners.get(0).getUser().getFullName());
                dto.setAuthorAvatar(owners.get(0).getUser().getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // Áp dụng filters
        List<DocumentDTO> filteredDtos = allDtos.stream().filter(dto -> {
            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                if (!dto.getTitle().toLowerCase().contains(searchLower) &&
                        (dto.getDescription() == null || !dto.getDescription().toLowerCase().contains(searchLower)) &&
                        (dto.getAuthorName() == null || !dto.getAuthorName().toLowerCase().contains(searchLower))) {
                    return false;
                }
            }

            // Status filter
            if (status != null && !status.trim().isEmpty()) {
                if (!status.equals(dto.getStatus())) {
                    return false;
                }
            }

            // Category filter
            if (categoryId != null) {
                if (dto.getCategoryIds() == null || !dto.getCategoryIds().contains(categoryId)) {
                    return false;
                }
            }

            // Type filter
            if (type != null && !type.trim().isEmpty()) {
                if (dto.getFileType() == null || !dto.getFileType().toUpperCase().contains(type.toUpperCase())) {
                    return false;
                }
            }

            // Price filter
            if (price != null && !price.trim().isEmpty()) {
                Double docPrice = dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0;
                switch (price) {
                    case "free":
                        if (docPrice > 0)
                            return false;
                        break;
                    case "paid":
                        if (docPrice <= 0)
                            return false;
                        break;
                    case "1-10":
                        if (docPrice < 1 || docPrice > 10)
                            return false;
                        break;
                    case "11-50":
                        if (docPrice < 11 || docPrice > 50)
                            return false;
                        break;
                    case "51-100":
                        if (docPrice < 51 || docPrice > 100)
                            return false;
                        break;
                    case "100+":
                        if (docPrice <= 100)
                            return false;
                        break;
                }
            }

            // Author filter
            if (author != null && !author.trim().isEmpty()) {
                if (dto.getAuthorName() == null || !dto.getAuthorName().toLowerCase().contains(author.toLowerCase())) {
                    return false;
                }
            }

            // Tags filter
            if (tags != null && !tags.trim().isEmpty()) {
                String tagsLower = tags.toLowerCase();
                if (dto.getTagNames() == null || dto.getTagNames().stream()
                        .noneMatch(tag -> tag.toLowerCase().contains(tagsLower))) {
                    return false;
                }
            }

            // Date filters
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(dateFrom + "T00:00:00");
                    if (dto.getCreatedAt() == null || dto.getCreatedAt().isBefore(fromDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Invalid date format, ignore filter
                }
            }

            if (dateTo != null && !dateTo.trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(dateTo + "T23:59:59");
                    if (dto.getCreatedAt() == null || dto.getCreatedAt().isAfter(toDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Invalid date format, ignore filter
                }
            }

            // Size filter
            if (size != null && !size.trim().isEmpty()) {
                Double fileSize = dto.getFileSize() != null ? dto.getFileSize() / 1024.0 : 0.0; // Convert to MB
                switch (size) {
                    case "0-1":
                        if (fileSize >= 1)
                            return false;
                        break;
                    case "1-10":
                        if (fileSize < 1 || fileSize > 10)
                            return false;
                        break;
                    case "10-50":
                        if (fileSize < 10 || fileSize > 50)
                            return false;
                        break;
                    case "50-100":
                        if (fileSize < 50 || fileSize > 100)
                            return false;
                        break;
                    case "100+":
                        if (fileSize <= 100)
                            return false;
                        break;
                }
            }

            // Views filter
            if (views != null && !views.trim().isEmpty()) {
                Long viewCount = dto.getViewsCount() != null ? dto.getViewsCount() : 0L;
                switch (views) {
                    case "0-100":
                        if (viewCount > 100)
                            return false;
                        break;
                    case "101-500":
                        if (viewCount < 101 || viewCount > 500)
                            return false;
                        break;
                    case "501-1000":
                        if (viewCount < 501 || viewCount > 1000)
                            return false;
                        break;
                    case "1001-5000":
                        if (viewCount < 1001 || viewCount > 5000)
                            return false;
                        break;
                    case "5000+":
                        if (viewCount <= 5000)
                            return false;
                        break;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // Tính toán phân trang cho kết quả đã filter
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredDtos.size());
        List<DocumentDTO> pageContent = start < filteredDtos.size() ? filteredDtos.subList(start, end)
                : new ArrayList<>();

        return new PageImpl<>(pageContent, pageable, filteredDtos.size());
    }

    @Transactional
    public void deleteDocument(Long id) {
        Optional<Document> document = documentRepository.findById(id);
        if (!document.isPresent()) {
            throw new RuntimeException("Document not found with id: " + id);
        }
        try {
            documentOwnerRepository.deleteByDocumentId(id);
            documentCategoryRepository.deleteByDocumentId(id);
            documentTagRepository.deleteByDocumentId(id);
            documentRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
        }
    }

    public long countAll() {
        return documentRepository.count();
    }

    public long getTotalViews() {
        return documentRepository.findAll().stream()
                .mapToLong(doc -> doc.getViewsCount() != null ? doc.getViewsCount() : 0)
                .sum();
    }

    public long getTotalDownloads() {
        return documentRepository.findAll().stream()
                .mapToLong(doc -> doc.getDownloadsCount() != null ? doc.getDownloadsCount() : 0)
                .sum();
    }

    public long countByStatus(String status) {
        return documentRepository.countByStatus(status);
    }

    // Method để tính thống kê từ danh sách đã lọc
    public FilteredStatistics getFilteredStatistics(String search, String status, Long categoryId,
            String type, String price, String author, String tags, String dateFrom, String dateTo, String size,
            String views) {

        // Lấy tất cả documents và áp dụng filter (tương tự như getFilteredDocuments)
        List<Document> allDocuments = documentRepository.findAll();
        List<DocumentDTO> allDtos = allDocuments.stream().map(document -> {
            DocumentDTO dto = convertToDTO(document);
            List<DocumentCategory> categories = documentCategoryRepository.findByDocumentId(document.getId());
            dto.setCategoryIds(categories.stream().map(dc -> dc.getCategory().getId()).collect(Collectors.toList()));
            dto.setCategoryNames(
                    categories.stream().map(dc -> dc.getCategory().getName()).collect(Collectors.toList()));
            List<DocumentTag> documentTags = documentTagRepository.findByDocumentId(document.getId());
            dto.setTagNames(documentTags.stream().map(dt -> dt.getTag().getName()).collect(Collectors.toList()));
            if (dto.getTagNames() == null) {
                dto.setTagNames(new ArrayList<>());
            }
            List<DocumentOwner> owners = documentOwnerRepository.findByDocumentId(document.getId());
            if (!owners.isEmpty()) {
                dto.setUserId(owners.get(0).getUser().getId());
                dto.setAuthorName(owners.get(0).getUser().getFullName());
                dto.setAuthorAvatar(owners.get(0).getUser().getAvatarUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        // Áp dụng cùng logic filter như trong getFilteredDocuments
        List<DocumentDTO> filteredDtos = allDtos.stream().filter(dto -> {
            // Search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase();
                if (!dto.getTitle().toLowerCase().contains(searchLower) &&
                        (dto.getDescription() == null || !dto.getDescription().toLowerCase().contains(searchLower)) &&
                        (dto.getAuthorName() == null || !dto.getAuthorName().toLowerCase().contains(searchLower))) {
                    return false;
                }
            }

            // Status filter
            if (status != null && !status.trim().isEmpty()) {
                if (!status.equals(dto.getStatus())) {
                    return false;
                }
            }

            // Category filter
            if (categoryId != null) {
                if (dto.getCategoryIds() == null || !dto.getCategoryIds().contains(categoryId)) {
                    return false;
                }
            }

            // Type filter
            if (type != null && !type.trim().isEmpty()) {
                if (dto.getFileType() == null || !dto.getFileType().toUpperCase().contains(type.toUpperCase())) {
                    return false;
                }
            }

            // Price filter
            if (price != null && !price.trim().isEmpty()) {
                Double docPrice = dto.getPrice() != null ? dto.getPrice().doubleValue() : 0.0;
                switch (price) {
                    case "free":
                        if (docPrice > 0)
                            return false;
                        break;
                    case "paid":
                        if (docPrice <= 0)
                            return false;
                        break;
                    case "1-10":
                        if (docPrice < 1 || docPrice > 10)
                            return false;
                        break;
                    case "11-50":
                        if (docPrice < 11 || docPrice > 50)
                            return false;
                        break;
                    case "51-100":
                        if (docPrice < 51 || docPrice > 100)
                            return false;
                        break;
                    case "100+":
                        if (docPrice <= 100)
                            return false;
                        break;
                }
            }

            // Author filter
            if (author != null && !author.trim().isEmpty()) {
                if (dto.getAuthorName() == null || !dto.getAuthorName().toLowerCase().contains(author.toLowerCase())) {
                    return false;
                }
            }

            // Tags filter
            if (tags != null && !tags.trim().isEmpty()) {
                String tagsLower = tags.toLowerCase();
                if (dto.getTagNames() == null || dto.getTagNames().stream()
                        .noneMatch(tag -> tag.toLowerCase().contains(tagsLower))) {
                    return false;
                }
            }

            // Date filters
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(dateFrom + "T00:00:00");
                    if (dto.getCreatedAt() == null || dto.getCreatedAt().isBefore(fromDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Invalid date format, ignore filter
                }
            }

            if (dateTo != null && !dateTo.trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(dateTo + "T23:59:59");
                    if (dto.getCreatedAt() == null || dto.getCreatedAt().isAfter(toDate)) {
                        return false;
                    }
                } catch (Exception e) {
                    // Invalid date format, ignore filter
                }
            }

            // Size filter
            if (size != null && !size.trim().isEmpty()) {
                Double fileSize = dto.getFileSize() != null ? dto.getFileSize() / 1024.0 : 0.0; // Convert to MB
                switch (size) {
                    case "0-1":
                        if (fileSize >= 1)
                            return false;
                        break;
                    case "1-10":
                        if (fileSize < 1 || fileSize > 10)
                            return false;
                        break;
                    case "10-50":
                        if (fileSize < 10 || fileSize > 50)
                            return false;
                        break;
                    case "50-100":
                        if (fileSize < 50 || fileSize > 100)
                            return false;
                        break;
                    case "100+":
                        if (fileSize <= 100)
                            return false;
                        break;
                }
            }

            // Views filter
            if (views != null && !views.trim().isEmpty()) {
                Long viewCount = dto.getViewsCount() != null ? dto.getViewsCount() : 0L;
                switch (views) {
                    case "0-100":
                        if (viewCount > 100)
                            return false;
                        break;
                    case "101-500":
                        if (viewCount < 101 || viewCount > 500)
                            return false;
                        break;
                    case "501-1000":
                        if (viewCount < 501 || viewCount > 1000)
                            return false;
                        break;
                    case "1001-5000":
                        if (viewCount < 1001 || viewCount > 5000)
                            return false;
                        break;
                    case "5000+":
                        if (viewCount <= 5000)
                            return false;
                        break;
                }
            }

            return true;
        }).collect(Collectors.toList());

        // Tính thống kê từ danh sách đã lọc
        long totalDocuments = filteredDtos.size();
        long totalViews = filteredDtos.stream().mapToLong(dto -> dto.getViewsCount() != null ? dto.getViewsCount() : 0)
                .sum();
        long totalDownloads = filteredDtos.stream()
                .mapToLong(dto -> dto.getDownloadsCount() != null ? dto.getDownloadsCount() : 0).sum();
        long pendingDocuments = filteredDtos.stream().filter(dto -> "PENDING".equals(dto.getStatus())).count();

        return new FilteredStatistics(totalDocuments, totalViews, totalDownloads, pendingDocuments);
    }

    // Inner class để chứa thống kê
    public static class FilteredStatistics {
        private final long totalDocuments;
        private final long totalViews;
        private final long totalDownloads;
        private final long pendingDocuments;

        public FilteredStatistics(long totalDocuments, long totalViews, long totalDownloads, long pendingDocuments) {
            this.totalDocuments = totalDocuments;
            this.totalViews = totalViews;
            this.totalDownloads = totalDownloads;
            this.pendingDocuments = pendingDocuments;
        }

        public long getTotalDocuments() {
            return totalDocuments;
        }

        public long getTotalViews() {
            return totalViews;
        }

        public long getTotalDownloads() {
            return totalDownloads;
        }

        public long getPendingDocuments() {
            return pendingDocuments;
        }
    }

    private DocumentDTO convertToDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setSlug(document.getSlug());
        dto.setDescription(document.getDescription());
        dto.setPrice(document.getPrice());
        dto.setFileId(document.getFile() != null ? document.getFile().getId() : null);
        dto.setFileName(document.getFile() != null ? document.getFile().getFileName() : null);
        dto.setFileType(document.getFile() != null ? document.getFile().getFileType() : null);
        dto.setFileSize(document.getFile() != null && document.getFile().getFileSize() != null
                ? document.getFile().getFileSize()
                : 0.0);
        dto.setStatus(document.getStatus());
        dto.setVisibility(document.getVisibility());
        dto.setDownloadsCount(document.getDownloadsCount() != null ? document.getDownloadsCount() : 0L);
        dto.setViewsCount(document.getViewsCount() != null ? document.getViewsCount() : 0L);
        dto.setCreatedAt(document.getCreatedAt());
        dto.setUpdatedAt(document.getUpdatedAt());
        dto.setPublishedAt(document.getPublishedAt());
        return dto;
    }

    private String generateSlug(String text) {
        if (text == null) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        String slug = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        if (slug.isEmpty()) {
            slug = UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }

    @Transactional
    private String ensureUniqueSlug(String slug) {
        String baseSlug = slug;
        int counter = 1;
        while (documentRepository.findBySlugWithLock(baseSlug).isPresent()) {
            baseSlug = slug + "-" + counter++;
            if (counter > 1000) {
                throw new IllegalStateException("Unable to generate a unique slug after 1000 attempts");
            }
        }
        return baseSlug;
    }
}
