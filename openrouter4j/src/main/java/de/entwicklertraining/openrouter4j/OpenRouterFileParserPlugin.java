package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed implementation of the OpenRouter {@code file-parser} plugin: parses
 * files (PDFs in particular) so their text reaches the model. Emitted into the
 * {@code plugins} request array as {@code {"id": "file-parser", ...}} with
 * only the explicitly configured fields.
 * <p>
 * JSON field: {@code plugins[].id = "file-parser"}. Default: no plugin is sent.
 * <p>
 * Trap: the {@code pdf.engine} values {@code "mistral-ocr"},
 * {@code "native"} and {@code "cloudflare-ai"} are the documented engines;
 * {@code "pdf-text"} is deprecated and automatically redirected to
 * {@code "cloudflare-ai"}.
 *
 * @see <a href="https://openrouter.ai/docs/guides/features/plugins">Plugins</a>
 */
public final class OpenRouterFileParserPlugin implements OpenRouterPlugin {

    /** The plugin discriminator emitted as {@code plugins[].id}. */
    public static final String PLUGIN_ID = "file-parser";

    private final Boolean enabled;
    private final String pdfEngine;
    private final Map<String, Object> extraOptions;

    private OpenRouterFileParserPlugin(Builder builder) {
        this.enabled = builder.enabled;
        this.pdfEngine = builder.pdfEngine;
        this.extraOptions = new LinkedHashMap<>(builder.extraOptions);
    }

    /**
     * Creates a new builder for the {@code file-parser} plugin.
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Returns the plugin discriminator {@code "file-parser"} ({@code plugins[].id}). */
    @Override
    public String id() {
        return PLUGIN_ID;
    }

    /**
     * Returns the configured {@code enabled} value, or {@code null} when unset
     * (the key is not sent and {@code true} applies).
     */
    public Boolean enabled() {
        return enabled;
    }

    /**
     * Returns the configured {@code pdf.engine} value
     * ({@code "mistral-ocr"}, {@code "native"} or {@code "cloudflare-ai"}),
     * or {@code null} when unset.
     */
    public String pdfEngine() {
        return pdfEngine;
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("id", PLUGIN_ID);
        if (enabled != null) {
            json.put("enabled", enabled);
        }
        if (pdfEngine != null) {
            json.put("pdf", new JSONObject().put("engine", pdfEngine));
        }
        for (Map.Entry<String, Object> e : extraOptions.entrySet()) {
            json.put(e.getKey(), e.getValue() == null ? JSONObject.NULL : e.getValue());
        }
        return json;
    }

    /**
     * Builder for the {@code file-parser} plugin. Only explicitly configured
     * fields are emitted; a verbatim {@link #option(String, Object)} escape
     * hatch covers keys this library does not know yet.
     */
    public static final class Builder {

        private Boolean enabled;
        private String pdfEngine;
        private final Map<String, Object> extraOptions = new LinkedHashMap<>();

        private Builder() {
        }

        /**
         * Sets {@code enabled}: set to {@code false} to disable the plugin for
         * this request. Default: unset ({@code true} applies).
         */
        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets {@code pdf.engine}: the PDF parsing engine. Documented values:
         * {@code "mistral-ocr"}, {@code "native"}, {@code "cloudflare-ai"}
         * (the deprecated {@code "pdf-text"} is redirected to
         * {@code "cloudflare-ai"} server-side).
         */
        public Builder pdfEngine(String engine) {
            this.pdfEngine = engine;
            return this;
        }

        /**
         * Adds a plugin field verbatim - escape hatch for keys this library
         * does not know yet. The key must not be {@code "id"}. Null values are
         * emitted as JSON {@code null}.
         */
        public Builder option(String key, Object value) {
            if ("id".equals(key)) {
                throw new IllegalArgumentException("The 'id' field is set from the plugin type and must not be set via option()");
            }
            this.extraOptions.put(key, value);
            return this;
        }

        /**
         * Builds the {@link OpenRouterFileParserPlugin} value type.
         */
        public OpenRouterFileParserPlugin build() {
            return new OpenRouterFileParserPlugin(this);
        }
    }
}
