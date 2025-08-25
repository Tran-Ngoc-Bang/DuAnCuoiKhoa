package com.fpoly.shared_learning_materials.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("azure")
public class MonitoringConfig {

    /**
     * Azure monitoring configuration
     * This class provides basic monitoring setup for Azure deployment
     */

    // Note: Micrometer dependencies have been removed to avoid compatibility issues
    // Basic monitoring is handled through health checks and logging

}