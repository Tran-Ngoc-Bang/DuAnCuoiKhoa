package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    @Query(value = """
            SELECT TOP (10)
                ua.id as activity_id,
                ua.action,
                ua.created_at,
                ua.ip_address,
                u.id as user_id,
                u.username,
                u.full_name,
                d.id as document_id,
                d.title as document_title
            FROM User_Activities ua
            LEFT JOIN Users u ON ua.user_id = u.id
            LEFT JOIN Documents d ON ua.document_id = d.id
            ORDER BY ua.created_at DESC
            """, nativeQuery = true)
    List<Map<String, Object>> findRecentActivitiesWithDetails(Pageable pageable);

    @Query("SELECT ua FROM UserActivity ua ORDER BY ua.createdAt DESC")
    List<UserActivity> findRecentActivities(Pageable pageable);

    @Query("SELECT COUNT(ua) FROM UserActivity ua WHERE ua.action = :action")
    long countByAction(String action);

    @Query("SELECT ua FROM UserActivity ua WHERE ua.user.id = :userId ORDER BY ua.createdAt DESC")
    List<UserActivity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}