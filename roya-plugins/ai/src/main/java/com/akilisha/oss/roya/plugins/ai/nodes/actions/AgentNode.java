package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Agent node - executes an AI agent with tools.
 * 
 * <p>Agents are LLMs with tools that can autonomously decide when to use tools.
 * This node wraps the {@link AI.Agents} API to create and execute agents within workflows.
 * 
 * <p>Example:
 * <pre>
 * List&lt;ToolSpecification&gt; tools = List.of(...);
 * 
 * ai.workflow("agent-workflow")
 *     .trigger("start", ManualTrigger.create())
 *     .agents("agent", builder -> builder
 *         .inputKey("query")
 *         .outputKey("response")
 *         .tools(tools)
 *         .systemPrompt("You are a helpful assistant with access to tools")
 *     )
 *     .build();
 * </pre>
 */
public class AgentNode implements WorkflowNode {
    
    private final AI ai;
    private final String inputKey;
    private final String outputKey;
    private final Object tools;  // List<dev.langchain4j.agent.tool.ToolSpecification>
    private final String systemPrompt;
    private final AIOptions options;
    
    private AgentNode(AI ai, String inputKey, String outputKey,
                     Object tools, String systemPrompt, AIOptions options) {
        this.ai = ai;
        this.inputKey = inputKey;
        this.outputKey = outputKey;
        this.tools = tools;
        this.systemPrompt = systemPrompt;
        this.options = options;
    }
    
    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get input message
                String userMessage = inputKey != null && !inputKey.isEmpty()
                    ? String.valueOf(input.data().getOrDefault(inputKey, ""))
                    : String.valueOf(input.data().getOrDefault("message", ""));
                
                if (userMessage.isEmpty() || userMessage.equals("null")) {
                    return NodeOutput.failure("Agent input key '" + inputKey + "' not found or empty");
                }
                
                // Create agent with tools
                AI.Agent agent = ai.agents().create(agentBuilder -> {
                    if (tools != null) {
                        @SuppressWarnings("unchecked")
                        java.util.List<Object> toolsList = (java.util.List<Object>) tools;
                        agentBuilder.tools(toolsList);
                    }
                    if (systemPrompt != null && !systemPrompt.isEmpty()) {
                        agentBuilder.systemPrompt(systemPrompt);
                    }
                    // Note: model is configured via AIOptions, not directly in agent builder
                    // The adapter will use the configured chat model
                });
                
                // Run agent
                AI.AgentResult result = agent.run(userMessage);
                
                // Build output
                var outputData = new java.util.HashMap<>(input.data());
                if (outputKey != null && !outputKey.isEmpty()) {
                    outputData.put(outputKey, result.text());
                } else {
                    outputData.put("response", result.text());
                }
                
                // Add trace if available
                if (result.trace() != null && !result.trace().isEmpty()) {
                    outputData.put("trace", result.trace());
                }
                
                return NodeOutput.success(outputData);
                
            } catch (Exception e) {
                return NodeOutput.failure("Agent execution failed: " + e.getMessage());
            }
        });
    }
    
    /**
     * Builder for AgentNode.
     */
    public static class Builder {
        private final AI ai;
        private String inputKey = "message";
        private String outputKey = "response";
        private Object tools;
        private String systemPrompt;
        private AIOptions options;
        
        private Builder(AI ai) {
            this.ai = ai;
        }
        
        public static Builder builder(AI ai) {
            return new Builder(ai);
        }
        
        public Builder inputKey(String inputKey) {
            this.inputKey = inputKey;
            return this;
        }
        
        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }
        
        /**
         * Set tools for the agent.
         * 
         * @param tools List of ToolSpecification instances
         * @return This builder
         */
        public Builder tools(Object tools) {
            this.tools = tools;
            return this;
        }
        
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }
        
        public Builder options(AIOptions options) {
            this.options = options;
            return this;
        }
        
        public AgentNode build() {
            return new AgentNode(ai, inputKey, outputKey, tools, systemPrompt, options);
        }
    }
}

