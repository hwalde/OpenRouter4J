package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

import java.util.Objects;

/**
 * One entry of the Anthropic {@code safeguards} request array on the Messages
 * endpoint: a server-side safeguard Anthropic evaluates alongside the
 * completion (e.g. {@code dangerous_tool_use}). Every entry is
 * {@code {"type": <string> [, "classifier_context": <object>]}}.
 * <p>
 * Create instances through {@link #of(String)},
 * {@link #of(String, JSONObject)} or the verbatim escape hatch
 * {@link #raw(JSONObject)} for safeguard types Anthropic adds after this
 * library was released.
 *
 * @see <a href="https://openrouter.ai/docs/api/api-reference/anthropic-messages/create-a-message">Anthropic Messages API</a>
 */
public final class OpenRouterSafeguard {

    private final JSONObject json;

    private OpenRouterSafeguard(JSONObject json) {
        this.json = json;
    }

    /**
     * A safeguard with no classifier context
     * ({@code {"type":"..."}}).
     *
     * @param type the safeguard type (e.g. {@code dangerous_tool_use})
     * @return the safeguard
     */
    public static OpenRouterSafeguard of(String type) {
        Objects.requireNonNull(type, "type must not be null");
        JSONObject json = new JSONObject();
        json.put("type", type);
        return new OpenRouterSafeguard(json);
    }

    /**
     * A safeguard with a classifier context object
     * ({@code {"type":"...","classifier_context":{...}}}).
     *
     * @param type the safeguard type (e.g. {@code dangerous_tool_use})
     * @param classifierContext the free-form classifier context (e.g.
     *        {@code {"permission_mode":"auto","v":1}}); emitted only when
     *        non-null
     * @return the safeguard
     */
    public static OpenRouterSafeguard of(String type, JSONObject classifierContext) {
        Objects.requireNonNull(type, "type must not be null");
        JSONObject json = new JSONObject();
        json.put("type", type);
        if (classifierContext != null) {
            json.put("classifier_context", new JSONObject(classifierContext.toString()));
        }
        return new OpenRouterSafeguard(json);
    }

    /**
     * Verbatim escape hatch: emits the given safeguard object unchanged. Use
     * it for safeguard types Anthropic adds after this library was released.
     *
     * @param safeguard the raw safeguard JSON; must contain a {@code type}
     *        field
     * @return the safeguard
     */
    public static OpenRouterSafeguard raw(JSONObject safeguard) {
        Objects.requireNonNull(safeguard, "safeguard must not be null");
        if (!safeguard.has("type")) {
            throw new IllegalArgumentException("A safeguard must carry a 'type' field");
        }
        return new OpenRouterSafeguard(new JSONObject(safeguard.toString()));
    }

    /**
     * The JSON object emitted into the {@code safeguards} request array.
     */
    public JSONObject toJson() {
        return new JSONObject(json.toString());
    }

    @Override
    public String toString() {
        return json.toString();
    }
}
