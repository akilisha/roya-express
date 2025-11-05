package com.akilisha.oss.roya.plugins.ai;

import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;

import java.util.Map;

/**
 * Prompts utility class - exposes LangChain4j's Prompt and PromptTemplate primitives.
 * 
 * <p>This provides direct access to LangChain4j's prompt abstraction layer,
 * enabling reusable templates with variable substitution and type-safe prompt construction.
 * 
 * <p><b>LangChain4j's Prompt Primitive:</b>
 * <ul>
 *   <li><b>Prompt</b>: A value object representing a prompt text. Can be converted to
 *       SystemMessage, UserMessage, or AiMessage.</li>
 *   <li><b>PromptTemplate</b>: A reusable template with {{variable}} placeholders that
 *       can be applied with variables to produce a Prompt.</li>
 * </ul>
 * 
 * <p><b>Why Prompts?</b>
 * <ul>
 *   <li><b>Separation of concerns</b>: Template construction separated from execution</li>
 *   <li><b>Reusability</b>: Templates can be reused with different variables</li>
 *   <li><b>Type safety</b>: Prompt is a value object, not just a String</li>
 *   <li><b>Conversion</b>: Prompt can be converted to different message types</li>
 * </ul>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>
 * // Simple prompt template
 * PromptTemplate recipeTemplate = Prompts.template(
 *     "Create a recipe for a {{dishType}} with the following ingredients: {{ingredients}}"
 * );
 * 
 * Map&lt;String, Object&gt; variables = Map.of(
 *     "dishType", "oven dish",
 *     "ingredients", "potato, tomato, feta, olive oil"
 * );
 * 
 * Prompt prompt = recipeTemplate.apply(variables);
 * 
 * // Use with AI service
 * AI ai = req.get(AI.class);
 * String response = ai.ask(
 *     Prompts.from("You are a helpful cooking assistant"),
 *     prompt
 * );
 * </pre>
 * 
 * <p><b>Special Variables:</b>
 * PromptTemplate automatically supports:
 * <ul>
 *   <li>{@code {{current_date}}}: Current date (LocalDate.now())</li>
 *   <li>{@code {{current_time}}}: Current time (LocalTime.now())</li>
 *   <li>{@code {{current_date_time}}}: Current date and time (LocalDateTime.now())</li>
 * </ul>
 * 
 * <p>This is a thin wrapper around LangChain4j's primitives. For advanced usage,
 * import {@code dev.langchain4j.model.input.Prompt} and {@code dev.langchain4j.model.input.PromptTemplate} directly.
 * 
 * @see Prompt
 * @see PromptTemplate
 */
public final class Prompts {
    
    private Prompts() {
        // Utility class - no instantiation
    }
    
    /**
     * Create a Prompt from a text string.
     * 
     * @param text The prompt text
     * @return A Prompt instance
     */
    public static Prompt from(String text) {
        return Prompt.from(text);
    }
    
    /**
     * Create a PromptTemplate from a template string with {{variable}} placeholders.
     * 
     * @param template The template string (e.g., "Create a recipe for {{dishType}}")
     * @return A PromptTemplate instance
     */
    public static PromptTemplate template(String template) {
        return PromptTemplate.from(template);
    }
    
    /**
     * Apply a single value to a template containing {{it}} placeholder.
     * 
     * @param template The template string
     * @param value The value to inject
     * @return A Prompt with the value applied
     */
    public static Prompt apply(String template, Object value) {
        return PromptTemplate.from(template).apply(value);
    }
    
    /**
     * Apply multiple variables to a template.
     * 
     * @param template The template string
     * @param variables Map of variable names to values
     * @return A Prompt with variables applied
     */
    public static Prompt apply(String template, Map<String, Object> variables) {
        return PromptTemplate.from(template).apply(variables);
    }
}

