package de.entwicklertraining.openrouter4j.image;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /images/models: the image generation models of the catalog.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterImageModelsResponse
        extends OpenRouterResponse<OpenRouterImageModelsRequest> {

    OpenRouterImageModelsResponse(JSONObject json, OpenRouterImageModelsRequest request) {
        super(json, request);
    }

    /**
     * @return every image model as {@link OpenRouterImageModel} views, empty
     *         when {@code data} is absent or not an array
     */
    public List<OpenRouterImageModel> models() {
        List<OpenRouterImageModel> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterImageModel(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * One image generation model of the discovery listing.
     */
    public static final class OpenRouterImageModel {

        private final JSONObject json;

        OpenRouterImageModel(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code id} - the model slug (e.g.
         * {@code bytedance-seed/seedream-4.5}).
         *
         * @return the value, or {@code null} when absent
         */
        public String id() {
            return optString("id");
        }

        /**
         * JSON path: {@code name} - the display name.
         *
         * @return the value, or {@code null} when absent
         */
        public String name() {
            return optString("name");
        }

        /**
         * JSON path: {@code description} - the model description.
         *
         * @return the value, or {@code null} when absent
         */
        public String description() {
            return optString("description");
        }

        /**
         * JSON path: {@code created} - Unix timestamp (seconds) of when the
         * model was created.
         *
         * @return the value, or {@code null} when absent or not a number
         */
        public Long created() {
            return optLong("created");
        }

        /**
         * JSON path: {@code architecture.input_modalities} - the supported
         * input modalities (e.g. {@code text}, {@code image}), empty when
         * absent.
         *
         * @return the modality list, never {@code null}
         */
        public List<String> inputModalities() {
            return optModalityList("input_modalities");
        }

        /**
         * JSON path: {@code architecture.output_modalities} - the supported
         * output modalities (e.g. {@code image}), empty when absent.
         *
         * @return the modality list, never {@code null}
         */
        public List<String> outputModalities() {
            return optModalityList("output_modalities");
        }

        /**
         * JSON path: {@code supports_streaming} - whether any endpoint of the
         * model supports native SSE streaming on the dedicated Image API
         * ({@code stream: true}); OR across endpoints.
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
         * JSON path: {@code endpoints} - the relative URL of the full
         * per-endpoint records for this model (e.g.
         * {@code /api/v1/images/models/bytedance-seed/seedream-4.5/endpoints});
         * queryable via {@code client.images().modelEndpoints("author/slug")}.
         *
         * @return the value, or {@code null} when absent
         */
        public String endpointsUrl() {
            return optString("endpoints");
        }

        /**
         * @return the raw {@code supported_parameters} object (a map of
         *         parameter name to capability descriptor), or {@code null}
         *         when absent
         */
        public JSONObject supportedParameters() {
            try {
                return json.optJSONObject("supported_parameters");
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the raw JSON object of this model entry */
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

        private Long optLong(String key) {
            try {
                if (!json.has(key) || json.isNull(key)) {
                    return null;
                }
                Object value = json.get(key);
                return value instanceof Number number ? number.longValue() : null;
            } catch (Exception e) {
                return null;
            }
        }

        private List<String> optModalityList(String key) {
            List<String> result = new ArrayList<>();
            try {
                JSONObject architecture = json.optJSONObject("architecture");
                JSONArray arr = architecture != null ? architecture.optJSONArray(key) : null;
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
    }
}
