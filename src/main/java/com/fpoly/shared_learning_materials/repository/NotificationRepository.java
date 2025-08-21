package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Notification;
import com.fpoly.shared_learning_materials.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy thông báo theo user và sắp xếp theo thời gian tạo
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Lấy thông báo chưa đọc của user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Đếm số thông báo chưa đọc
    long countByUserAndIsReadFalse(User user);
    
    // Lấy thông báo với phân trang
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Lấy thông báo theo type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, String type);
    
    // Lấy thông báo gần đây (admin dashboard) - lấy tất cả notifications
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findRecentSystemNotifications(Pageable pageable);
    
    // Lấy tất cả thông báo gần đây cho admin
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findAllRecentNotifications(Pageable pageable);
}