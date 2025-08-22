package com.fpoly.shared_learning_materials.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fpoly.shared_learning_materials.domain.DocumentTag;
import com.fpoly.shared_learning_materials.domain.DocumentTagId;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTag, DocumentTagId> {

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentTag dt WHERE dt.id.documentId = ?1")
    void deleteByDocumentId(Long documentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DocumentTag dt WHERE dt.id.tagId = ?1")
    void deleteByTagId(Long tagId);

    List<DocumentTag> findByDocumentId(Long documentId);

    @Query("SELECT COUNT(dt) FROM DocumentTag dt WHERE dt.id.tagId = :tagId")
    long countByTagId(@Param("tagId") Long tagId);

    @Query("SELECT COUNT(DISTINCT dt.document) FROM DocumentTag dt")
    Long countDistinctDocumentsWithTags();

    @Query("SELECT COUNT(dt) FROM DocumentTag dt")
    Long countTotalDocumentsTagged();

    @Query("SELECT COUNT(DISTINCT dt.id.tagId) FROM DocumentTag dt")
    Long countUsedTags();

    @Query("SELECT dt FROM DocumentTag dt WHERE dt.id.documentId IN :documentIds")
    List<DocumentTag> findByDocumentIdIn(@Param("documentIds") List<Long> documentIds);
}