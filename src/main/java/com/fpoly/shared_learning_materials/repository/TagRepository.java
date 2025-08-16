package com.fpoly.shared_learning_materials.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    boolean existsBySlugAndDeletedAtIsNull(String slug);
    Page<Tag> findByDeletedAtIsNull(Pageable pageable);
    Optional<Tag> findByName(String name);
    

    
    @Query("SELECT t FROM Tag t WHERE " +
            "(:filter = 'all' OR " +
            "(:filter = 'week' AND t.createdAt >= :startDate) OR " +
            "(:filter = 'month' AND t.createdAt >= :startDate) OR " +
            "(:filter = 'year' AND t.createdAt >= :startDate)) " +
            "AND t.deletedAt IS NULL")
     Page<Tag> findByFilter(@Param("filter") String filter, @Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    
    @Query("SELECT t FROM Tag t")
    List<Tag> findAllTags();
   
    @Query("SELECT COUNT(t) FROM Tag t")
    long countAllTags();
    
    @Query("SELECT t FROM Tag t")
    Page<Tag> findAllTags(Pageable pageable);
    
    @Query("SELECT t FROM Tag t WHERE t.createdAt >= :startDate")
    List<Tag> findByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT t FROM Tag t WHERE t.createdAt >= :startDate")
    Page<Tag> findByCreatedAtAfter(@Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    @Query("SELECT t FROM Tag t LEFT JOIN DocumentTag dt ON t.id = dt.tag.id " +
            "GROUP BY t.id, t.createdAt, t.createdBy, t.deletedAt, t.description, t.name, t.slug, t.updatedAt " +
            "ORDER BY COUNT(dt.document) DESC")
     Page<Tag> findPopularTags(Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM Tag t WHERE t.createdAt >= :startDate")
    Long countByCreatedAtAfter(@Param("startDate") LocalDateTime startDate);

}