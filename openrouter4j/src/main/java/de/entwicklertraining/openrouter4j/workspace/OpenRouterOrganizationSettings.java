package de.entwicklertraining.openrouter4j.workspace;

import org.json.JSONObject;

/**
 * A typed view of the organization settings (the {@code data} of GET and PATCH
 * /organization/settings): the organization the settings belong to and the
 * filtered-model-catalog switch.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention
 * (malformed values answer {@code null}, so a malformed response looks like an
 * empty one); use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterOrganizationSettings {

    private final JSONObject json;

    OpenRouterOrganizationSettings(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw JSON row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
 * JSON path: {@code id} - ID of the organization the settings belong to
 * (non-string values answer {@code null}).
 */
    public String id() {
        Object value = json.opt("id");
        return value instanceof String text ? text : null;
    }
    /**
 * JSON path: {@code is_filtered_model_catalog_enabled} - when {@code true}, the
 * signed-in dashboard lists only the models the organization can use.
 *
 * <p>Trap: {@code GET /api/v1/models} called with an API key always returns
 * only the models that key can use, whatever this setting says; anonymous
 * requests receive the public catalog. The setting therefore controls the
 * dashboard's model list, not the API's - reading it does not change what
 * {@code /models} answers for a key.
 */
    public Boolean isFilteredModelCatalogEnabled() {
        Object value = json.opt("is_filtered_model_catalog_enabled");
        return value instanceof Boolean flag ? flag : null;
    }
}
