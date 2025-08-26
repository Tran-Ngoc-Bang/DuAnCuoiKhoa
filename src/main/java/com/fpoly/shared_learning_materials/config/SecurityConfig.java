package com.fpoly.shared_learning_materials.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.config.http.SessionCreationPolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private CustomUserDetailsService customUserDetailsService;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }

        @Autowired
        @org.springframework.context.annotation.Lazy
        private com.fpoly.shared_learning_materials.service.UserService userService;

        @Bean
        public AuthenticationSuccessHandler authenticationSuccessHandler() {
                return new SavedRequestAwareAuthenticationSuccessHandler() {
                        @Override
                        public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication)
                                        throws java.io.IOException, jakarta.servlet.ServletException {

                                // Cập nhật thông tin đăng nhập cuối
                                String username = authentication.getName();
                                String ipAddress = getClientIpAddress(request);
                                userService.updateLastLogin(username, ipAddress);

                                // Tiếp tục với logic redirect
                                super.onAuthenticationSuccess(request, response, authentication);
                        }

                        @Override
                        protected String determineTargetUrl(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication) {

                                // Lấy saved request URL từ session
                                String targetUrl = super.determineTargetUrl(request, response, authentication);

                                // Log để debug
                                System.out.println("Original targetUrl: " + targetUrl);
                                System.out.println("Request URI: " + request.getRequestURI());
                                System.out.println("User authorities: " + authentication.getAuthorities());

                                // Kiểm tra URL có hợp lệ và an toàn không
                                if (targetUrl != null && !targetUrl.equals("/") && !targetUrl.equals("/login")
                                                && isValidRedirectUrl(targetUrl)) {
                                        System.out.println("Using saved targetUrl: " + targetUrl);
                                        return targetUrl;
                                }

                                // Fallback theo role - tránh redirect loop
                                boolean isAdmin = authentication.getAuthorities().stream()
                                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                                                                .equals("ROLE_ADMIN"));

                                String fallbackUrl = isAdmin ? "/admin" : "/";
                                System.out.println("Using fallback URL: " + fallbackUrl);
                                return fallbackUrl;
                        }

                        private boolean isValidRedirectUrl(String url) {
                                // Chỉ cho phép URL nội bộ, không có protocol
                                return url != null &&
                                                url.startsWith("/") &&
                                                !url.startsWith("//") &&
                                                !url.contains("javascript:") &&
                                                !url.contains("data:") &&
                                                !url.equals("/login") && // Tránh redirect về login
                                                url.length() < 200; // Giới hạn độ dài
                        }

                        private String getClientIpAddress(jakarta.servlet.http.HttpServletRequest request) {
                                String xForwardedFor = request.getHeader("X-Forwarded-For");
                                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                                        return xForwardedFor.split(",")[0].trim();
                                }

                                String xRealIp = request.getHeader("X-Real-IP");
                                if (xRealIp != null && !xRealIp.isEmpty()) {
                                        return xRealIp;
                                }

                                return request.getRemoteAddr();
                        }
                };
        }

        @Bean
        public AuthenticationFailureHandler authenticationFailureHandler() {
                return new SimpleUrlAuthenticationFailureHandler() {
                        @Override
                        public void onAuthenticationFailure(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        org.springframework.security.core.AuthenticationException exception)
                                        throws java.io.IOException, jakarta.servlet.ServletException {

                                // Lấy username từ request
                                String username = request.getParameter("username");
                                String errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng!";
                                String redirectUrl = "/login?error=true";

                                // Check for specific exceptions first (from HEAD)
                                if (exception instanceof InternalAuthenticationServiceException) {
                                        // Kiểm tra cause của InternalAuthenticationServiceException
                                        Throwable cause = exception.getCause();
                                        if (cause instanceof CustomUserDetailsService.AccountLockedException) {
                                                errorMessage = cause.getMessage();
                                                redirectUrl = "/login?error="
                                                                + java.net.URLEncoder.encode(errorMessage, "UTF-8");
                                                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                                                return;
                                        } else {
                                                errorMessage = "Lỗi xác thực: " + exception.getMessage();
                                        }
                                } else if (exception instanceof LockedException) {
                                        errorMessage = exception.getMessage();
                                        redirectUrl = "/login?error="
                                                        + java.net.URLEncoder.encode(errorMessage, "UTF-8");
                                        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                                        return;
                                } else if (exception instanceof BadCredentialsException) {
                                        errorMessage = "Tên đăng nhập hoặc mật khẩu không đúng!";
                                } else if (exception instanceof DisabledException) {
                                        errorMessage = "Tài khoản đã bị vô hiệu hóa!";
                                        redirectUrl = "/login?error="
                                                        + java.net.URLEncoder.encode(errorMessage, "UTF-8");
                                        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                                        return;
                                }

                                // Handle account locking logic (from feature/nvt)
                                if (username != null && !username.trim().isEmpty()) {
                                        // Kiểm tra tài khoản có bị khóa không
                                        if (userService.isAccountLocked(username)) {
                                                long remainingMinutes = userService.getRemainingLockTime(username);
                                                redirectUrl = "/login?locked=true&minutes=" + remainingMinutes;
                                        } else {
                                                // Tăng số lần đăng nhập thất bại
                                                userService.incrementFailedLoginAttempts(username);

                                                // Kiểm tra lại sau khi tăng failed attempts
                                                if (userService.isAccountLocked(username)) {
                                                        long remainingMinutes = userService
                                                                        .getRemainingLockTime(username);
                                                        redirectUrl = "/login?locked=true&minutes=" + remainingMinutes;
                                                } else {
                                                        // Nếu không bị khóa, sử dụng error message từ exception
                                                        // handling
                                                        redirectUrl = "/login?error=" + java.net.URLEncoder
                                                                        .encode(errorMessage, "UTF-8");
                                                }
                                        }
                                }

                                // Redirect với thông báo phù hợp
                                setDefaultFailureUrl(redirectUrl);
                                super.onAuthenticationFailure(request, response, exception);
                        }
                };
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Configure authorization
                                .authorizeHttpRequests(authz -> authz
                                                // Public resources
                                                .requestMatchers("/assets/**", "/css/**", "/js/**", "/images/**",
                                                                "/favicon.ico",
                                                                "/.well-known/**")
                                                .permitAll()
                                                .requestMatchers("/payment/**").permitAll()

                                                .requestMatchers("/", "/home", "/login", "/register", "/confirm",
                                                                "/forgot-password", "/verify-reset-code",
                                                                "/reset-password")
                                                .permitAll()

                                                .requestMatchers("/coin-packages", "/coin-packages/**").permitAll()

                                                // Admin endpoints
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // User endpoints
                                                .requestMatchers("/my-transactions", "/profile/**", "/favorites/**")
                                                .hasAnyRole("USER", "ADMIN")

                                                // All other requests need authentication
                                                .anyRequest().authenticated())

                                // Configure form login
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                .successHandler(authenticationSuccessHandler())
                                                .failureHandler(authenticationFailureHandler())
                                                .permitAll())

                                // Configure logout
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                                                .clearAuthentication(true)
                                                .permitAll())

                                // Configure remember me
                                .rememberMe(remember -> remember
                                                .key("uniqueAndSecret")
                                                .tokenValiditySeconds(86400) // 24 hours
                                                .userDetailsService(customUserDetailsService)
                                                .rememberMeParameter("remember-me"))

                                // Configure session management
                                .sessionManagement(session -> session
                                                .maximumSessions(5) // Allow multiple sessions
                                                .maxSessionsPreventsLogin(false)
                                                .expiredUrl("/login?expired=true"))

                                // Configure CSRF protection
                                .csrf(csrf -> {
                                        HttpSessionCsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();
                                        repo.setParameterName("_csrf");
                                        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
                                        requestHandler.setCsrfRequestAttributeName("_csrf");

                                        csrf.csrfTokenRepository(repo)
                                                        .ignoringRequestMatchers("/api/**", "/login", "/register")
                                                        .csrfTokenRequestHandler(requestHandler);
                                })

                                // Configure security headers
                                .headers(headers -> headers
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .contentTypeOptions(contentTypeOptions -> {
                                                })
                                                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                                                                .maxAgeInSeconds(31536000)
                                                                .includeSubDomains(true)))

                                // Configure exception handling
                                .exceptionHandling(exceptions -> exceptions
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }
}