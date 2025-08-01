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

        @Bean
        public AuthenticationSuccessHandler authenticationSuccessHandler() {
                return new SimpleUrlAuthenticationSuccessHandler() {
                        @Override
                        protected String determineTargetUrl(jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response,
                                        org.springframework.security.core.Authentication authentication) {

                                // Kiểm tra role và redirect tương ứng
                                boolean isAdmin = authentication.getAuthorities().stream()
                                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority()
                                                                .equals("ROLE_ADMIN"));

                                if (isAdmin) {
                                        return "/admin";
                                } else {
                                        return "/";
                                }
                        }
                };
        }

        @Bean
        public AuthenticationFailureHandler authenticationFailureHandler() {
                SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
                handler.setDefaultFailureUrl("/login?error=true");
                return handler;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                // Configure authorization
                                .authorizeHttpRequests(authz -> authz
                                                // Public resources
                                                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico")
                                                .permitAll()
                                                .requestMatchers("/", "/home", "/login", "/register").permitAll()
                                                .requestMatchers("/coin-packages", "/coin-packages/**").permitAll()

                                                // Admin endpoints
                                                .requestMatchers("/admin/**").hasRole("ADMIN")

                                                // User endpoints
                                                .requestMatchers("/my-transactions", "/profile/**")
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
                                                .logoutSuccessUrl("/login?logout=true")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
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
                                                .maximumSessions(1)
                                                .maxSessionsPreventsLogin(false)
                                                .expiredUrl("/login?expired=true"))

                                // Configure CSRF protection
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**") // Ignore CSRF for API endpoints if
                                                                                    // needed
                                )

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