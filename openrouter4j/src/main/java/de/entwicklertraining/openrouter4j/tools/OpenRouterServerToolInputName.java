package de.entwicklertraining.openrouter4j.tools;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code aliases[]} on a server-tool catalog
 * entry (schema {@code ServerToolInputName}): one spelling accepted in
 * {@code tools[].type} for this tool - including the canonical {@code id} -
 * and what the call surfaces as under that spelling.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolInputName {

    private final JSONObject json;

    OpenRouterServerToolInputName(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw alias row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code type} - the accepted spelling of
     * {@code tools[].type} (e.g. {@code web_search_20250305}).
     *
     * @return the spelling, or {@code null} when absent or not a string
     */
    public String type() {
        Object value = json.opt("type");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code api_formats[]} - the API formats that accept this
     * spelling in {@code tools[].type} ({@code responses},
     * {@code chat-completions}, {@code anthropic-messages}; unknown values
     * are passed through verbatim). Non-string entries are dropped.
     *
     * @return the accepting formats, empty when absent
     */
    public List<String> apiFormats() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray formats = json.optJSONArray("api_formats");
            if (formats != null) {
                for (int i = 0; i < formats.length(); i++) {
                    Object value = formats.opt(i);
                    if (value instanceof String text) {
                        result.add(text);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code output_names[]} - what the call surfaces as on each
     * accepting API format when requested under this spelling; a format is
     * absent when the call is not visible to the caller. Non-object entries
     * are dropped.
     *
     * @return the output names, empty when absent
     */
    public List<OpenRouterServerToolOutputName> outputNames() {
        List<OpenRouterServerToolOutputName> result = new ArrayList<>();
        try {
            JSONArray names = json.optJSONArray("output_names");
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    JSONObject row = names.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolOutputName(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}