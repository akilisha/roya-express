package com.akilisha.oss.roya.plugins.ai.nodes.actions;

import com.akilisha.oss.roya.plugins.ai.AI;
import com.akilisha.oss.roya.plugins.ai.AIOptions;
import com.akilisha.oss.roya.plugins.ai.Vision;
import com.akilisha.oss.roya.workflow.core.NodeInput;
import com.akilisha.oss.roya.workflow.core.NodeOutput;
import com.akilisha.oss.roya.workflow.core.WorkflowNode;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Vision node - performs multimodal operations (image analysis, audio transcription, video description).
 *
 * <p>Supports:
 * <ul>
 *   <li>Image analysis using vision-capable models (GPT-4 Vision, Claude, etc.)</li>
 *   <li>Audio transcription (when audio transcription models are available)</li>
 *   <li>Video description (when video analysis is available)</li>
 *   <li>PDF processing</li>
 * </ul>
 *
 * <p>Example:
 * <pre>
 * ai.workflow("image-analysis")
 *     .trigger("start", ManualTrigger.create())
 *     .vision("analyze", builder -> builder
 *         .operation(VisionOperation.ANALYZE_IMAGE)
 *         .inputKey("imageUrl")
 *         .promptKey("prompt")
 *         .outputKey("description")
 *     )
 *     .build();
 * </pre>
 */
public class VisionNode implements WorkflowNode {

    public enum VisionOperation {
        ANALYZE_IMAGE,
        TRANSCRIBE_AUDIO,
        DESCRIBE_VIDEO,
        PROCESS_PDF
    }

    private final AI ai;
    private final VisionOperation operation;
    private final String inputKey;      // Key for image/audio/video URL or path
    private final String promptKey;     // Key for prompt text (optional for some operations)
    private final String outputKey;     // Output key for result
    private final String prompt;       // Static prompt (if promptKey is not used)
    private final AIOptions options;

    private VisionNode(AI ai, VisionOperation operation, String inputKey,
                      String promptKey, String prompt, String outputKey, AIOptions options) {
        this.ai = ai;
        this.operation = operation;
        this.inputKey = inputKey;
        this.promptKey = promptKey;
        this.prompt = prompt;
        this.outputKey = outputKey;
        this.options = options;
    }

    @Override
    public CompletableFuture<NodeOutput> execute(NodeInput input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Vision vision = ai.vision();
                
                // Get input URL/path
                String mediaUrl = inputKey != null && !inputKey.isEmpty()
                    ? String.valueOf(input.data().getOrDefault(inputKey, ""))
                    : String.valueOf(input.data().getOrDefault("url", ""));

                if (mediaUrl.isEmpty() || mediaUrl.equals("null")) {
                    return NodeOutput.failure("Vision input key '" + inputKey + "' not found or empty");
                }

                // Get prompt
                String promptText = prompt;
                if (promptKey != null && !promptKey.isEmpty()) {
                    Object promptObj = input.data().get(promptKey);
                    if (promptObj != null) {
                        promptText = promptObj.toString();
                    }
                }
                if (promptText == null || promptText.isEmpty()) {
                    promptText = getDefaultPrompt(operation);
                }

                // Execute operation
                String result;
                switch (operation) {
                    case ANALYZE_IMAGE:
                        result = vision.analyzeImage(mediaUrl, promptText, options != null ? options : AIOptions.defaults());
                        break;
                    case TRANSCRIBE_AUDIO:
                        result = vision.transcribeAudio(mediaUrl, options != null ? options : AIOptions.defaults());
                        break;
                    case DESCRIBE_VIDEO:
                        result = vision.describeVideo(mediaUrl, promptText, options != null ? options : AIOptions.defaults());
                        break;
                    case PROCESS_PDF:
                        result = vision.processPdf(mediaUrl, promptText, options != null ? options : AIOptions.defaults());
                        break;
                    default:
                        return NodeOutput.failure("Unknown vision operation: " + operation);
                }

                // Build output
                var outputData = new java.util.HashMap<>(input.data());
                if (outputKey != null && !outputKey.isEmpty()) {
                    outputData.put(outputKey, result);
                } else {
                    outputData.put("result", result);
                }

                return NodeOutput.success(outputData);

            } catch (Exception e) {
                return NodeOutput.failure("Vision operation failed: " + e.getMessage());
            }
        });
    }

    private String getDefaultPrompt(VisionOperation operation) {
        return switch (operation) {
            case ANALYZE_IMAGE -> "What's in this image? Describe it in detail.";
            case TRANSCRIBE_AUDIO -> ""; // No prompt needed for transcription
            case DESCRIBE_VIDEO -> "What's happening in this video? Describe it in detail.";
            case PROCESS_PDF -> "Extract and summarize the key information from this PDF.";
        };
    }

    /**
     * Builder for VisionNode.
     */
    public static class Builder {
        private final AI ai;
        private VisionOperation operation = VisionOperation.ANALYZE_IMAGE;
        private String inputKey = "url";
        private String promptKey;
        private String prompt;
        private String outputKey = "result";
        private AIOptions options;

        private Builder(AI ai) {
            this.ai = ai;
        }

        public static Builder builder(AI ai) {
            return new Builder(ai);
        }

        public Builder operation(VisionOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder inputKey(String inputKey) {
            this.inputKey = inputKey;
            return this;
        }

        public Builder promptKey(String promptKey) {
            this.promptKey = promptKey;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder outputKey(String outputKey) {
            this.outputKey = outputKey;
            return this;
        }

        public Builder options(AIOptions options) {
            this.options = options;
            return this;
        }

        public VisionNode build() {
            return new VisionNode(ai, operation, inputKey, promptKey, prompt, outputKey, options);
        }
    }
}

