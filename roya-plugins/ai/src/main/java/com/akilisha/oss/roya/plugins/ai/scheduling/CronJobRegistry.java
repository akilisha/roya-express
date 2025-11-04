package com.akilisha.oss.roya.plugins.ai.scheduling;

import com.akilisha.oss.roya.plugins.ai.nodes.triggers.CronJobTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for cron job-triggered workflows.
 * 
 * Manages Quartz scheduler and registers cron jobs for CronJobTrigger nodes,
 * enabling scheduled execution of AI workflows.
 */
public class CronJobRegistry {
    
    private static final CronJobRegistry INSTANCE = new CronJobRegistry();
    
    /**
     * Cron job registration information.
     */
    public record CronJobRegistration(
        String jobId,
        String cronExpression,
        String timezone,
        String workflowName,
        String triggerNodeId,
        Workflow workflow,
        CronJobTrigger trigger
    ) {}
    
    private final Map<String, CronJobRegistration> registrations = new ConcurrentHashMap<>();
    private Scheduler scheduler;
    private boolean initialized = false;
    
    private CronJobRegistry() {
        // Singleton
    }
    
    /**
     * Get the singleton instance.
     */
    public static CronJobRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize the Quartz scheduler.
     * Should be called once during application startup.
     */
    public synchronized void initialize() throws SchedulerException {
        if (initialized) {
            return;
        }
        
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            scheduler = schedulerFactory.getScheduler();
            scheduler.start();
            initialized = true;
            System.out.println("✓ CronJobRegistry initialized - Quartz scheduler started");
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to initialize Quartz scheduler: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shutdown the scheduler.
     * Should be called during application shutdown.
     */
    public synchronized void shutdown() {
        if (scheduler != null) {
            try {
                scheduler.shutdown();
                System.out.println("✓ CronJobRegistry shut down - Quartz scheduler stopped");
            } catch (SchedulerException e) {
                System.err.println("Error shutting down Quartz scheduler: " + e.getMessage());
            }
        }
    }
    
    /**
     * Register a cron job-triggered workflow.
     * 
     * @param registration Cron job registration information
     */
    public void register(CronJobRegistration registration) throws SchedulerException {
        if (!initialized) {
            initialize();
        }
        
        String jobId = registration.jobId();
        registrations.put(jobId, registration);
        
        // Create Quartz job detail
        JobDetail jobDetail = JobBuilder.newJob(CronJobWorkflowJob.class)
            .withIdentity(jobId, "roya-workflows")
            .usingJobData("workflowName", registration.workflowName())
            .usingJobData("triggerNodeId", registration.triggerNodeId())
            .build();
        
        // Create cron trigger
        CronTrigger cronTrigger;
        try {
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(registration.cronExpression());
            
            // Set timezone if specified
            if (registration.timezone() != null && !registration.timezone().isEmpty()) {
                scheduleBuilder.inTimeZone(java.util.TimeZone.getTimeZone(registration.timezone()));
            }
            
            cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(jobId + "-trigger", "roya-workflows")
                .withSchedule(scheduleBuilder)
                .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cron expression: " + registration.cronExpression() + 
                " - " + e.getMessage(), e);
        }
        
        // Schedule the job
        scheduler.scheduleJob(jobDetail, cronTrigger);
        
        System.out.println("✓ Registered cron job: " + jobId + " (" + registration.cronExpression() + 
            " @ " + registration.timezone() + ") → workflow: " + registration.workflowName());
    }
    
    /**
     * Unregister a cron job.
     */
    public void unregister(String jobId) throws SchedulerException {
        if (scheduler == null || !initialized) {
            return;
        }
        
        CronJobRegistration registration = registrations.remove(jobId);
        if (registration != null) {
            scheduler.deleteJob(new JobKey(jobId, "roya-workflows"));
            System.out.println("✓ Unregistered cron job: " + jobId);
        }
    }
    
    /**
     * Get registration for a job ID.
     */
    public CronJobRegistration get(String jobId) {
        return registrations.get(jobId);
    }
    
    /**
     * Get the Quartz scheduler instance.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    /**
     * Quartz Job implementation that executes workflows.
     */
    public static class CronJobWorkflowJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String workflowName = dataMap.getString("workflowName");
            String triggerNodeId = dataMap.getString("triggerNodeId");
            
            // Get workflow from registry
            com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry workflowRegistry = 
                com.akilisha.oss.roya.plugins.ai.workflow.WorkflowRegistry.getInstance();
            
            Workflow workflow = workflowRegistry.get(workflowName);
            if (workflow == null) {
                System.err.println("⚠️  Workflow not found for cron job: " + workflowName);
                return;
            }
            
            // Create workflow input with cron job metadata
            Map<String, Object> cronData = Map.of(
                "_cron", Map.of(
                    "jobId", context.getJobDetail().getKey().getName(),
                    "fireTime", context.getFireTime().toString(),
                    "scheduledFireTime", context.getScheduledFireTime().toString(),
                    "nextFireTime", context.getNextFireTime() != null ? 
                        context.getNextFireTime().toString() : "N/A"
                )
            );
            
            // Execute workflow from trigger node
            try {
                WorkflowExecutor executor = new WorkflowExecutor(workflow);
                WorkflowResult result = executor.executeFrom(triggerNodeId, cronData).join();
                
                if (result.isSuccess()) {
                    System.out.println("✓ Cron job executed successfully: " + workflowName + 
                        " (trigger: " + triggerNodeId + ")");
                } else {
                    String error = result.finalOutput().error().orElse("Unknown error");
                    System.err.println("✗ Cron job execution failed: " + workflowName + 
                        " (trigger: " + triggerNodeId + ") - " + error);
                }
            } catch (Exception e) {
                System.err.println("✗ Cron job execution error: " + workflowName + 
                    " (trigger: " + triggerNodeId + ") - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}

