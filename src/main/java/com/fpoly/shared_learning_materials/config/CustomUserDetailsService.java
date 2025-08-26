package com.fpoly.shared_learning_materials.config;

import com.fpoly.shared_learning_materials.domain.User;
import com.fpoly.shared_learning_materials.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user by username: " + username);

        User user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println(
                "User found: " + user.getUsername() + ", status: " + user.getStatus() + ", role: " + user.getRole());

        // Check if user is active
        if (!"active".equals(user.getStatus())) {
            System.out.println("User account is not active: " + user.getStatus());
            throw new AccountLockedException("Tài khoản đã bị khóa bởi quản trị viên");
        }

        // Check if user is locked due to failed attempts
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.LocalDateTime.now())) {
            System.out.println("User account is locked until: " + user.getLockedUntil());
            throw new AccountLockedException("Tài khoản đã bị khóa do nhập sai mật khẩu quá nhiều lần");
        }

        System.out.println("User authentication successful: " + username);
        return new CustomUserPrincipal(user);
    }

    // Custom exception for locked accounts
    public static class AccountLockedException extends org.springframework.security.authentication.LockedException {
        public AccountLockedException(String msg) {
            super(msg);
        }
    }

    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // Add role with ROLE_ prefix for Spring Security
            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
            } else {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }

            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // Implement if needed
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getLockedUntil() == null ||
                    user.getLockedUntil().isBefore(java.time.LocalDateTime.now());
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // Implement if needed
        }

        @Override
        public boolean isEnabled() {
            return "active".equals(user.getStatus());
        }

        // Getter for the User entity
        public User getUser() {
            return user;
        }

        public Long getUserId() {
            return user.getId();
        }

        public String getFullName() {
            return user.getFullName();
        }

        public String getEmail() {
            return user.getEmail();
        }

        public String getRole() {
            return user.getRole();
        }

        public String getAvatarUrl() {
            return user.getAvatarUrl();
        }
    }
}