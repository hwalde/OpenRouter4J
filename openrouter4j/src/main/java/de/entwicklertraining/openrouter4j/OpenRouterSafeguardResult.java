package de.entwicklertraining.openrouter4j;

import org.json.JSONObject;

/**
 * Typed view over one entry of the top-level {@code safeguard_results} array
 * returned when OpenRouter Armor ({@link OpenRouterArmorPlugin}) evaluated the
 * client tool calls of a response (schema {@code ArmorSafeguardResult}).
 * <p>
 * Mirrors {@link de.entwicklertraining.openrouter4j.messages.OpenRouterMessagesResponse.OpenRouterSafeguardResult}
 * in the root package so Chat Completions and Responses can expose the same
 * {@code type()} / {@code status()} / {@code json()} shape without coupling
 * endpoint packages together.
 * <p>
 * All accessors follow the swallow-and-return-{@code null} convention: a
 * missing or wrong-typed field returns {@code null} rather than throwing or
 * coercing scalars to strings.
 * <p>
 * Traps:
 * <ul>
 *   <li><b>Beta caveat:</b> Armor is in beta; the {@code safeguard_results}
 *       shape may change.</li>
 *   <li><b>Independent of {@code openrouter_metadata}:</b>
 *       {@code safeguard_results} is present whenever Armor evaluated the
 *       response (even when nothing was flagged), without needing the
 *       {@code metadataInResponse(true)} opt-in.</li>
 * </ul>
 *
 * @see OpenRouterArmorPlugin
 */
public final class OpenRouterSafeguardResult {

    private final JSONObject json;

    /**
     * Wraps a {@code safeguard_results[]} JSON object (a {@code null} argument
     * is treated as an empty object so accessors return {@code null}).
     *
     * @param json the raw safeguard result entry, or {@code null}
     */
    public OpenRouterSafeguardResult(JSONObject json) {
        this.json = json == null ? new JSONObject() : json;
    }

    /**
     * JSON path: {@code safeguard_results[].type} - the safeguard discriminator
     * (e.g. {@code "dangerous_tool_use"}).
     *
     * @return the safeguard type string, or {@code null} when absent or not a
     *         string
     */
    public String type() {
        try {
            if (!json.has("type") || json.isNull("type")) {
                return null;
            }
            Object v = json.opt("type");
            return v instanceof String s ? s : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code safeguard_results[].status} - the status object for
     * this safeguard (for {@code "dangerous_tool_use"}: {@code {"type":
     * "available", "tool_uses": {"<call_id>": {"type": "evaluated", "outcome":
     * "flagged"|"not_flagged", "explanation"?: "[Rule Name]"} | {"type":
     * "skipped"} | {"type": "unavailable", "reason": "..."}}}}).
     *
     * @return the status object, or {@code null} when absent or not an object
     */
    public JSONObject status() {
        try {
            return json.optJSONObject("status");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code safeguard_results[].status.tool_uses} - convenience
     * accessor for the per-tool-call verdict map inside {@link #status()}
     * (keyed by {@code tool_calls[].id} on Chat Completions and {@code call_id}
     * on Responses).
     *
     * @return the {@code tool_uses} map object, or {@code null} when absent
     */
    public JSONObject toolUses() {
        try {
            JSONObject s = status();
            return s != null ? s.optJSONObject("tool_uses") : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the underlying {@code safeguard_results[]} JSON object verbatim.
     *
     * @return the raw JSON object (never {@code null})
     */
    public JSONObject json() {
        return json;
    }
}
