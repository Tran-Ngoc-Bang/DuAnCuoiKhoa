package com.fpoly.shared_learning_materials.controller;

import com.fpoly.shared_learning_materials.service.AzureBlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.lang.management.ManagementFactory;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private AzureBlobStorageService azureBlobStorageService;

    @GetMapping("/simple")
    public ResponseEntity<Map<String, Object>> simpleHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Quick database check
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(3)) {
                    response.put("status", "UP");
                    response.put("database", "UP");
                } else {
                    response.put("status", "DOWN");
                    response.put("database", "DOWN");
                    return ResponseEntity.status(503).body(response);
                }
            }

            // Quick storage check if available
            if (azureBlobStorageService != null) {
                boolean storageHealthy = azureBlobStorageService.testConnection();
                response.put("storage", storageHealthy ? "UP" : "DOWN");
                if (!storageHealthy) {
                    response.put("status", "DOWN");
                    return ResponseEntity.status(503).body(response);
                }
            }

            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("error", e.getMessage());
            log.error("Health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Database detailed check
            Map<String, Object> dbDetails = new HashMap<>();
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(5)) {
                    dbDetails.put("status", "UP");
                    dbDetails.put("connection_time", "OK");

                    // Test a simple query
                    try (java.sql.Statement stmt = connection.createStatement()) {
                        java.sql.ResultSet rs = stmt.executeQuery("SELECT 1");
                        if (rs.next()) {
                            dbDetails.put("query_test", "OK");
                        }
                    }
                } else {
                    dbDetails.put("status", "DOWN");
                    dbDetails.put("error", "Connection not valid");
                }
            } catch (Exception e) {
                dbDetails.put("status", "DOWN");
                dbDetails.put("error", e.getMessage());
            }
            response.put("database", dbDetails);

            // Storage detailed check
            if (azureBlobStorageService != null) {
                Map<String, Object> storageDetails = new HashMap<>();
                try {
                    boolean storageHealthy = azureBlobStorageService.testConnection();
                    storageDetails.put("status", storageHealthy ? "UP" : "DOWN");
                    if (storageHealthy) {
                        storageDetails.put("connection", "OK");
                    } else {
                        storageDetails.put("error", "Storage connection failed");
                    }
                } catch (Exception e) {
                    storageDetails.put("status", "DOWN");
                    storageDetails.put("error", e.getMessage());
                }
                response.put("storage", storageDetails);
            }

            // System information
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> systemInfo = new HashMap<>();
            systemInfo.put("total_memory_mb", runtime.totalMemory() / (1024 * 1024));
            systemInfo.put("free_memory_mb", runtime.freeMemory() / (1024 * 1024));
            systemInfo.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
            systemInfo.put("available_processors", runtime.availableProcessors());
            systemInfo.put("uptime_ms",
                    System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime());

            response.put("system", systemInfo);
            response.put("timestamp", System.currentTimeMillis());

            // Determine overall status
            boolean isHealthy = true;
            if (dbDetails.get("status").equals("DOWN")) {
                isHealthy = false;
            }
            if (azureBlobStorageService != null &&
                    response.get("storage") instanceof Map &&
                    ((Map<?, ?>) response.get("storage")).get("status").equals("DOWN")) {
                isHealthy = false;
            }

            response.put("overall_status", isHealthy ? "UP" : "DOWN");

            return isHealthy ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);

        } catch (Exception e) {
            response.put("overall_status", "DOWN");
            response.put("error", e.getMessage());
            log.error("Detailed health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(503).body(response);
        }
    }

    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> memoryHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            response.put("memory_used_mb", usedMemory / (1024 * 1024));
            response.put("memory_max_mb", maxMemory / (1024 * 1024));
            response.put("memory_usage_percent", (usedMemory * 100) / maxMemory);
            response.put("available_processors", runtime.availableProcessors());

            // Check if memory usage is high (warning threshold: 80%)
            if ((usedMemory * 100) / maxMemory > 80) {
                log.warn("High memory usage detected: {}%", (usedMemory * 100) / maxMemory);
                response.put("memory_warning", "High memory usage detected");
                response.put("status", "WARNING");
            } else {
                response.put("status", "OK");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            log.error("Memory health check failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(response);
        }
    }
}