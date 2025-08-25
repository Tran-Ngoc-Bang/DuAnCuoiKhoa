package com.fpoly.shared_learning_materials.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@EnableAsync
@EnableScheduling
@Profile("azure")
public class AzureConfig {

    /**
     * Configure Caffeine cache for Azure F1 tier optimization
     * Limited memory usage with appropriate eviction policies
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100) // Limit cache size for F1 tier
                .expireAfterWrite(5, TimeUnit.MINUTES) // Auto-expire after 5 minutes
                .expireAfterAccess(10, TimeUnit.MINUTES) // Expire if not accessed
                .recordStats()); // Enable statistics for monitoring
        return cacheManager;
    }

    /**
     * Configure JVM memory settings for Azure F1 tier (1GB RAM)
     * These settings help optimize memory usage within the free tier limits
     */
    @Bean
    public String jvmMemorySettings() {
        // Set JVM memory settings for Azure F1 tier
        System.setProperty("java.awt.headless", "true");
        System.setProperty("file.encoding", "UTF-8");

        // Memory settings optimized for 1GB RAM
        System.setProperty("spring.jvm.memory",
                "-Xmx512m -Xms256m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 " +
                        "-XX:+UseStringDeduplication -XX:+OptimizeStringConcat " +
                        "-XX:+UseCompressedOops -XX:+UseCompressedClassPointers");

        return "JVM memory settings configured for Azure F1 tier";
    }
}