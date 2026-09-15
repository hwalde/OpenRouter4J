package de.entwicklertraining.openrouter4j.audio;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /audio/transcriptions: the transcribed text and, when
 * available, the usage statistics; with {@code response_format=verbose_json}
 * additionally task, language, duration, segments and words.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterSttResponse extends OpenRouterResponse<OpenRouterSttRequest> {

    OpenRouterSttResponse(JSONObject json, OpenRouterSttRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code text} - the transcribed text.
     *
     * @return the value, or {@code null} when absent
     */
    public String text() {
        try {
            return json.optString("text", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code task} - the task performed (present with
     * {@code verbose_json}).
     *
     * @return the value, or {@code null} when absent
     */
    public String task() {
        try {
            return json.optString("task", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code language} - the detected or forced language (present
     * with {@code verbose_json}).
     *
     * @return the value, or {@code null} when absent
     */
    public String language() {
        try {
            return json.optString("language", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code duration} - the duration of the input audio in
     * seconds (present with {@code verbose_json}).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double duration() {
        try {
            if (!json.has("duration") || json.isNull("duration")) {
                return null;
            }
            Object value = json.get("duration");
            return value instanceof Number number ? number.doubleValue() : null;
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
     * JSON path: {@code usage.input_tokens} - input tokens billed for the
     * request.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long inputTokens() {
        return optUsageLong("input_tokens");
    }

    /**
     * JSON path: {@code usage.output_tokens} - output tokens generated.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long outputTokens() {
        return optUsageLong("output_tokens");
    }

    /**
     * JSON path: {@code usage.total_tokens} - total tokens used
     * (input + output).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalTokens() {
        return optUsageLong("total_tokens");
    }

    /**
     * JSON path: {@code usage.seconds} - the duration of the input audio in
     * seconds (audio billing unit).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double seconds() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("seconds") || usage.isNull("seconds")) {
                return null;
            }
            Object value = usage.get("seconds");
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.cost} - the total cost of the request in USD.
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

    /**
     * @return the timestamped transcript segments (present with
     *         {@code verbose_json}), empty when absent
     */
    public List<OpenRouterSttSegment> segments() {
        List<OpenRouterSttSegment> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("segments");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject entry = arr.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterSttSegment(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * @return the timestamped words (present when the provider returned
     *         word-level timestamps), empty when absent
     */
    public List<OpenRouterSttWord> words() {
        List<OpenRouterSttWord> result = new ArrayList<>();
        try {
            JSONArray arr = json.optJSONArray("words");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject entry = arr.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterSttWord(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
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
     * A timestamped transcript segment (present with {@code verbose_json}).
     */
    public static final class OpenRouterSttSegment {

        private final JSONObject json;

        OpenRouterSttSegment(JSONObject json) {
            this.json = json;
        }

        /** @return the segment index, or {@code null} when absent */
        public Integer id() {
            return optInt("id");
        }

        /** @return the start time in seconds, or {@code null} when absent */
        public Double start() {
            return optDouble("start");
        }

        /** @return the end time in seconds, or {@code null} when absent */
        public Double end() {
            return optDouble("end");
        }

        /** @return the transcribed text of the segment, or {@code null} when absent */
        public String text() {
            try {
                return json.optString("text", null);
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * @return the speaker index for the segment (present when the
         *         provider returned diarization data), or {@code null}
         */
        public Integer speaker() {
            return optInt("speaker");
        }

        private Integer optInt(String key) {
            try {
                if (!json.has(key) || json.isNull(key)) {
                    return null;
                }
                Object value = json.get(key);
                return value instanceof Number number ? number.intValue() : null;
            } catch (Exception e) {
                return null;
            }
        }

        private Double optDouble(String key) {
            try {
                if (!json.has(key) || json.isNull(key)) {
                    return null;
                }
                Object value = json.get(key);
                return value instanceof Number number ? number.doubleValue() : null;
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * A timestamped word (present when the provider returned word-level
     * timestamps).
     */
    public static final class OpenRouterSttWord {

        private final JSONObject json;

        OpenRouterSttWord(JSONObject json) {
            this.json = json;
        }

        /** @return the word, or {@code null} when absent */
        public String word() {
            try {
                return json.optString("word", null);
            } catch (Exception e) {
                return null;
            }
        }

        /** @return the start time in seconds, or {@code null} when absent */
        public Double start() {
            return optDouble("start");
        }

        /** @return the end time in seconds, or {@code null} when absent */
        public Double end() {
            return optDouble("end");
        }

        /**
         * @return the speaker index for the word (present when the provider
         *         returned diarization data), or {@code null}
         */
        public Integer speaker() {
            try {
                if (!json.has("speaker") || json.isNull("speaker")) {
                    return null;
                }
                Object value = json.get("speaker");
                return value instanceof Number number ? number.intValue() : null;
            } catch (Exception e) {
                return null;
            }
        }

        private Double optDouble(String key) {
            try {
                if (!json.has(key) || json.isNull(key)) {
                    return null;
                }
                Object value = json.get(key);
                return value instanceof Number number ? number.doubleValue() : null;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
