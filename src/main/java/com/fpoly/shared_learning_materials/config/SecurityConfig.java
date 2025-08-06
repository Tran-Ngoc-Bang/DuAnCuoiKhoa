package com.fpoly.shared_learning_materials.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll() // Cho phép tất cả yêu cầu mà không cần đăng
																			// nhập
		).csrf().disable(); // Tắt CSRF nếu không cần (tùy chọn)
//                                .authorizeHttpRequests(auth -> auth
//                                                .requestMatchers("/admin/**").hasRole("ADMIN")
//                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/login",
//                                                                "/register", "/", "/static/**")
//                                                .permitAll()
//                                                .anyRequest().authenticated())
//                                .formLogin(form -> form
//                                                // Sử dụng form login mặc định của Spring Boot
//                                                .defaultSuccessUrl("/admin/users", true)
//                                                .permitAll())
//                                .logout(logout -> logout
//                                                .logoutUrl("/logout")
//                                                .logoutSuccessUrl("/login?logout")
//                                                .permitAll());
		return http.build();
	}
}