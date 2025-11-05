package com.akilisha.oss.roya.workflow.examples;

import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.Workflow;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;
import com.akilisha.oss.roya.workflow.execution.WorkflowExecutor;
import com.akilisha.oss.roya.workflow.execution.WorkflowResult;
import com.akilisha.oss.roya.workflow.visitor.LoggingVisitor;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example demonstrating CONTINUATION WORKFLOWS.
 * <p>
 * Use Case: Data processing pipeline with three stages:
 * 1. Data Collection - Scrape data from web
 * 2. Data Processing - Clean and analyze data
 * 3. Report Generation - Create final report
 * <p>
 * Each stage is a separate workflow that can be reused independently.
 */
public class ContinuationWorkflowExample {

    public static void main(String[] args) {
        System.out.println("=== Continuation Workflow Example ===\n");

        // Stage 1: Data Collection Workflow
        Workflow dataCollection = Workflow.create()
                .trigger("start", new ScraperNode())
                .action("validate", new ValidationNode())
                .edge("start", "validate")
                .build();

        // Stage 2: Data Processing Workflow
        Workflow dataProcessing = Workflow.create()
                .trigger("start", new CleanerNode())
                .action("analyze", new AnalysisNode())
                .edge("start", "analyze")
                .build();

        // Stage 3: Report Generation Workflow
        Workflow reportGeneration = Workflow.create()
                .trigger("start", new FormatterNode())
                .action("export", new ExportNode())
                .edge("start", "export")
                .build();

        // Main workflow: Chain all three stages using continuation
        Workflow pipeline = Workflow.create()
                .trigger("init", input -> {
                    System.out.println("📥 Pipeline started with input: " + input.getString("source"));
                    return CompletableFuture.completedFuture(
                            NodeOutput.success(input.data())
                    );
                })

                // Continuation 1: Data collection
                .continuation("collect", dataCollection, "start")

                // Continuation 2: Data processing
                .continuation("process", dataProcessing, "start")

                // Continuation 3: Report generation
                .continuation("report", reportGeneration, "start")

                .edge("init", "collect")
                .edge("collect", "process")
                .edge("process", "report")
                .build();

        // Execute the pipeline
        WorkflowExecutor executor = new WorkflowExecutor(pipeline)
                .addVisitor(new LoggingVisitor());

        WorkflowResult result = executor.executeFrom(
                "init",
                Map.of("source", "https://example.com/data")
        ).join();

        // Display results
        System.out.println("\n" + "=".repeat(50));
        if (result.isSuccess()) {
            System.out.println("✅ Pipeline completed successfully!");
            System.out.println("📊 Data collected: " + result.context().get("recordCount"));
            System.out.println("📈 Analysis score: " + result.context().get("analysisScore"));
            System.out.println("📄 Report: " + result.context().get("reportPath"));
        } else {
            System.out.println("❌ Pipeline failed!");
        }
        System.out.println("=".repeat(50));

        executor.shutdown();
    }

    // Example nodes for data collection stage
    static class ScraperNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🕷️  Scraping data from: " + input.getString("source"));
            try {
                Thread.sleep(500); // Simulate scraping
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of(
                            "rawData", "[scraped content...]",
                            "recordCount", 150
                    ))
            );
        }
    }

    static class ValidationNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("✓ Validating " + input.context().get("recordCount") + " records");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("validated", true))
            );
        }
    }

    // Example nodes for data processing stage
    static class CleanerNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("🧹 Cleaning data...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("cleanedData", "[cleaned content]"))
            );
        }
    }

    static class AnalysisNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📊 Analyzing data...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("analysisScore", 8.5))
            );
        }
    }

    // Example nodes for report generation stage
    static class FormatterNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            System.out.println("📝 Formatting report...");
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("formattedReport", "[report content]"))
            );
        }
    }

    static class ExportNode implements WorkflowNode {
        @Override
        public CompletableFuture<NodeOutput> execute(NodeInput input) {
            String path = "/reports/data-report-" + System.currentTimeMillis() + ".pdf";
            System.out.println("💾 Exporting report to: " + path);
            return CompletableFuture.completedFuture(
                    NodeOutput.success(Map.of("reportPath", path))
            );
        }
    }
}
