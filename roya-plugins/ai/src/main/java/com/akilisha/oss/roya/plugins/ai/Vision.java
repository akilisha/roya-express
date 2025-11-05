package com.akilisha.oss.roya.plugins.ai;

/**
 * Vision API for multimodal operations (images, audio, video, PDF).
 * 
 * <p>Supports:
 * <ul>
 *   <li>Image generation (DALL-E 2, DALL-E 3)</li>
 *   <li>Image analysis (GPT-4 Vision, Claude, Gemini, etc.)</li>
 *   <li>Audio transcription (Gemini with AudioContent, requires langchain4j-google-ai-gemini)</li>
 *   <li>Video description (Gemini with VideoContent, requires langchain4j-google-ai-gemini)</li>
 *   <li>PDF processing (Gemini with PdfFileContent, requires langchain4j-google-ai-gemini)</li>
 * </ul>
 * 
 * <p><b>Note:</b> Audio, video, and PDF processing require Google Gemini models via 
 * {@code langchain4j-google-ai-gemini}. These features leverage LangChain4j's multimodal 
 * content types (AudioContent, VideoContent, PdfFileContent) introduced in version 0.33.
 * 
 * <p>Examples:
 * <pre>
 * AI ai = req.get(AI.class);
 * 
 * // Image analysis
 * String description = ai.vision().analyzeImage("https://example.com/image.jpg", "What's in this image?");
 * 
 * // Image generation (DALL-E)
 * String imageUrl = ai.vision().generateImage("A futuristic cityscape at sunset");
 * 
 * // Audio transcription (Gemini)
 * String transcript = ai.vision().transcribeAudio("https://storage.googleapis.com/cloud-samples-data/generative-ai/audio/pixel.mp3");
 * 
 * // Video description (Gemini)
 * String chapters = ai.vision().describeVideo("https://example.com/video.mp4", "Prepare chapters for this video file, using the YouTube chapter notation");
 * 
 * // PDF processing (Gemini)
 * String summary = ai.vision().processPdf("https://example.com/document.pdf", "Give a summary of this paper");
 * </pre>
 * 
 * <p>All methods support:
 * <ul>
 *   <li>HTTP/HTTPS URLs: "https://example.com/file.mp3"</li>
 *   <li>Google Cloud Storage URLs: "gs://bucket/file.mp3"</li>
 *   <li>Local file paths: "/path/to/file.mp3"</li>
 * </ul>
 * 
 * @see <a href="https://glaforge.dev/posts/2024/07/25/analyzing-videos-audios-and-pdfs-with-gemini-in-langchain4j/">LangChain4j Multimodal Support</a>
 */
public interface Vision {
    
    /**
     * Generate an image using DALL-E models.
     * 
     * @param prompt Text description of the image to generate
     * @return URL of the generated image
     */
    String generateImage(String prompt);
    
    /**
     * Generate an image with options.
     * 
     * @param prompt Text description of the image to generate
     * @param options AI options (model, size, quality, etc.)
     * @return URL of the generated image
     */
    String generateImage(String prompt, AIOptions options);
    
    /**
     * Analyze an image using vision-capable models.
     * 
     * @param imageUrl Image URL, base64 data URI, or file path
     * @param prompt Prompt describing what to analyze
     * @return Analysis result
     */
    String analyzeImage(String imageUrl, String prompt);
    
    /**
     * Analyze an image with options.
     * 
     * @param imageUrl Image URL, base64 data URI, or file path
     * @param prompt Prompt describing what to analyze
     * @param options AI options (model, temperature, etc.)
     * @return Analysis result
     */
    String analyzeImage(String imageUrl, String prompt, AIOptions options);
    
    /**
     * Transcribe audio to text.
     * 
     * @param audioUrl Audio URL or file path
     * @return Transcription text
     */
    String transcribeAudio(String audioUrl);
    
    /**
     * Transcribe audio with options.
     * 
     * @param audioUrl Audio URL or file path
     * @param options AI options (model, language, etc.)
     * @return Transcription text
     */
    String transcribeAudio(String audioUrl, AIOptions options);
    
    /**
     * Describe video content.
     * 
     * @param videoUrl Video URL or file path
     * @param prompt Prompt describing what to analyze
     * @return Description result
     */
    String describeVideo(String videoUrl, String prompt);
    
    /**
     * Describe video with options.
     * 
     * @param videoUrl Video URL or file path
     * @param prompt Prompt describing what to analyze
     * @param options AI options
     * @return Description result
     */
    String describeVideo(String videoUrl, String prompt, AIOptions options);
    
    /**
     * Process PDF file.
     * 
     * @param pdfUrl PDF URL or file path
     * @param prompt Prompt describing what to extract/analyze
     * @return Processing result
     */
    String processPdf(String pdfUrl, String prompt);
    
    /**
     * Process PDF with options.
     * 
     * @param pdfUrl PDF URL or file path
     * @param prompt Prompt describing what to extract/analyze
     * @param options AI options
     * @return Processing result
     */
    String processPdf(String pdfUrl, String prompt, AIOptions options);
}

