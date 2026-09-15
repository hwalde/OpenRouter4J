package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /images/models/{author}/{slug}/endpoints: the full
 * per-endpoint records of one image generation model.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterImageModelEndpointsResponse
        extends OpenRouterResponse<OpenRouterImageModelEndpointsRequest> {

    OpenRouterImageModelEndpointsResponse(JSONObject json, OpenRouterImageModelEndpointsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the model slug the endpoints belong to.
     *
     * @return the value, or {@code null} when absent
     */
    public String id() {
        try {
            return json.optString("id", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return every serving endpoint as {@link OpenRouterImageEndpoint}
     *         views, empty when {@code endpoints} is absent or not an array
     */
    public List<OpenRouterImageEndpoint> endpoints() {
        List<OpenRouterImageEndpoint> result = new ArrayList<>();
        try {
            JSONArray endpoints = json.optJSONArray("endpoints");
            if (endpoints != null) {
                for (int i = 0; i < endpoints.length(); i++) {
                    JSONObject entry = endpoints.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterImageEndpoint(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * One serving endpoint of an image generation model.
     */
    public static final class OpenRouterImageEndpoint {

        private final JSONObject json;

        OpenRouterImageEndpoint(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code provider_name} - the provider display name.
         *
         * @return the value, or {@code null} when absent
         */
        public String providerName() {
            return optString("provider_name");
        }

        /**
         * JSON path: {@code provider_slug} - the provider slug.
         *
         * @return the value, or {@code null} when absent
         */
        public String providerSlug() {
            return optString("provider_slug");
        }

        /**
         * JSON path: {@code provider_tag} - the provider tag for
         * request-side selection; may be {@code null} in the API.
         *
         * @return the value, or {@code null} when absent
         */
        public String providerTag() {
            return optString("provider_tag");
        }

        /**
         * JSON path: {@code supports_streaming} - whether this endpoint
         * supports native SSE streaming ({@code stream: true} in the
         * request).
         *
         * @return the value, or {@code null} when absent
         */
        public Boolean supportsStreaming() {
            try {
                if (!json.has("supports_streaming") || json.isNull("supports_streaming")) {
                    return null;
                }
                return json.optBoolean("supports_streaming");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * JSON path: {@code allowed_passthrough_parameters} - the
         * provider-specific options accepted under
         * {@code provider.options[provider_slug]}, empty when absent.
         *
         * @return the parameter names, never {@code null}
         */
        public List<String> allowedPassthroughParameters() {
            List<String> result = new ArrayList<>();
            try {
                JSONArray arr = json.optJSONArray("allowed_passthrough_parameters");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        String value = arr.optString(i, null);
                        if (value != null) {
                            result.add(value);
                        }
                    }
                }
            } catch (Exception ignored) {
                // swallow: keep whatever was parsed before the failure
            }
            return result;
        }

        /**
         * JSON path: {@code pricing} - the billable pricing lines of this
         * endpoint (each with {@code billable}, {@code unit},
         * {@code cost_usd} and optionally {@code variant}), or {@code null}
         * when absent.
         *
         * @return the raw pricing array, or {@code null} when absent
         */
        public JSONArray pricing() {
            try {
                return json.optJSONArray("pricing");
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the raw {@code supported_parameters} object - the
         *         definitive set of parameters this endpoint accepts for the
         *         model - or {@code null} when absent
         */
        public JSONObject supportedParameters() {
            try {
                return json.optJSONObject("supported_parameters");
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw JSON object of this endpoint entry */
        public JSONObject json() {
            return json;
        }

        private String optString(String key) {
            try {
                return json.optString(key, null);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
