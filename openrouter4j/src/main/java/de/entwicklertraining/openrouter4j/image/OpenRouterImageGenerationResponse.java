package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Response of POST /images: the generated image(s) as base64-encoded bytes
 * plus the token and cost usage of the generation.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterImageGenerationResponse
        extends OpenRouterResponse<OpenRouterImageGenerationRequest> {

    OpenRouterImageGenerationResponse(JSONObject json, OpenRouterImageGenerationRequest request) {
        super(json, request);
    }

    /**
     * @return every generated image as {@link OpenRouterImage} views, empty
     *         when {@code data} is absent or not an array
     */
    public List<OpenRouterImage> images() {
        List<OpenRouterImage> result = new ArrayList<>();
        try {
            org.json.JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterImage(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * Convenience lookup of the first generated image.
     *
     * @return the first image, or {@code null} when no image was returned
     */
    public OpenRouterImage firstImage() {
        List<OpenRouterImage> images = images();
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * JSON path: {@code created} - Unix timestamp (seconds) when the image
     * was generated. Trap (observed live 2026-09-15): POST /images currently
     * returns {@code created: 0} regardless of the provider - do not rely on
     * this field for ordering or display.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long created() {
        try {
            if (!json.has("created") || json.isNull("created")) {
                return null;
            }
            Object value = json.get("created");
            return value instanceof Number number ? number.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the raw {@code usage} object, or {@code null} when absent
     */
    public JSONObject usage() {
        try {
            return json.optJSONObject("usage");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.prompt_tokens} - tokens counted for the prompt
     * (including images, input audio and tools, if any).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long promptTokens() {
        return optUsageLong("prompt_tokens");
    }

    /**
     * JSON path: {@code usage.completion_tokens} - tokens generated.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long completionTokens() {
        return optUsageLong("completion_tokens");
    }

    /**
     * JSON path: {@code usage.total_tokens} - sum of prompt and completion
     * tokens.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalTokens() {
        return optUsageLong("total_tokens");
    }

    /**
     * JSON path: {@code usage.cost} - the cost of the completion in USD.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double cost() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("cost") || usage.isNull("cost")) {
                return null;
            }
            Object value = usage.get("cost");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long optUsageLong(String key) {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has(key) || usage.isNull(key)) {
                return null;
            }
            Object value = usage.get(key);
            return value instanceof Number number ? number.longValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * One generated image of the response: the base64-encoded bytes and, when
     * the provider reported it, the media type.
     */
    public static final class OpenRouterImage {

        private final JSONObject json;

        OpenRouterImage(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code b64_json} - the base64-encoded image bytes (for
         * SVG output the UTF-8 SVG markup, base64-encoded).
         *
         * @return the base64 string, or {@code null} when absent
         */
        public String base64() {
            try {
                return json.optString("b64_json", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the decoded image bytes, or {@code null} when
         *         {@code b64_json} is absent or not valid base64
         */
        public byte[] bytes() {
            String encoded = base64();
            if (encoded == null) {
                return null;
            }
            try {
                return Base64.getDecoder().decode(encoded);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        /**
         * JSON path: {@code media_type} - the MIME type of the image, e.g.
         * {@code image/png}, {@code image/jpeg}, {@code image/webp},
         * {@code image/svg+xml}. May be omitted when the format could not be
         * determined.
         *
         * @return the media type, or {@code null} when absent
         */
        public String mediaType() {
            try {
                return json.optString("media_type", null);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
