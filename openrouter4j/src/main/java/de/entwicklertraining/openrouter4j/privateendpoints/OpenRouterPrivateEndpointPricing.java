package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * The negotiated per-token rates of a private endpoint (schema
 * {@code PrivateEndpointPricing}). The schema serves both directions - the
 * same object is the {@code pricing} field of the create/update request
 * bodies and of the endpoint responses - so this class does both jobs:
 * a read view built from a response ({@link #json()} plus the four
 * accessors) and a request value built with {@link #builder()}.
 *
 * <p>Money never becomes a number here: every rate is a decimal
 * <b>string</b> ({@code "0.0000025"}), matching the wire format. Do not
 * parse into {@code double} - the strings carry more precision than a
 * binary double keeps.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention.
 */
public final class OpenRouterPrivateEndpointPricing {

    private static final Pattern DECIMAL = Pattern.compile("^\\d+(\\.\\d+)?$");

    private final JSONObject json;

    OpenRouterPrivateEndpointPricing(JSONObject json) {
        this.json = json;
    }

    /**
     * Starting point for building the {@code pricing} request value.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the raw pricing object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code prompt} - USD per prompt token, as a decimal string
     * (required in both directions).
     *
     * @return the prompt rate, or {@code null} when absent or not a string
     */
    public String prompt() {
        Object value = json.opt("prompt");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code completion} - USD per completion token, as a decimal
     * string (required in both directions).
     *
     * @return the completion rate, or {@code null} when absent or not a
     *         string
     */
    public String completion() {
        Object value = json.opt("completion");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code cache_read} - USD per cache-read prompt token, as a
     * decimal string. On create an omitted rate is inherited from the public
     * endpoint of the same model and provider (scaled by its cache-read to
     * prompt ratio, or the pricing strategy default when it publishes no
     * cache-read rate); on PATCH omitting it reverts the rate to that
     * default. Required when there is no such endpoint and the provider
     * bills cache reads.
     *
     * @return the cache-read rate, or {@code null} when absent or not a
     *         string
     */
    public String cacheRead() {
        Object value = json.opt("cache_read");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code cache_write} - USD per cache-write prompt token, as
     * a decimal string. Inheritance and revert semantics are the same as
     * {@link #cacheRead()}.
     *
     * @return the cache-write rate, or {@code null} when absent or not a
     *         string
     */
    public String cacheWrite() {
        Object value = json.opt("cache_write");
        return value instanceof String text ? text : null;
    }

    /**
     * Builder for the {@code pricing} request value.
     *
     * <p>Every rate must match the documented decimal-string pattern
     * {@code ^\d+(\.\d+)?$} (validated loudly); {@code prompt} and
     * {@code completion} are required, the cache rates are optional.
     */
    public static final class Builder {

        private String prompt;
        private String completion;
        private String cacheRead;
        private String cacheWrite;

        private Builder() {
        }

        /**
         * Sets the JSON field {@code prompt} (required) - USD per prompt
         * token, as a decimal string (pattern {@code ^\d+(\.\d+)?$},
         * validated loudly).
         *
         * @param prompt the prompt rate
         * @return this builder
         */
        public Builder prompt(String prompt) {
            this.prompt = requireDecimal(prompt, "prompt");
            return this;
        }

        /**
         * Sets the JSON field {@code completion} (required) - USD per
         * completion token, as a decimal string (pattern
         * {@code ^\d+(\.\d+)?$}, validated loudly).
         *
         * @param completion the completion rate
         * @return this builder
         */
        public Builder completion(String completion) {
            this.completion = requireDecimal(completion, "completion");
            return this;
        }

        /**
         * Sets the JSON field {@code cache_read} (optional) - USD per
         * cache-read prompt token, as a decimal string (pattern
         * {@code ^\d+(\.\d+)?$}, validated loudly). Omitted on create:
         * inherited from the public endpoint of the same model and provider;
         * omitted on PATCH: reverted to the pricing strategy default.
         *
         * @param cacheRead the cache-read rate
         * @return this builder
         */
        public Builder cacheRead(String cacheRead) {
            this.cacheRead = cacheRead != null ? requireDecimal(cacheRead, "cache_read") : null;
            return this;
        }

        /**
         * Sets the JSON field {@code cache_write} (optional) - USD per
         * cache-write prompt token, as a decimal string (pattern
         * {@code ^\d+(\.\d+)?$}, validated loudly). Inheritance and revert
         * semantics are the same as {@link #cacheRead(String)}.
         *
         * @param cacheWrite the cache-write rate
         * @return this builder
         */
        public Builder cacheWrite(String cacheWrite) {
            this.cacheWrite = cacheWrite != null ? requireDecimal(cacheWrite, "cache_write") : null;
            return this;
        }

        /**
         * Builds the pricing request value. {@code prompt} and
         * {@code completion} are required.
         *
         * @return the pricing value
         * @throws IllegalStateException when {@code prompt} or
         *         {@code completion} is missing
         */
        public OpenRouterPrivateEndpointPricing build() {
            if (prompt == null) {
                throw new IllegalStateException("prompt is required for private endpoint pricing");
            }
            if (completion == null) {
                throw new IllegalStateException("completion is required for private endpoint pricing");
            }
            JSONObject json = new JSONObject();
            json.put("prompt", prompt);
            json.put("completion", completion);
            if (cacheRead != null) {
                json.put("cache_read", cacheRead);
            }
            if (cacheWrite != null) {
                json.put("cache_write", cacheWrite);
            }
            return new OpenRouterPrivateEndpointPricing(json);
        }

        private static String requireDecimal(String value, String field) {
            if (value == null || !DECIMAL.matcher(value).matches()) {
                throw new IllegalArgumentException(
                        field + " must be a decimal string matching ^\\d+(\\.\\d+)?$");
            }
            return value;
        }
    }
}
