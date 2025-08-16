package com.fpoly.shared_learning_materials.repository;

import com.fpoly.shared_learning_materials.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
}