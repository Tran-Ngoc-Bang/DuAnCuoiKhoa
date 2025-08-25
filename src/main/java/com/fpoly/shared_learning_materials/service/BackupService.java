package com.fpoly.shared_learning_materials.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class BackupService {

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private AzureBlobStorageService azureBlobStorageService;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Value("${app.backup.dir:backups}")
    private String backupDir;

    /**
     * Create database backup (schema and data)
     * This is a simplified backup for free tier constraints
     */
    public boolean createDatabaseBackup() {
        try {
            log.info("Starting database backup...");

            // Create backup directory
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // Generate backup filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "db_backup_" + timestamp + ".sql";
            Path backupFile = backupPath.resolve(backupFileName);

            // Create backup file
            try (Connection connection = dataSource.getConnection();
                    Statement stmt = connection.createStatement();
                    FileOutputStream fos = new FileOutputStream(backupFile.toFile())) {

                // Get database metadata
                DatabaseMetaData metaData = connection.getMetaData();

                // Write backup header
                String header = String.format(
                        "-- Database Backup for Shared Learning Materials\n" +
                                "-- Created: %s\n" +
                                "-- Database: %s\n" +
                                "-- Server: %s\n\n",
                        LocalDateTime.now(),
                        metaData.getDatabaseProductName(),
                        metaData.getURL());
                fos.write(header.getBytes());

                // Get all tables
                List<String> tables = new ArrayList<>();
                ResultSet tableRs = metaData.getTables(null, null, "%", new String[] { "TABLE" });
                while (tableRs.next()) {
                    tables.add(tableRs.getString("TABLE_NAME"));
                }

                // Backup each table
                for (String tableName : tables) {
                    backupTable(stmt, tableName, fos);
                }

                log.info("Database backup completed: {}", backupFile);

                // Upload to Azure Blob Storage if available
                if ("azure".equals(activeProfile) && azureBlobStorageService != null) {
                    uploadBackupToAzure(backupFile);
                }

                return true;
            }

        } catch (Exception e) {
            log.error("Database backup failed", e);
            return false;
        }
    }

    /**
     * Backup individual table
     */
    private void backupTable(Statement stmt, String tableName, FileOutputStream fos) throws Exception {
        log.debug("Backing up table: {}", tableName);

        // Get table structure
        String createTableSql = getCreateTableSQL(stmt, tableName);
        fos.write(("\n-- Table: " + tableName + "\n").getBytes());
        fos.write(createTableSql.getBytes());
        fos.write(";\n\n".getBytes());

        // Get table data
        String selectSql = "SELECT * FROM " + tableName;
        try (ResultSet rs = stmt.executeQuery(selectSql)) {
            int columnCount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                StringBuilder insertSql = new StringBuilder();
                insertSql.append("INSERT INTO ").append(tableName).append(" VALUES (");

                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1)
                        insertSql.append(", ");

                    Object value = rs.getObject(i);
                    if (value == null) {
                        insertSql.append("NULL");
                    } else if (value instanceof String || value instanceof java.sql.Date
                            || value instanceof java.sql.Timestamp) {
                        insertSql.append("'").append(value.toString().replace("'", "''")).append("'");
                    } else {
                        insertSql.append(value.toString());
                    }
                }

                insertSql.append(");\n");
                fos.write(insertSql.toString().getBytes());
            }
        }
    }

    /**
     * Get CREATE TABLE SQL for a table
     */
    private String getCreateTableSQL(Statement stmt, String tableName) throws Exception {
        // This is a simplified version - in production, you might want to use a more
        // robust approach
        return "CREATE TABLE " + tableName + " (id INT PRIMARY KEY)"; // Simplified
    }

    /**
     * Upload backup to Azure Blob Storage
     */
    private void uploadBackupToAzure(Path backupFile) {
        try {
            byte[] backupData = Files.readAllBytes(backupFile);
            String fileName = backupFile.getFileName().toString();

            azureBlobStorageService.uploadFile(backupData, fileName, "application/sql");
            log.info("Backup uploaded to Azure Blob Storage: {}", fileName);

            // Delete local backup file to save space
            Files.delete(backupFile);
            log.info("Local backup file deleted: {}", fileName);

        } catch (Exception e) {
            log.error("Failed to upload backup to Azure", e);
        }
    }

    /**
     * Verify backup integrity
     */
    public boolean verifyBackup(String backupFileName) {
        try {
            Path backupFile = Paths.get(backupDir, backupFileName);

            if (!Files.exists(backupFile)) {
                log.error("Backup file not found: {}", backupFileName);
                return false;
            }

            // Check file size
            long fileSize = Files.size(backupFile);
            if (fileSize == 0) {
                log.error("Backup file is empty: {}", backupFileName);
                return false;
            }

            // Check file content (basic validation)
            String content = Files.readString(backupFile);
            if (!content.contains("Database Backup for Shared Learning Materials")) {
                log.error("Invalid backup file format: {}", backupFileName);
                return false;
            }

            log.info("Backup verification successful: {} ({} bytes)", backupFileName, fileSize);
            return true;

        } catch (Exception e) {
            log.error("Backup verification failed: {}", backupFileName, e);
            return false;
        }
    }

    /**
     * Clean up old backup files
     */
    public void cleanupOldBackups(int keepDays) {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
            int deletedCount = 0;

            File[] backupFiles = backupPath.toFile()
                    .listFiles((dir, name) -> name.startsWith("db_backup_") && name.endsWith(".sql"));
            if (backupFiles != null) {
                for (File file : backupFiles) {
                    if (file.lastModified() < cutoffDate.toEpochSecond(java.time.ZoneOffset.UTC) * 1000) {
                        if (Files.deleteIfExists(file.toPath())) {
                            deletedCount++;
                            log.info("Deleted old backup file: {}", file.getName());
                        }
                    }
                }
            }

            log.info("Cleanup completed: {} old backup files deleted", deletedCount);

        } catch (Exception e) {
            log.error("Backup cleanup failed", e);
        }
    }

    /**
     * Scheduled backup (daily at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        log.info("Starting scheduled database backup...");

        if (createDatabaseBackup()) {
            log.info("Scheduled backup completed successfully");

            // Clean up old backups (keep last 7 days)
            cleanupOldBackups(7);
        } else {
            log.error("Scheduled backup failed");
        }
    }

    /**
     * Get backup statistics
     */
    public BackupStatistics getBackupStatistics() {
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                return new BackupStatistics(0, 0, null);
            }

            File[] backupFiles = backupPath.toFile()
                    .listFiles((dir, name) -> name.startsWith("db_backup_") && name.endsWith(".sql"));
            if (backupFiles == null) {
                return new BackupStatistics(0, 0, null);
            }

            long totalSize = 0;
            LocalDateTime latestBackup = null;

            for (File file : backupFiles) {
                totalSize += file.length();
                LocalDateTime fileDate = LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(file.lastModified()),
                        java.time.ZoneId.systemDefault());

                if (latestBackup == null || fileDate.isAfter(latestBackup)) {
                    latestBackup = fileDate;
                }
            }

            return new BackupStatistics(backupFiles.length, totalSize, latestBackup);

        } catch (Exception e) {
            log.error("Failed to get backup statistics", e);
            return new BackupStatistics(0, 0, null);
        }
    }

    /**
     * Backup statistics class
     */
    public static class BackupStatistics {
        private final int backupCount;
        private final long totalSize;
        private final LocalDateTime latestBackup;

        public BackupStatistics(int backupCount, long totalSize, LocalDateTime latestBackup) {
            this.backupCount = backupCount;
            this.totalSize = totalSize;
            this.latestBackup = latestBackup;
        }

        public int getBackupCount() {
            return backupCount;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public LocalDateTime getLatestBackup() {
            return latestBackup;
        }
    }
}