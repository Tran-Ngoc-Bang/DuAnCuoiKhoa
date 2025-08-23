package com.fpoly.shared_learning_materials.service;

import com.fpoly.shared_learning_materials.domain.Tag;
import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.dto.TagDTO;
import com.fpoly.shared_learning_materials.exception.ResourceNotFoundException;
import com.fpoly.shared_learning_materials.repository.DocumentTagRepository;
import com.fpoly.shared_learning_materials.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DocumentTagRepository documentTagRepository;

    public List<TagDTO> getFilteredTags(String filter) {
        LocalDateTime startDate = getStartDateForFilter(filter == null ? "month" : filter);
        List<Tag> tags;
        if ("all".equals(filter)) {
            tags = tagRepository.findAllTags();
            System.out.println("Filter: all, Total tags: " + tags.size());
        } else {
            tags = tagRepository.findByCreatedAtAfter(startDate);
            System.out.println(
                    "Filter: " + filter + ", Start date: " + startDate + ", Filtered tags count: " + tags.size());
        }
        return tags.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Page<TagDTO> getAllTags(int page, int size, String sort) {
        return getAllTags(page, size, sort, null);
    }

    public Page<TagDTO> getAllTags(int page, int size, String sort, String search) {
        System.out.println(
                "Fetching tags with page: " + page + ", size: " + size + ", sort: " + sort + ", search: " + search);
        Pageable pageable;
        switch (sort) {
            case "name_asc":
                pageable = PageRequest.of(page, size, Sort.by("name").ascending());
                break;
            case "name_desc":
                pageable = PageRequest.of(page, size, Sort.by("name").descending());
                break;
            case "popular":
                pageable = PageRequest.of(page, size);
                Page<Tag> popularTags = tagRepository.findPopularTags(pageable);
                System.out.println("Popular tags count: " + popularTags.getContent().size() + ", total: "
                        + popularTags.getTotalElements());
                return popularTags.map(this::convertToDTO);
            case "recent":
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                break;
            default:
                // Mặc định sắp xếp theo thời gian tạo mới nhất
                pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                break;
        }

        Page<Tag> tagPage;
        if (search != null && !search.trim().isEmpty()) {
            // Nếu có search, tìm kiếm theo tên tag
            tagPage = tagRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
            System.out.println("Search tags with keyword: '" + search + "', found: " + tagPage.getTotalElements());
        } else {
            // Nếu không có search, lấy tất cả
            tagPage = tagRepository.findAllTags(pageable);
        }

        System.out.println("Tags count: " + tagPage.getContent().size() + ", total: " + tagPage.getTotalElements());
        return tagPage.map(this::convertToDTO);
    }

    public long countAllTags() {
        long count = tagRepository.countAllTags();
        System.out.println("Total tags count: " + count);
        return count;
    }

    public Long countDocumentsWithTags() {
        return documentTagRepository.countDistinctDocumentsWithTags();
    }

    public Long countTagsCreatedThisMonth() {
        LocalDateTime firstDayOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        return tagRepository.countByCreatedAtAfter(firstDayOfMonth);
    }

    public Long countTotalDocumentsTagged() {
        return documentTagRepository.countTotalDocumentsTagged();
    }

    public Long countUnusedTags() {
        Long totalTags = tagRepository.count();
        Long usedTags = documentTagRepository.countUsedTags();
        return totalTags - usedTags;
    }

    public Optional<TagDTO> getTagById(Long id) {
        return tagRepository.findById(id).map(this::convertToDTO);
    }

    public void createTag(TagDTO tagDTO) {
        if (tagDTO == null || tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tag không được để trống");
        }
        if (tagRepository.existsByName(tagDTO.getName())) {
            throw new IllegalArgumentException("Tên tag đã tồn tại");
        }
        Tag tag = new Tag();
        tag.setName(tagDTO.getName().trim());
        tag.setSlug(tagDTO.getName().trim()); // Bỏ slug, luôn set null
        tag.setDescription(tagDTO.getDescription() != null ? tagDTO.getDescription().trim() : null);

        // Gán createdBy
        if (tagDTO.getCreatedById() != null) {
            User user = new User();
            user.setId(tagDTO.getCreatedById());
            tag.setCreatedBy(user);
        } else {
            throw new IllegalArgumentException("Không thể tạo tag: Không có thông tin người tạo");
        }

        tag.setDeletedAt("active".equals(tagDTO.getStatus()) ? null : LocalDateTime.now());
        tagRepository.save(tag);
    }

    public TagDTO updateTag(TagDTO tagDTO) {
        if (tagDTO == null || tagDTO.getId() == null || tagDTO.getName() == null || tagDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("ID và tên tag không được để trống");
        }

        Tag existingTag = tagRepository.findById(tagDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagDTO.getId()));

        if (!existingTag.getName().equals(tagDTO.getName().trim())
                && tagRepository.existsByName(tagDTO.getName().trim())) {
            throw new IllegalArgumentException("Tên tag đã tồn tại");
        }

        existingTag.setName(tagDTO.getName().trim());
        existingTag.setSlug(tagDTO.getName().trim()); // Bỏ slug, luôn set null
        existingTag.setDescription(tagDTO.getDescription() != null ? tagDTO.getDescription().trim() : null);
        existingTag.setUpdatedAt(LocalDateTime.now());

        if (tagDTO.getStatus() != null) {
            if ("inactive".equals(tagDTO.getStatus().trim()) && existingTag.getDeletedAt() == null) {
                existingTag.setDeletedAt(LocalDateTime.now());
            } else if ("active".equals(tagDTO.getStatus().trim()) && existingTag.getDeletedAt() != null) {
                existingTag.setDeletedAt(null);
            }
        }

        existingTag = tagRepository.save(existingTag);
        return convertToDTO(existingTag);
    }

    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        if (tag.getDeletedAt() == null) {
            tag.setDeletedAt(LocalDateTime.now()); // Vô hiệu hóa
        } else {
            tag.setDeletedAt(null); // Kích hoạt
        }
        tagRepository.save(tag);
    }

    private TagDTO convertToDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setSlug(null); // Bỏ slug, luôn set null
        dto.setDescription(tag.getDescription());
        dto.setStatus(tag.getDeletedAt() != null ? "inactive" : "active");
        dto.setCreatedById(tag.getCreatedBy() != null ? tag.getCreatedBy().getId() : null);
        dto.setCreatedByName(tag.getCreatedBy() != null ? tag.getCreatedBy().getFullName() : "Không xác định");
        dto.setCreatedAt(tag.getCreatedAt());
        dto.setUpdatedAt(tag.getUpdatedAt());
        dto.setDeletedAt(tag.getDeletedAt());
        Long documentCount = documentTagRepository.countByTagId(tag.getId());
        dto.setDocumentCount(documentCount != null ? documentCount : 0L);
        return dto;
    }

    private LocalDateTime getStartDateForFilter(String filter) {
        switch (filter) {
            case "week":
                return LocalDateTime.now().minusWeeks(1);
            case "month":
                return LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
            case "year":
                return LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear());
            case "all":
            default:
                return LocalDateTime.of(1970, 1, 1, 0, 0);
        }
    }

    // Restore tag by setting deletedAt to null
    public void restoreTag(Long id) {
        System.out.println("=== RESTORE TAG ===");
        System.out.println("Tag ID: " + id);

        try {
            Optional<Tag> tagOpt = tagRepository.findById(id);
            if (!tagOpt.isPresent()) {
                throw new ResourceNotFoundException("Tag không tồn tại với ID: " + id);
            }

            Tag tag = tagOpt.get();

            if (tag.getDeletedAt() == null) {
                System.out.println("Tag đã ở trạng thái active, không cần khôi phục");
                return;
            }

            // Restore tag by setting deletedAt to null
            tag.setDeletedAt(null);
            tag.setUpdatedAt(LocalDateTime.now());

            tagRepository.save(tag);

            System.out.println("Tag restored successfully: " + tag.getName());

        } catch (Exception e) {
            System.err.println("Error restoring tag: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi khôi phục tag: " + e.getMessage());
        }
    }

    // Permanent delete tag from database
    public void permanentDeleteTag(Long id) {
        System.out.println("=== PERMANENT DELETE TAG ===");
        System.out.println("Tag ID: " + id);

        try {
            Optional<Tag> tagOpt = tagRepository.findById(id);
            if (!tagOpt.isPresent()) {
                throw new ResourceNotFoundException("Tag không tồn tại với ID: " + id);
            }

            Tag tag = tagOpt.get();

            // Kiểm tra xem tag đã bị xóa mềm chưa
            if (tag.getDeletedAt() == null) {
                throw new IllegalArgumentException("Chỉ có thể xóa vĩnh viễn tag đã bị xóa mềm");
            }

            System.out.println("Permanently deleting tag: " + tag.getName());

            // Xóa tất cả liên kết với documents trước
            documentTagRepository.deleteByTagId(id);
            System.out.println("Deleted all document-tag relationships for tag: " + id);

            // Xóa tag khỏi database
            tagRepository.delete(tag);
            System.out.println("Tag permanently deleted from database: " + tag.getName());

        } catch (Exception e) {
            System.err.println("Error permanently deleting tag: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa vĩnh viễn tag: " + e.getMessage());
        }
    }
//     public Page<TagDTO> searchTags(String keyword, int page, int size) {
//     Pageable pageable = PageRequest.of(page, size);
//     return tagRepository.findByNameContainingIgnoreCase(keyword, pageable)
//             .map(this::convertToDTO); // Giả định có phương thức convertToDTO
// }
    public List<Tag> searchTags(String keyword) {
        return tagRepository.findByNameContainingIgnoreCase(keyword);
    }
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }
}
