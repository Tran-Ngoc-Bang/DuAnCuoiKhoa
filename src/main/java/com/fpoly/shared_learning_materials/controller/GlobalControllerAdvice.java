package com.fpoly.shared_learning_materials.controller;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import com.fpoly.shared_learning_materials.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("unreadNotificationCount")
    public Long getUnreadNotificationCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                String username = authentication.getName();
                User currentUser = userRepository.findByUsernameAndDeletedAtIsNull(username)
                        .orElse(null);
                
                if (currentUser != null) {
                    return notificationService.getUnreadCount(currentUser);
                }
            }
        } catch (Exception e) {
            // Log error but don't break the page
            System.err.println("Error getting unread notification count: " + e.getMessage());
        }
        
        return 0L;
    }
}