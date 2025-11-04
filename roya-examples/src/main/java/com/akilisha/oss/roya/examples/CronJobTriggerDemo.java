package com.akilisha.oss.roya.examples;

import com.akilisha.oss.roya.Roya;
import com.akilisha.oss.roya.plugins.ai.AIPlugin;
import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.nodes.triggers.CronJobTrigger;
import com.akilisha.oss.roya.workflow.core.Workflow;

/**
 * Demo for CronJobTrigger functionality.
 * 
 * This example demonstrates how to create a workflow that runs on a schedule.
 * The workflow will execute every minute (for demo purposes) and generate a summary.
 * 
 * Usage:
 *   java -cp ... CronJobTriggerDemo
 * 
 * The workflow will automatically be scheduled and executed according to the cron expression.
 */
public class CronJobTriggerDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Starting CronJobTrigger Demo...");
        
        // Create Roya app
        Roya app = Roya.create();
        
        // Install AI plugin
        AIPlugin aiPlugin = new AIPlugin();
        aiPlugin.register(app.services());
        aiPlugin.setup(app);
        
        // Get AI service
        AI ai = app.services().get(AI.class);
        
        // Create a workflow with a CronJobTrigger
        // This will run every minute (for demo - use "0 0 9 * * ?" for 9 AM daily in production)
        System.out.println("📋 Creating scheduled workflow...");
        Workflow scheduledWorkflow = ai.workflow("scheduled-daily-report")
            .trigger("schedule", CronJobTrigger.create("0 * * * * ?"))  // Every minute (for demo)
            .llm("generate-report", builder -> builder
                .systemPrompt("You are a helpful assistant. Generate a brief daily report summary.")
                .inputKey("_cron")  // Cron metadata will be available
                .outputKey("report")
            )
            .edge("schedule", "generate-report")
            .build();
        
        System.out.println("✅ Scheduled workflow created: scheduled-daily-report");
        System.out.println("✅ Cron job registered: Every minute");
        System.out.println("\n⏰ The workflow will execute automatically according to the cron schedule.");
        System.out.println("   Check the console for execution logs.");
        System.out.println("\n📝 Example cron expressions:");
        System.out.println("   - \"0 0 9 * * ?\" - 9 AM daily");
        System.out.println("   - \"0 0 12 * * MON-FRI\" - Noon on weekdays");
        System.out.println("   - \"0 0 0 1 * ?\" - First day of every month at midnight");
        System.out.println("   - \"0 */5 * * * ?\" - Every 5 minutes");
        System.out.println("\nPress Ctrl+C to stop the server");
        
        // Keep server running
        Thread.currentThread().join();
    }
}

