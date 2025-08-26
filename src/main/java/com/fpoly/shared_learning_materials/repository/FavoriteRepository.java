package com.fpoly.shared_learning_materials.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fpoly.shared_learning_materials.domain.*;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long>, JpaSpecificationExecutor<Favorite> {
        Optional<Favorite> findByUserAndDocument(User user, Document document);

        @Query("SELECT f.document.id FROM Favorite f WHERE f.user.id = :userId")
        Set<Long> findFavoriteDocIdsByUserId(@Param("userId") Long userId);

        Set<Favorite> findByUser(User user);

        @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user = :user")
        long countByUser(@Param("user") User user);
}
