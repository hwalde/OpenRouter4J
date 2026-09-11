package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the {@code openrouter:image_generation} server tool:
 * generates images from text prompts using an image generation model. Emitted
 * into the {@code tools} request array as
 * {@code {"type": "openrouter:image_generation"}} plus a {@code parameters}
 * object holding only the explicitly configured fields.
 * <p>
 * The parameters accept the model slug plus all {@code image_config} params
 * ({@code aspect_ratio}, {@code quality}, {@code size}, {@code background},
 * {@code output_format}, {@code output_compression}, {@code moderation}, ...);
 * the typed builder covers {@code model} and the verbatim
 * {@link Builder#option(String, Object)} escape hatch carries every
 * provider-specific key.
 * <p>
 * The generated image arrives on the response of the request
 * ({@code OpenRouterChatCompletionResponse#images()}), not as a file on disk.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/server-tools/image-generation">Image generation server tool</a>
 */
public final class OpenRouterImageGenerationServerTool implements OpenRouterServerTool {

    /** The server-tool discriminator emitted as {@code tools[].type}. */
    public static final String TOOL_TYPE = "openrouter:image_generation";

    private final String model;
    private final Map<String, Object> extraOptions;

    private OpenRouterImageGenerationServerTool(Builder builder) {
        this.model = builder.model;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code openrouter:image_generation} server
     * tool.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the server-tool discriminator {@code "openrouter:image_generation"}. */
    @Override
    public String type() {
        return TOOL_TYPE;
    }

    /**
     * Returns the configured {@code parameters.model} slug, or {@code null}
     * when unset (defaults to {@code "openai/gpt-5-image"}).
     */
    public String model() {
        return model;
    }

    /**
     * Returns the JSON object emitted into the {@code tools} request array:
     * {@code {"type": "openrouter:image_generation"}} plus a
     * {@code parameters} object when at least one option is set.
     */
    @Override
    public JSONObject toJson() {
        JSONObject tool = new JSONObject();
        tool.put("type", TOOL_TYPE);

        JSONObject parameters = new JSONObject();
        if (model != null) {
            parameters.put("model", model);
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            parameters.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }

        if (!parameters.isEmpty()) {
            tool.put("parameters", parameters);
        }
        return tool;
    }

    /**
     * Builder for the {@code openrouter:image_generation} server tool. Only
     * explicitly configured fields are emitted; a verbatim
     * {@link #option(String, Object)} escape hatch covers the
     * {@code image_config}-style provider-specific keys.
     */
    public static final class Builder {

        private String model;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code parameters.model}: the image generation model to use
         * (e.g. {@code "openai/gpt-5-image"}). Defaults to
         * {@code "openai/gpt-5-image"} when unset.
         */
        public Builder model(String model) {
            this.model = model;
            return this;
        }

        /**
         * Adds a {@code parameters} entry verbatim - escape hatch for the
         * {@code image_config}-style provider-specific keys (e.g.
         * {@code aspect_ratio}, {@code quality}, {@code size},
         * {@code background}, {@code output_format},
         * {@code output_compression}, {@code moderation}). Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterImageGenerationServerTool}.
         */
        public OpenRouterImageGenerationServerTool build() {
            return new OpenRouterImageGenerationServerTool(this);
        }
    }
}
