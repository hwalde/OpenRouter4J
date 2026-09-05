package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provider-specific image generation configuration sent as the
 * {@code image_config} request field of a chat completion. The keys and values
 * vary by model/provider - this type carries them verbatim, with typed
 * convenience methods for the keys the OpenRouter documentation names
 * ({@code num_images}, {@code aspect_ratio}, {@code resolution}).
 * <p>
 * JSON field: {@code image_config}. Default: unset (the key is not sent).
 * Trap: the key is only meaningful for multimodal-output models and must be
 * combined with {@code modalities} containing {@code "image"} - without that,
 * providers ignore it.
 *
 * @see <a href="https://openrouter.ai/docs/guides/overview/multimodal/image-generation">Image generation</a>
 */
public final class OpenRouterImageConfig {

    private final Map<String, Object> options;

    private OpenRouterImageConfig(Map<String, Object> options) {
        this.options = options;
    }

    /**
     * Creates a new, empty builder for {@code image_config}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Sets {@code image_config.num_images}: how many images the model should
     * generate for the response (provider-dependent).
     */
    public Integer numImages() {
        return intValue("num_images");
    }

    /**
     * Sets {@code image_config.aspect_ratio}: the requested aspect ratio of
     * generated images, e.g. {@code "1:1"} or {@code "16:9"} (provider-dependent).
     */
    public String aspectRatio() {
        return stringValue("aspect_ratio");
    }

    /**
     * Sets {@code image_config.resolution}: the requested image resolution,
     * e.g. {@code "1K"}, {@code "2K"} or {@code "4K"} (provider-dependent).
     */
    public String resolution() {
        return stringValue("resolution");
    }

    /**
     * Sets {@code image_config.quality}: the requested image quality,
     * e.g. {@code "high"} (provider-dependent).
     */
    public String quality() {
        return stringValue("quality");
    }

    /**
     * Returns the raw option value for {@code key}, or {@code null} when unset.
     */
    public Object option(String key) {
        return options.get(key);
    }

    /**
     * Returns an unmodifiable copy of all configured options, in insertion order.
     */
    public Map<String, Object> options() {
        return Map.copyOf(options);
    }

    /**
     * Returns the JSON object emitted as the {@code image_config} request field.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> e : options.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    private Integer intValue(String key) {
        Object value = options.get(key);
        return value instanceof Number n ? n.intValue() : null;
    }

    private String stringValue(String key) {
        Object value = options.get(key);
        return value instanceof String s ? s : null;
    }

    /** Builder for {@link OpenRouterImageConfig}. */
    public static final class Builder {

        private final Map<String, Object> options = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code num_images}: how many images the model should generate
         * (provider-dependent).
         */
        public Builder numImages(Integer numImages) {
            options.put("num_images", numImages);
            return this;
        }

        /**
         * Sets {@code aspect_ratio}: the requested aspect ratio of generated
         * images, e.g. {@code "1:1"} or {@code "16:9"} (provider-dependent).
         */
        public Builder aspectRatio(String aspectRatio) {
            options.put("aspect_ratio", aspectRatio);
            return this;
        }

        /**
         * Sets {@code resolution}: the requested image resolution, e.g.
         * {@code "1K"}, {@code "2K"} or {@code "4K"} (provider-dependent).
         */
        public Builder resolution(String resolution) {
            options.put("resolution", resolution);
            return this;
        }

        /**
         * Sets {@code quality}: the requested image quality, e.g. {@code "high"}
         * (provider-dependent).
         */
        public Builder quality(String quality) {
            options.put("quality", quality);
            return this;
        }

        /**
         * Sets an arbitrary provider-specific {@code image_config} key verbatim.
         * Use this for keys without a typed convenience method.
         *
         * @param key the option key as documented for the target model/provider
         * @param value the option value (String, Number, Boolean or null)
         */
        public Builder option(String key, Object value) {
            options.put(key, value);
            return this;
        }

        public OpenRouterImageConfig build() {
            return new OpenRouterImageConfig(new LinkedHashMap<>(options));
        }
    }
}
