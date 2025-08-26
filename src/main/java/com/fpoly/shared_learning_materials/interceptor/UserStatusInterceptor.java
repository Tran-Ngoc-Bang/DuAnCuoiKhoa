package com.fpoly.shared_learning_materials.interceptor;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class UserStatusInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Chỉ kiểm tra nếu user đã đăng nhập và không phải anonymous
        if (authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getName())) {

            String username = authentication.getName();

            try {
                // Kiểm tra trạng thái user trong database
                User user = userRepository.findByUsernameAndDeletedAtIsNull(username).orElse(null);

                if (user != null) {
                    boolean shouldInvalidateSession = false;
                    String redirectUrl = "/login";

                    // Kiểm tra trạng thái tài khoản
                    if (!"active".equals(user.getStatus())) {
                        shouldInvalidateSession = true;
                        redirectUrl = "/login?error="
                                + java.net.URLEncoder.encode("Tài khoản đã bị khóa bởi quản trị viên", "UTF-8");
                    }

                    // Kiểm tra khóa tạm thời do nhập sai mật khẩu
                    if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                        shouldInvalidateSession = true;
                        redirectUrl = "/login?error=" + java.net.URLEncoder
                                .encode("Tài khoản đã bị khóa do nhập sai mật khẩu quá nhiều lần", "UTF-8");
                    }

                    // Nếu cần invalidate session
                    if (shouldInvalidateSession) {
                        // Clear SecurityContext
                        SecurityContextHolder.clearContext();

                        // Invalidate session
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            session.invalidate();
                        }

                        // Redirect về login page
                        response.sendRedirect(redirectUrl);
                        return false; // Dừng xử lý request
                    }
                }

            } catch (Exception e) {
                // Log lỗi nhưng không block request
                System.err.println("Error checking user status: " + e.getMessage());
            }
        }

        return true; // Tiếp tục xử lý request
    }
}