package de.entwicklertraining.openrouter4j.privateendpoints;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A typed view of one private endpoint with its upstream configuration and
 * current pricing (schema {@code PrivateEndpoint}) - the shape
 * GET /private-endpoints/{id} and PATCH /private-endpoints/{id} answer with.
 *
 * <p>Unlike {@link OpenRouterPrivateEndpointSummary} this view carries no
 * {@code created_at}, but adds {@code provider_slug}, {@code base_url},
 * {@code upstream_model_id}, {@code pricing}, {@code declared_parameters},
 * {@code tested_parameters} and {@code settings}. Note that a create response
 * does NOT echo {@code declared_parameters} or {@code tested_parameters} -
 * read them here via GET /private-endpoints/{id}.
 *
 * <p>{@code declared_parameters} and {@code tested_parameters} are lists of
 * plain parameter-name strings on the wire (the request side uses the same
 * string shape - schema {@code PrivateEndpointDeclaredParameter} is a string
 * enum, not an object), and unknown parameter names pass through verbatim.
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterPrivateEndpoint {

    private final JSONObject json;

    OpenRouterPrivateEndpoint(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw endpoint object behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code id} - stable identifier of the private endpoint
     * (UUID).
     *
     * @return the id, or {@code null} when absent or not a string
     */
    public String id() {
        Object value = json.opt("id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code status} - lifecycle state: {@code draft}, {@code active}
     * or {@code disabled}. Unknown spellings pass through verbatim.
     *
     * @return the status, or {@code null} when absent or not a string
     */
    public String status() {
        Object value = json.opt("status");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_permaslug} - permanent slug of the model this
     * endpoint serves.
     *
     * @return the permaslug, or {@code null} when absent or not a string
     */
    public String modelPermaslug() {
        Object value = json.opt("model_permaslug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_slug} - public model slug, or {@code null}
     * when the model is no longer listed (or when the field is absent or not
     * a string).
     *
     * @return the model slug, or {@code null}
     */
    public String modelSlug() {
        Object value = json.opt("model_slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code model_name} - display name of the model, or
     * {@code null} when the model is no longer listed (or when the field is
     * absent or not a string).
     *
     * @return the model display name, or {@code null}
     */
    public String modelName() {
        Object value = json.opt("model_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code provider_name} - display name of the upstream
     * provider.
     *
     * @return the provider display name, or {@code null} when absent or not
     *         a string
     */
    public String providerName() {
        Object value = json.opt("provider_name");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code provider_slug} - slug of the upstream provider, or
     * {@code null} when the provider is no longer listed (or when the field
     * is absent or not a string).
     *
     * @return the provider slug, or {@code null}
     */
    public String providerSlug() {
        Object value = json.opt("provider_slug");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code base_url} - HTTPS base URL of the deployment, or
     * {@code null} for providers whose URL is derived from the BYOK
     * credential (Azure, Amazon Bedrock, Google Vertex) (and when the field
     * is absent or not a string).
     *
     * @return the base URL, or {@code null}
     */
    public String baseUrl() {
        Object value = json.opt("base_url");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code upstream_model_id} - model or deployment identifier
     * sent to the upstream provider.
     *
     * @return the upstream model id, or {@code null} when absent or not a
     *         string
     */
    public String upstreamModelId() {
        Object value = json.opt("upstream_model_id");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code declared_zdr} - whether you attest this deployment
     * retains no prompt or completion data; {@code null} means undeclared
     * (and is also the answer when the field is absent or not a boolean).
     *
     * @return the attestation, or {@code null}
     */
    public Boolean declaredZdr() {
        Object value = json.opt("declared_zdr");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code declared_region} - where you attest this deployment
     * processes data ({@code global}, {@code europe}, {@code us}; unknown
     * spellings pass through verbatim); {@code null} means undeclared (and
     * is also the answer when the field is absent or not a string).
     *
     * @return the attestation, or {@code null}
     */
    public String declaredRegion() {
        Object value = json.opt("declared_region");
        return value instanceof String text ? text : null;
    }

    /**
     * JSON path: {@code max_completion_tokens} - most completion tokens a
     * request routed to this endpoint can produce, or {@code null} when the
     * endpoint sets no cap beyond the model context length (and when the
     * field is absent or not a number).
     *
     * @return the completion cap, or {@code null}
     */
    public Integer maxCompletionTokens() {
        Object value = json.opt("max_completion_tokens");
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * JSON path: {@code pricing} - the negotiated per-token rates reported
     * for requests routed to this endpoint.
     *
     * @return the pricing view, or {@code null} when absent or not an object
     */
    public OpenRouterPrivateEndpointPricing pricing() {
        try {
            JSONObject pricing = json.optJSONObject("pricing");
            return pricing != null ? new OpenRouterPrivateEndpointPricing(pricing) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * JSON path: {@code declared_parameters} - parameters you declared the
     * upstream supports. Requests that use a parameter only route to this
     * endpoint when it is tested or declared. Plain parameter-name strings
     * (schema {@code PrivateEndpointDeclaredParameter}); unknown names pass
     * through verbatim; non-string entries are dropped.
     *
     * @return the declared parameter names, empty when absent or malformed
     */
    public List<String> declaredParameters() {
        return stringList(json.opt("declared_parameters"));
    }

    /**
     * JSON path: {@code tested_parameters} - parameters a validation run
     * proved the upstream honors ({@code tools}, {@code tool_choice},
     * {@code response_format}, {@code structured_outputs}, {@code stop}).
     * Read-only; cleared when the upstream configuration changes. Non-string
     * entries are dropped.
     *
     * @return the tested parameter names, empty when absent or malformed
     */
    public List<String> testedParameters() {
        return stringList(json.opt("tested_parameters"));
    }

    /**
     * JSON path: {@code settings} - the capabilities and limits routing uses
     * for this endpoint.
     *
     * @return the settings view, or {@code null} when absent or not an
     *         object
     */
    public OpenRouterPrivateEndpointSettings settings() {
        try {
            JSONObject settings = json.optJSONObject("settings");
            return settings != null ? new OpenRouterPrivateEndpointSettings(settings) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static List<String> stringList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof JSONArray array) {
            for (int i = 0; i < array.length(); i++) {
                Object entry = array.opt(i);
                if (entry instanceof String text) {
                    result.add(text);
                }
            }
        }
        return result;
    }
}
