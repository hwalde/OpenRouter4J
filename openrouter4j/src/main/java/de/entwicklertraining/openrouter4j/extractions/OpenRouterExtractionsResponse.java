package de.entwicklertraining.openrouter4j.extractions;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Response of POST /api/alpha/extractions (schema
 * {@code ExtractionsResponse}): the extraction id, the resolved model, the
 * serving provider, the state text spans index into, the truncation flag,
 * one typed answer per requested field and the usage.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty map) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 *
 * <p>Traps: {@code state_text} is nullable and only returned when requested
 * through provider options ("the text that spans index into").
 * {@code truncated} is {@code true} when the provider read only a prefix of
 * the state - the answers then cover that prefix only. This endpoint lives
 * in OpenRouter's alpha namespace ("Alpha: the shape is not stable") and may
 * change or disappear without a major version of the API.
 *
 * <p>Spec-derived: the wire shape follows the published OpenAPI spec
 * (incl. its response example); no live response has been recorded yet.
 */
public final class OpenRouterExtractionsResponse extends OpenRouterResponse<OpenRouterExtractionsRequest> {

    /**
     * Creates the response view.
     *
     * @param json the parsed response body
     * @param request the request that produced this response
     */
    public OpenRouterExtractionsResponse(JSONObject json, OpenRouterExtractionsRequest request) {
        super(json, request);
    }

    /**
     * JSON path: {@code id} - the extraction id (e.g.
     * {@code gen-dec-...}).
     *
     * @return the id, or {@code null} when absent or not a string
     */
    public String id() {
        return stringOf("id");
    }

    /**
     * JSON path: {@code model} - the model that served the request (the
     * resolved id, e.g. {@code cloudflare/clairic}).
     *
     * @return the model, or {@code null} when absent or not a string
     */
    public String model() {
        return stringOf("model");
    }

    /**
     * JSON path: {@code provider} - the serving provider (e.g.
     * {@code Cloudflare}).
     *
     * @return the provider, or {@code null} when absent or not a string
     */
    public String provider() {
        return stringOf("provider");
    }

    /**
     * JSON path: {@code state_text} - the text that {@code span} / spans
     * index into, when requested through provider options. Nullable and
     * normally absent.
     *
     * @return the state text, or {@code null} when absent, JSON null or not
     *         a string
     */
    public String stateText() {
        return stringOf("state_text");
    }

    /**
     * JSON path: {@code truncated} - {@code true} when the provider read
     * only a prefix of the state; the answers then cover that prefix only.
     *
     * @return the flag, or {@code null} when absent or not a boolean
     */
    public Boolean truncated() {
        Object value = json.opt("truncated");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code answers} - one typed answer per requested field,
     * keyed by the caller-chosen field name. Non-object entries are dropped
     * while good siblings survive. The map preserves the iteration order of
     * the parsed {@code answers} object (JSON objects are unordered on the
     * wire).
     *
     * @return the answers keyed like the request's {@code fields}, empty
     *         when {@code answers} is absent
     */
    public Map<String, OpenRouterExtractionAnswer> answers() {
        Map<String, OpenRouterExtractionAnswer> result = new LinkedHashMap<>();
        try {
            JSONObject answers = json.optJSONObject("answers");
            if (answers != null) {
                for (String key : answers.keySet()) {
                    JSONObject entry = answers.optJSONObject(key);
                    if (entry != null) {
                        result.put(key, new OpenRouterExtractionAnswer(key, entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code answers[name]} - one typed answer; use
     * {@link OpenRouterExtractionAnswer#type()} to discriminate between the
     * nine answer shapes before reading the value accessors.
     *
     * @param name the field name from the request
     * @return the answer view, or {@code null} when no answer carries that
     *         name
     */
    public OpenRouterExtractionAnswer answer(String name) {
        JSONObject entry = null;
        try {
            JSONObject answers = json.optJSONObject("answers");
            if (answers != null) {
                entry = answers.optJSONObject(name);
            }
        } catch (Exception ignored) {
            return null;
        }
        return entry == null ? null : new OpenRouterExtractionAnswer(name, entry);
    }

    /**
     * JSON path: {@code usage} - the token accounting of the request.
     *
     * @return the usage view, or {@code null} when absent or not an object
     */
    public OpenRouterExtractionsUsage usage() {
        try {
            JSONObject usage = json.optJSONObject("usage");
            return usage == null ? null : new OpenRouterExtractionsUsage(usage);
        } catch (Exception e) {
            return null;
        }
    }

    private String stringOf(String field) {
        try {
            Object value = json.opt(field);
            return value instanceof String text ? text : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
