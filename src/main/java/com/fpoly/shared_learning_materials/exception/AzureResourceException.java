package com.fpoly.shared_learning_materials.exception;

/**
 * Custom exception for Azure resource limit violations
 * This exception is thrown when Azure free tier limits are exceeded
 */
public class AzureResourceException extends RuntimeException {

    private final String resourceType;
    private final String limitType;
    private final long currentUsage;
    private final long limit;

    public AzureResourceException(String message, String resourceType, String limitType, long currentUsage,
            long limit) {
        super(message);
        this.resourceType = resourceType;
        this.limitType = limitType;
        this.currentUsage = currentUsage;
        this.limit = limit;
    }

    public AzureResourceException(String message, String resourceType, String limitType, long currentUsage, long limit,
            Throwable cause) {
        super(message, cause);
        this.resourceType = resourceType;
        this.limitType = limitType;
        this.currentUsage = currentUsage;
        this.limit = limit;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getLimitType() {
        return limitType;
    }

    public long getCurrentUsage() {
        return currentUsage;
    }

    public long getLimit() {
        return limit;
    }

    public double getUsagePercentage() {
        return (double) currentUsage / limit * 100;
    }

    @Override
    public String getMessage() {
        return String.format("%s - %s: %s (Current: %d, Limit: %d, Usage: %.1f%%)",
                super.getMessage(), resourceType, limitType, currentUsage, limit, getUsagePercentage());
    }
}