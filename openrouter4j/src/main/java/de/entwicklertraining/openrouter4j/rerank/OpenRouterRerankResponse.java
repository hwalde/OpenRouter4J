package de.entwicklertraining.openrouter4j.rerank;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of POST /rerank: the reranked documents (sorted by relevance
 * descending), the model that served the request and the usage.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}/empty
 * convention: a field that is absent (or a malformed body) yields {@code null}
 * (or an empty list) instead of an exception. Use {@link #getJson()} to
 * inspect the raw response.
 */
public final class OpenRouterRerankResponse extends OpenRouterResponse<OpenRouterRerankRequest> {

    OpenRouterRerankResponse(JSONObject json, OpenRouterRerankRequest request) {
        super(json, request);
    }

    /**
     * @return every result of the response as {@link OpenRouterRerankResult}
     *         views in API order (relevance descending), empty when
     *         {@code results} is absent or not an array
     */
    public List<OpenRouterRerankResult> results() {
        List<OpenRouterRerankResult> result = new ArrayList<>();
        try {
            JSONArray results = json.optJSONArray("results");
            if (results != null) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject entry = results.optJSONObject(i);
                    if (entry != null) {
                        result.add(new OpenRouterRerankResult(entry));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code id} - unique identifier for the rerank response
     * (ORID format, e.g. {@code gen-rerank-1234567890-abc}).
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
     * JSON path: {@code model} - the model that served the request (may
     * differ from the requested model when routing substituted an endpoint).
     *
     * @return the value, or {@code null} when absent
     */
    public String model() {
        try {
            return json.optString("model", null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code provider} - the provider that served the rerank
     * request (e.g. {@code Cohere}).
     *
     * @return the value, or {@code null} when absent
     */
    public String provider() {
        try {
            return json.optString("provider", null);
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
     * JSON path: {@code usage.total_tokens} - total tokens used.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long totalTokens() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("total_tokens") || usage.isNull("total_tokens")) {
                return null;
            }
            return usage.optLong("total_tokens");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.search_units} - search units consumed (Cohere
     * billing unit; billed per document/query size).
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Long searchUnits() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("search_units") || usage.isNull("search_units")) {
                return null;
            }
            return usage.optLong("search_units");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code usage.cost} - cost of the request in credits.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double cost() {
        try {
            JSONObject usage = usage();
            if (usage == null || !usage.has("cost") || usage.isNull("cost")) {
                return null;
            }
            return usage.optDouble("cost");
        } catch (Exception e) {
            return null;
        }
    }
}
