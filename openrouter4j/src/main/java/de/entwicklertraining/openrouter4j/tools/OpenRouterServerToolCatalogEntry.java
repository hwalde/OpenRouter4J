package de.entwicklertraining.openrouter4j.tools;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one entry of {@code data[]} on the server-tool catalog
 * (GET /tools, schema {@code ServerTool}): one tool OpenRouter can run on
 * behalf of a model - its accepted {@code tools[].type} spellings per API
 * format, the engines behind it with their pricing, and how many endpoints
 * run it natively.
 *
 * <p><b>Spelling validation:</b> {@link #id()} is the canonical
 * {@code openrouter:*} name and the stable {@code tools[].type} on every API
 * format in {@link #supportedApiFormats()}; {@link #aliases()} lists every
 * other spelling each format accepts (including the canonical one) and what
 * the call surfaces as under it. {@code GET /tools/{name}} accepts any of
 * those spellings, but a lookup by a non-canonical one answers HTTP 308
 * with the canonical URL only in the {@code Location} header, which this
 * client cannot follow (see {@link OpenRouterToolGetRequest}) - match the
 * spelling here and look the tool up by the enclosing {@link #id()}.
 *
 * <p><b>Engines and billing:</b> {@link #defaultEngine()} applies where the
 * endpoint runs the tool itself, otherwise {@link #fallbackEngine()}; a
 * request pinned to a data region skips either engine when its
 * {@code data_regions} does not list that region. Who bills an engine's
 * calls is answered per engine by
 * {@link OpenRouterServerToolEngine#pricingSource()}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public class OpenRouterServerToolCatalogEntry {

    private final JSONObject json;

    OpenRouterServerToolCatalogEntry(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw catalog row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - the canonical {@code openrouter:*} name; the
     * stable {@code tools[].type} on every API format in
     * {@link #supportedApiFormats()}.
     *
     * @return the canonical name, or {@code null} when absent or not a
     *         string
     */
    public String id() {
        Object value = json.opt("id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code name} - the human-readable tool name (e.g.
     * {@code Web search}).
     *
     * @return the name, or {@code null} when absent or not a string
     */
    public String name() {
        Object value = json.opt("name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code summary} - one sentence for cards and search
     * results; never sent to a model.
     *
     * @return the summary, or {@code null} when absent or not a string
     */
    public String summary() {
        Object value = json.opt("summary");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code tool_description} - the description sent upstream
     * as the function tool description.
     *
     * @return the tool description, or {@code null} when absent or not a
     *         string
     */
    public String toolDescription() {
        Object value = json.opt("tool_description");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code status} - {@code active} or {@code deprecated};
     * unknown values are passed through verbatim.
     *
     * @return the status, or {@code null} when absent or not a string
     */
    public String status() {
        Object value = json.opt("status");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code docs_url} - where the tool is documented.
     *
     * @return the documentation URL, or {@code null} when absent or not a
     *         string
     */
    public String docsUrl() {
        Object value = json.opt("docs_url");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code aliases[]} - every spelling accepted in
     * {@code tools[].type} for this tool, including the canonical
     * {@link #id()}, and what the call surfaces as under each. Non-object
     * entries are dropped.
     *
     * @return the accepted spellings, empty when absent
     */
    public List<OpenRouterServerToolInputName> aliases() {
        List<OpenRouterServerToolInputName> result = new ArrayList<>();
        try {
            JSONArray aliases = json.optJSONArray("aliases");
            if (aliases != null) {
                for (int i = 0; i < aliases.length(); i++) {
                    JSONObject row = aliases.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolInputName(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code supported_api_formats[]} - the API formats whose
     * {@code tools[].type} accepts this tool's spellings ({@code responses},
     * {@code chat-completions}, {@code anthropic-messages}; unknown values
     * are passed through verbatim). Non-string entries are dropped.
     *
     * @return the supported formats, empty when absent
     */
    public List<String> supportedApiFormats() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray formats = json.optJSONArray("supported_api_formats");
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
     * JSON path: {@code default_engine} - the engine a request without
     * {@code parameters.engine} (or with {@code engine: "auto"}) runs on;
     * {@code native} applies where the endpoint runs the tool itself,
     * otherwise {@link #fallbackEngine()}. A request pinned to a data region
     * skips either engine when its {@code data_regions} does not list that
     * region. {@code null} for a tool without an engine parameter.
     *
     * @return the default engine, or {@code null} when absent, not a
     *         string or JSON {@code null}
     */
    public String defaultEngine() {
        if (!json.has("default_engine") || json.isNull("default_engine")) {
            return null;
        }
        Object value = json.opt("default_engine");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code fallback_engine} - the engine an unpinned request
     * runs on when {@link #defaultEngine()} is {@code native} and the
     * endpoint does not run the tool itself; {@code null} when there is no
     * fallback.
     *
     * @return the fallback engine, or {@code null} when absent, not a
     *         string or JSON {@code null}
     */
    public String fallbackEngine() {
        if (!json.has("fallback_engine") || json.isNull("fallback_engine")) {
            return null;
        }
        Object value = json.opt("fallback_engine");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code parameters_schema} - JSON Schema for the
     * caller-side {@code tools[].parameters} object.
     *
     * @return the schema object, or {@code null} when absent or not an
     *         object
     */
    public JSONObject parametersSchema() {
        return json.optJSONObject("parameters_schema");
    }

    /**
     * JSON path: {@code input_schema} - JSON Schema of the arguments the
     * model emits when calling the tool.
     *
     * @return the schema object, or {@code null} when absent or not an
     *         object
     */
    public JSONObject inputSchema() {
        return json.optJSONObject("input_schema");
    }

    /**
     * JSON path: {@code output_schema} - JSON Schema of the result returned
     * to the model; {@code null} when undeclared.
     *
     * @return the schema object, or {@code null} when absent, JSON
     *         {@code null} or not an object
     */
    public JSONObject outputSchema() {
        return json.optJSONObject("output_schema");
    }

    /**
     * JSON path: {@code engines[]} - the engines a request can select via
     * {@code parameters.engine}. Non-object entries are dropped.
     *
     * @return the engines, empty when absent
     */
    public List<OpenRouterServerToolEngine> engines() {
        List<OpenRouterServerToolEngine> result = new ArrayList<>();
        try {
            JSONArray engines = json.optJSONArray("engines");
            if (engines != null) {
                for (int i = 0; i < engines.length(); i++) {
                    JSONObject row = engines.optJSONObject(i);
                    if (row != null) {
                        result.add(new OpenRouterServerToolEngine(row));
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code native_support} - the endpoints whose provider runs
     * this tool itself during inference. Zero for tools no provider runs
     * natively.
     *
     * @return the native-support view, or {@code null} when absent or not
     *         an object
     */
    public OpenRouterServerToolNativeSupport nativeSupport() {
        JSONObject support = json.optJSONObject("native_support");
        return support != null ? new OpenRouterServerToolNativeSupport(support) : null;
    }
}