package com.akilisha.oss.roya.plugins.ai.nodes.triggers;

import com.akilisha.oss.roya.plugins.ai.scheduling.CronJobRegistry;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;

/**
 * Cron job trigger node - schedules workflow execution based on cron expression.
 * 
 * Uses Quartz scheduler to register cron jobs that trigger workflows at specified times.
 * Supports both 5-field (minute hour day month weekday) and 6-field (with seconds) cron expressions.
 * 
 * Features:
 * - Automatic registration with Quartz scheduler
 * - Cron expression validation
 * - Timezone support
 * - Recurring schedules
 * 
 * Example usage:
 * <pre>
 * Workflow workflow = ai.workflow("daily-report")
 *     .trigger("schedule", CronJobTrigger.create("0 0 9 * * ?"))  // 9 AM daily
 *     .llm("generate-report", builder -> builder.systemPrompt("..."))
 *     .edge("schedule", "generate-report")
 *     .build();
 * </pre>
 * 
 * Cron expression formats:
 * - 6-field: "0 0 9 * * ?" (second minute hour day month weekday) - 9 AM daily
 * - 5-field: "0 9 * * *" (minute hour day month weekday) - 9 AM daily
 * 
 * Timezone examples:
 * - "America/New_York"
 * - "Europe/London"
 * - "UTC" (default)
 */
public class CronJobTrigger implements WorkflowNode {
    
    private final String cronExpression;
    private final String timezone;
    private String workflowName; // Set during workflow build
    private String triggerNodeId; // Set during workflow build
    
    private CronJobTrigger(String cronExpression, String timezone) {
        this.cronExpression = cronExpression;
        this.timezone = timezone != null ? timezone : "UTC";
    }
    
    /**
     * Create a cron job trigger with a cron expression.
     * 
     * @param cronExpression Cron expression (e.g., "0 0 9 * * ?" for 9 AM daily)
     * @return CronJobTrigger instance
     */
    public static CronJobTrigger create(String cronExpression) {
        return new CronJobTrigger(cronExpression, "UTC");
    }
    
    /**
     * Create a cron job trigger with timezone.
     * 
     * @param cronExpression Cron expression
     * @param timezone Timezone (e.g., "America/New_York", "UTC")
     * @return CronJobTrigger instance
     */
    public static CronJobTrigger create(String cronExpression, String timezone) {
        return new CronJobTrigger(cronExpression, timezone);
    }
    
    /**
     * Set workflow metadata (called during workflow build).
     */
    public void setWorkflowMetadata(String workflowName, String triggerNodeId) {
        this.workflowName = workflowName;
        this.triggerNodeId = triggerNodeId;
    }
    
    /**
     * Register this cron job with the scheduler (called during workflow build).
     */
    public void register(com.akilisha.oss.roya.workflow.core.Workflow workflow) {
        if (workflowName == null || triggerNodeId == null) {
            throw new IllegalStateException("CronJobTrigger must be set with workflow metadata before registration");
        }
        
        // Generate unique job ID from workflow name and trigger node ID
        String jobId = generateJobId(workflowName, triggerNodeId);
        
        CronJobRegistry.CronJobRegistration registration = new CronJobRegistry.CronJobRegistration(
            jobId,
            cronExpression,
            timezone,
            workflowName,
            triggerNodeId,
            workflow,
            this
        );
        
        try {
            CronJobRegistry.getInstance().register(registration);
        } catch (org.quartz.SchedulerException e) {
            throw new RuntimeException("Failed to register cron job: " + e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression + 
                " - " + e.getMessage(), e);
        }
    }
    
    /**
     * Generate a unique job ID from workflow name and trigger node ID.
     */
    private String generateJobId(String workflowName, String triggerNodeId) {
        return workflowName + ":" + triggerNodeId;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        // When triggered by scheduler, pass through any context data
        // Cron job metadata is already added by CronJobRegistry
        return CompletableFuture.completedFuture(
            NodeOutput.success(input.data())
        );
    }
    
    /**
     * Get the cron expression.
     */
    public String getCronExpression() {
        return cronExpression;
    }
    
    /**
     * Get the timezone.
     */
    public String getTimezone() {
        return timezone;
    }
    
    /**
     * Builder for CronJobTrigger (optional, for more complex configurations).
     */
    public static class Builder {
        private String cronExpression;
        private String timezone = "UTC";
        
        /**
         * Set the cron expression.
         */
        public Builder cronExpression(String cronExpression) {
            this.cronExpression = cronExpression;
            return this;
        }
        
        /**
         * Set the timezone.
         */
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }
        
        /**
         * Build the CronJobTrigger instance.
         */
        public CronJobTrigger build() {
            if (cronExpression == null || cronExpression.isEmpty()) {
                throw new IllegalArgumentException("Cron expression is required");
            }
            return new CronJobTrigger(cronExpression, timezone);
        }
    }
    
    /**
     * Create a builder for CronJobTrigger.
     */
    public static Builder builder() {
        return new Builder();
    }
}

