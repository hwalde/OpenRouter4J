package de.entwicklertraining.openrouter4j.rerank;

import org.json.JSONObject;

/**
 * A typed view of one result of a POST /rerank response (the
 * {@code results[]} items): the input index of the document, its relevance
 * score and the echoed document object.
 *
 * <p>All accessors follow the library's swallow-and-return-{@code null}
 * convention. Use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterRerankResult {

    private final JSONObject json;

    OpenRouterRerankResult(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw response entry behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code index} - the index of the document in the original
     * input list ({@code documents} of the request).
     *
     * @return the value, or {@code null} when absent
     */
    public Integer index() {
        try {
            Object value = json.opt("index");
            return value instanceof Integer ? (Integer) value : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code relevance_score} - the relevance of the document to
     * the query; results arrive sorted by this score descending.
     *
     * @return the value, or {@code null} when absent or not a number
     */
    public Double relevanceScore() {
        try {
            Object value = json.opt("relevance_score");
            return value instanceof Number ? ((Number) value).doubleValue() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return the echoed {@code document} object ({@code text} and/or
     *         {@code image} of the original input), or {@code null} when absent
     */
    public JSONObject document() {
        try {
            return json.optJSONObject("document");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code document.text} - the text of the echoed document.
     * Plain-string inputs are echoed in this field.
     *
     * @return the value, or {@code null} when absent
     */
    public String documentText() {
        try {
            JSONObject document = document();
            return document != null ? document.optString("text", null) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code document.image} - the image (URL or data URI) of the
     * echoed document, present only for structured image documents.
     *
     * @return the value, or {@code null} when absent
     */
    public String documentImage() {
        try {
            JSONObject document = document();
            return document != null ? document.optString("image", null) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
