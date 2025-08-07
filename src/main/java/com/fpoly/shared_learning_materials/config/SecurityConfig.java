package com.fpoly.shared_learning_materials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(auth -> auth
                                                .anyRequest().permitAll() // Cho phép tất cả request
                                )
                                .csrf().disable(); // Tắt CSRF để tránh lỗi với form POST (nếu có)

                return http.build();
        }

}
