package de.entwicklertraining.openrouter4j.video;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET /videos/models: the video generation models of the catalog.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterVideoModelsResponse
        extends OpenRouterResponse<OpenRouterVideoModelsRequest> {

    OpenRouterVideoModelsResponse(JSONObject json, OpenRouterVideoModelsRequest request) {
        super(json, request);
    }

    /**
     * @return every video model as {@link OpenRouterVideoModel} views, empty
     *         when {@code data} is absent or not an array
     */
    public List<OpenRouterVideoModel> models() {
        List<OpenRouterVideoModel> result = new ArrayList<>();
        try {
            JSONArray data = json.optJSONArray("data");
            if (data != null) {
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterVideoModel(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * One video generation model of the discovery listing.
     */
    public static final class OpenRouterVideoModel {

        private final JSONObject json;

        OpenRouterVideoModel(JSONObject json) {
            this.json = json;
        }

        /**
         * JSON path: {@code id} - the model id (e.g. {@code google/veo-3.1}).
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
         * JSON path: {@code canonical_slug} - the canonical slug of the
         * model.
         *
         * @return the value, or {@code null} when absent
         */
        public String canonicalSlug() {
            return optString("canonical_slug");
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
         * JSON path: {@code supported_resolutions} - the supported output
         * resolutions (e.g. {@code 720p}), empty when absent.
         *
         * @return the resolutions, never {@code null}
         */
        public List<String> supportedResolutions() {
            return optStringList("supported_resolutions");
        }

        /**
         * JSON path: {@code supported_aspect_ratios} - the supported output
         * aspect ratios (e.g. {@code 16:9}), empty when absent.
         *
         * @return the aspect ratios, never {@code null}
         */
        public List<String> supportedAspectRatios() {
            return optStringList("supported_aspect_ratios");
        }

        /**
         * JSON path: {@code supported_sizes} - the supported output sizes
         * ({@code WIDTHxHEIGHT}), empty when absent.
         *
         * @return the sizes, never {@code null}
         */
        public List<String> supportedSizes() {
            return optStringList("supported_sizes");
        }

        /**
         * JSON path: {@code supported_durations} - the supported video
         * durations in seconds, empty when absent.
         *
         * @return the durations, never {@code null}
         */
        public List<Integer> supportedDurations() {
            List<Integer> result = new ArrayList<>();
            try {
                JSONArray arr = json.optJSONArray("supported_durations");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        Object value = arr.opt(i);
                        if (value instanceof Number number) {
                            result.add(number.intValue());
                        }
                    }
                }
            } catch (Exception ignored) {
                // swallow: keep whatever was parsed before the failure
            }
            return result;
        }

        /**
         * JSON path: {@code supported_frame_images} - the supported frame
         * image types ({@code first_frame}, {@code last_frame}), empty when
         * absent.
         *
         * @return the frame image types, never {@code null}
         */
        public List<String> supportedFrameImages() {
            return optStringList("supported_frame_images");
        }

        /**
         * JSON path: {@code generate_audio} - whether the model supports
         * generating audio alongside video.
         *
         * @return the value, or {@code null} when absent
         */
        public Boolean generateAudio() {
            return optBoolean("generate_audio");
        }

        /**
         * JSON path: {@code seed} - whether the model supports deterministic
         * generation via the seed parameter.
         *
         * @return the value, or {@code null} when absent
         */
        public Boolean seed() {
            return optBoolean("seed");
        }

        /**
         * JSON path: {@code allowed_passthrough_parameters} - the parameters
         * allowed to be passed through to the provider, empty when absent.
         *
         * @return the parameter names, never {@code null}
         */
        public List<String> allowedPassthroughParameters() {
            return optStringList("allowed_passthrough_parameters");
        }

        /**
         * @return the raw {@code pricing_skus} object (pricing SKUs with the
         *         provider prefix stripped, values as strings), or
         *         {@code null} when absent
         */
        public JSONObject pricingSkus() {
            try {
                return json.optJSONObject("pricing_skus");
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

        private Boolean optBoolean(String key) {
            try {
                if (!json.has(key) || json.isNull(key)) {
                    return null;
                }
                return json.optBoolean(key);
            } catch (Exception e) {
                return null;
            }
        }

        private List<String> optStringList(String key) {
            List<String> result = new ArrayList<>();
            try {
                JSONArray arr = json.optJSONArray(key);
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
