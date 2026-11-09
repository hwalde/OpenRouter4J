package de.entwicklertraining.openrouter4j.privateendpoints;

import org.json.JSONObject;

/**
 * A typed view of the endpoint shape the managed lifecycle routes return
 * (schema {@code ManagedPrivateEndpoint}): everything
 * {@link OpenRouterPrivateEndpointSummary} carries plus the two data-policy
 * attestations and the completion cap. This is what create (201), activate,
 * enable and disable answer with.
 *
 * <p>It is deliberately smaller than the full endpoint view
 * ({@link OpenRouterPrivateEndpoint}): no pricing, no
 * {@code declared_parameters}, no {@code tested_parameters} and no
 * {@code settings} - read those with GET /private-endpoints/{id}.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention;
 * use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterManagedPrivateEndpoint extends OpenRouterPrivateEndpointSummary {

    OpenRouterManagedPrivateEndpoint(JSONObject json) {
        super(json);
    }

    /**
     * JSON path: {@code declared_zdr} - whether you attest this deployment
     * retains no prompt or completion data. {@code null} means undeclared
     * (and is also the answer when the field is absent or not a boolean).
     * Self-declared and not verified by OpenRouter.
     *
     * @return the attestation, or {@code null} when undeclared or malformed
     */
    public Boolean declaredZdr() {
        Object value = json().opt("declared_zdr");
        return value instanceof Boolean flag ? flag : null;
    }

    /**
     * JSON path: {@code declared_region} - where you attest this deployment
     * processes data: {@code global} (not regional), {@code europe} or
     * {@code us}; {@code null} means undeclared (and is also the answer when
     * the field is absent or not a string). Unknown spellings pass through
     * verbatim. Self-declared and not verified by OpenRouter.
     *
     * @return the attestation, or {@code null} when undeclared or malformed
     */
    public String declaredRegion() {
        Object value = json().opt("declared_region");
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
        Object value = json().opt("max_completion_tokens");
        return value instanceof Number number ? number.intValue() : null;
    }
}
