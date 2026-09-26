package de.entwicklertraining.openrouter4j.systemone;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response of POST /systemone: the answers for every requested
 * question key, the resolved judging model, the serving provider and the
 * usage. Documented wire shape (openapi.yaml {@code POST /systemone} reuses
 * the {@code DecisionsResponse} schema); the example body below was recorded
 * live against the Decisions twin on 2026-09-19 - {@code /systemone} itself
 * has not been live-verified:
 * <pre>{@code
 * {
 *   "model": "typesafe/jev-1.13-20260917",
 *   "answers": {
 *     "is_bug":  {"type": "noul",   "noul": 0.96},
 *     "team":    {"type": "choice", "choice": "payments",
 *                 "probabilities": {"payments": 0.77, ...}, "confidence": 0.66},
 *     "urgency": {"type": "score",  "score": 1.99, "legend": {"0": "...", ...},
 *                 "probabilities": {...}, "confidence": 0.99}
 *   },
 *   "usage": {"input_tokens": 476, "output_tokens": 70, "cost": 0.000019992},
 *   "id": "gen-dec-...",
 *   "provider": "TypeSafe"
 * }
 * }</pre>
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty map) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 *
 * <p>Trap: bare System One model ids (e.g. {@code jev-1.13}) are mapped onto
 * the {@code typesafe/} namespace server-side; the response's {@code model}
 * carries the resolved versioned id. The wire shape is the Decisions one.
 */
public final class OpenRouterSystemOneResponse extends OpenRouterResponse<OpenRouterSystemOneRequest> {

    OpenRouterSystemOneResponse(JSONObject json, OpenRouterSystemOneRequest request) {
        super(json, request);
    }

    /**
     * @return the generation id of this request, or {@code null} when absent
     */
    public String id() {
        return stringOf("id");
    }

    /**
     * @return the judging model that served the request (the resolved
     *         version, e.g. {@code typesafe/jev-1.13-20260917}), or
     *         {@code null} when absent
     */
    public String model() {
        return stringOf("model");
    }

    /**
     * @return the serving provider (e.g. {@code TypeSafe}), or {@code null}
     *         when absent
     */
    public String provider() {
        return stringOf("provider");
    }

    /**
     * @return every answer keyed like the request's {@code questions}, as
     *         raw JSON objects, empty when {@code answers} is absent
     */
    public Map<String, JSONObject> answers() {
        Map<String, JSONObject> result = new LinkedHashMap<>();
        try {
            JSONObject answers = json.optJSONObject("answers");
            if (answers != null) {
                for (String key : answers.keySet()) {
                    JSONObject entry = answers.optJSONObject(key);
                    if (entry != null) {
                        result.put(key, entry);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * Typed view of one answer - use {@link OpenRouterSystemOneAnswer#type()}
     * to discriminate between the {@code noul}, {@code choice} and
     * {@code score} shapes before reading the value accessors.
     *
     * @param key the question key from the request
     * @return the answer view, or {@code null} when no answer carries that key
     */
    public OpenRouterSystemOneAnswer answer(String key) {
        JSONObject entry = answers().get(key);
        return entry == null ? null : new OpenRouterSystemOneAnswer(key, entry);
    }

    /**
     * @return the input tokens of the evaluation, or {@code null} when absent
     */
    public Long inputTokens() {
        return longOf(new String[]{"input_tokens"});
    }

    /**
     * @return the output tokens of the evaluation, or {@code null} when absent
     */
    public Long outputTokens() {
        return longOf(new String[]{"output_tokens"});
    }

    /**
     * @return the cost of the evaluation in USD, or {@code null} when absent
     */
    public Double cost() {
        try {
            JSONObject usage = json.optJSONObject("usage");
            if (usage == null) {
                return null;
            }
            Object value = usage.opt("cost");
            return value instanceof Number ? ((Number) value).doubleValue() : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String stringOf(String field) {
        try {
            Object value = json.opt(field);
            return value instanceof String ? (String) value : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long longOf(String[] fields) {
        try {
            JSONObject usage = json.optJSONObject("usage");
            if (usage == null) {
                return null;
            }
            for (String field : fields) {
                Object value = usage.opt(field);
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Typed view of one entry of the {@code answers} object, discriminated
     * on its {@code type} field: {@code noul} carries the probability
     * {@code noul} (how strongly the statement evaluates towards
     * {@code true}), {@code choice} carries the chosen option string in
     * {@code choice}, {@code score} carries the numeric position in
     * {@code score}. Both {@code choice} and {@code score} additionally carry
     * {@code probabilities}, {@code confidence} and (score only) the
     * {@code legend} mapping step indices to their descriptions.
     *
     * <p>Trap: the {@code noul} value is a probability between 0 and 1 on the
     * wire, not a boolean - read it with {@link #noulProbability()} and apply
     * your own threshold.
     */
    public static final class OpenRouterSystemOneAnswer {

        private final String key;
        private final JSONObject json;

        private OpenRouterSystemOneAnswer(String key, JSONObject json) {
            this.key = key;
            this.json = json;
        }

        /** @return the question key this answer answers */
        public String key() {
            return key;
        }

        /** @return the discriminator value ({@code noul}, {@code choice}, {@code score}), or {@code null} */
        public String type() {
            Object value = json.opt("type");
            return value instanceof String ? (String) value : null;
        }

        /** @return the raw answer object as received */
        public JSONObject json() {
            return json;
        }

        /**
         * @return the {@code noul} probability (0..1) for {@code noul}
         *         answers, or {@code null} for other types
         */
        public Double noulProbability() {
            return doubleOf("noul");
        }

        /**
         * @return the chosen option key for {@code choice} answers, or
         *         {@code null} for other types
         */
        public String choice() {
            Object value = json.opt("choice");
            return value instanceof String ? (String) value : null;
        }

        /**
         * @return the numeric score position for {@code score} answers, or
         *         {@code null} for other types
         */
        public Double score() {
            return doubleOf("score");
        }

        /**
         * @return the per-option (choice) or per-step (score) probability
         *         object, or {@code null} when absent
         */
        public JSONObject probabilities() {
            JSONObject value = json.optJSONObject("probabilities");
            return value == null ? null : new JSONObject(value.toString());
        }

        /**
         * @return the confidence value, or {@code null} when absent
         */
        public Double confidence() {
            return doubleOf("confidence");
        }

        /**
         * @return the {@code legend} object mapping score step indices to
         *         their descriptions ({@code score} answers only), or
         *         {@code null} when absent
         */
        public JSONObject legend() {
            JSONObject value = json.optJSONObject("legend");
            return value == null ? null : new JSONObject(value.toString());
        }

        private Double doubleOf(String field) {
            try {
                Object value = json.opt(field);
                return value instanceof Number ? ((Number) value).doubleValue() : null;
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
