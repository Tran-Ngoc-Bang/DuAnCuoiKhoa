package com.fpoly.shared_learning_materials.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.File;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}