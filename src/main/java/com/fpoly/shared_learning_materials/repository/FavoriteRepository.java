package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
}