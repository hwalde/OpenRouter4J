package de.entwicklertraining.openrouter4j.tools;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code engines[]} on a server-tool catalog
 * entry (schema {@code ServerToolEngine}): one engine a request can select
 * via {@code parameters.engine}, with who executes it, who bills it and at
 * what rates.
 *
 * <p><b>Billing:</b> {@link #pricingSource()} names who bills the engine's
 * calls beyond the model's tokens - {@code openrouter} bills the
 * {@link #pricing()} rows from the caller's credits, {@code provider} is a
 * per-call tool fee on the inference call at the provider's own rates,
 * {@code byok} is the engine's vendor against the caller's own key and
 * {@code none} is free. The {@code pricing} rows are non-empty exactly when
 * {@code pricing_source} is {@code openrouter}.
 *
 * <p><b>Regions:</b> a request pinned to a data region can only use engines
 * whose {@link #dataRegions()} list that region; {@code global} is always
 * listed.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolEngine {

    private final JSONObject json;

    OpenRouterServerToolEngine(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw engine row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the {@code parameters.engine} value that
     * selects this engine; {@code null} for the single engine of a tool
     * without an engine parameter.
     *
     * @return the engine id, or {@code null} when absent, not a string or
     *         JSON {@code null}
     */
    public String id() {
        if (!json.has("id") || json.isNull("id")) {
            return null;
        }
        Object value = json.opt("id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code name} - the engine display name (e.g. {@code Exa}).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code executed_by} - who runs the tool call: {@code provider}
     * (the model provider during inference), {@code openrouter}, or
     * {@code client} (the caller's application after the call is returned);
     * unknown values are passed through verbatim.
     *
     * @return the executor, or {@code null} when absent or not a string
     */
    public String executedBy() {
        Object value = json.opt("executed_by");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code data_regions[]} - the data regions whose requests
     * this engine serves without leaving the region ({@code global},
     * {@code europe}, {@code us}; unknown values are passed through
     * verbatim). A request pinned to a region can only use engines that list
     * it; {@code global} is always listed. Non-string entries are dropped.
     *
     * @return the served regions, empty when absent
     */
    public List<String> dataRegions() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray regions = json.optJSONArray("data_regions");
            if (regions != null) {
                for (int i = 0; i < regions.length(); i++) {
                    Object value = regions.opt(i);
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
     * JSON path: {@code byok} - whether the engine needs the caller's own
     * key, saved in plugin settings: {@code required} (OpenRouter holds
     * none), {@code optional} (OpenRouter's key is used unless the caller
     * saved one) or {@code none}; unknown values are passed through
     * verbatim.
     *
     * @return the BYOK mode, or {@code null} when absent or not a string
     */
    public String byok() {
        Object value = json.opt("byok");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code default_mode} - the {@code parameters.mode} a
     * request without one runs and bills as; {@code null} for engines
     * without modes. The {@code pricing[]} row with this {@code mode} is
     * what an unmoded request bills.
     *
     * @return the default mode, or {@code null} when absent, not a string
     *         or JSON {@code null}
     */
    public String defaultMode() {
        if (!json.has("default_mode") || json.isNull("default_mode")) {
            return null;
        }
        Object value = json.opt("default_mode");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code pricing_source} - who bills the engine's calls
     * beyond the model's tokens: {@code openrouter} (the {@link #pricing()}
     * rows, from the caller's credits), {@code provider} (a per-call tool
     * fee from the model's provider on the inference call, at its own
     * rates), {@code byok} (the engine's vendor, against the caller's own
     * key) or {@code none} (no charge); unknown values are passed through
     * verbatim.
     *
     * @return the pricing source, or {@code null} when absent or not a
     *         string
     */
    public String pricingSource() {
        Object value = json.opt("pricing_source");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code pricing_doc_url} - where the rates are documented
     * when {@code pricing_source} is {@code provider} or {@code byok};
     * {@code null} otherwise.
     *
     * @return the documentation URL, or {@code null} when absent, not a
     *         string or JSON {@code null}
     */
    public String pricingDocUrl() {
        if (!json.has("pricing_doc_url") || json.isNull("pricing_doc_url")) {
            return null;
        }
        Object value = json.opt("pricing_doc_url");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code pricing[]} - the rows OpenRouter bills; non-empty
     * exactly when {@code pricing_source} is {@code openrouter}. Non-object
     * entries are dropped.
     *
     * @return the price rows, empty when absent
     */
    public List<OpenRouterServerToolPrice> pricing() {
        List<OpenRouterServerToolPrice> result = new ArrayList<>();
        try {
            JSONArray pricing = json.optJSONArray("pricing");
            if (pricing != null) {
                for (int i = 0; i < pricing.length(); i++) {
                    JSONObject row = pricing.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolPrice(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }
}