package de.entwicklertraining.openrouter4j.tools;

import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

/**
 * A typed view of the {@code data} row of GET /tools/{name} (schema
 * {@code ServerToolDetails}): one server tool - read by canonical name (an
 * alias lookup answers HTTP 308 and fails, see
 * {@link OpenRouterToolGetRequest}) - with the models that run it natively.
 *
 * <p>Carries every accessor of {@link OpenRouterServerToolCatalogEntry};
 * the details shape additionally requires {@code native_support.models} -
 * surfaced both as {@link #nativeModels()} and as
 * {@link OpenRouterServerToolNativeSupport#models()} of
 * {@link OpenRouterServerToolCatalogEntry#nativeSupport()} (which answers
 * empty on the list rows of GET /tools, where the schema omits the models).
 *
 * <p>All accessors follow the swallow-and-return-{@code null}/empty
 * convention; use {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterServerToolCatalogDetails extends OpenRouterServerToolCatalogEntry {

    OpenRouterServerToolCatalogDetails(JSONObject json) {
        super(json);
    }

    /**
     * JSON path: {@code native_support.models[]} - the models that run this
     * tool natively. Required on this shape; the list rows of GET /tools
     * carry no models and answer empty. Non-object entries are dropped.
     *
     * @return the native models, empty when absent
     */
    public List<OpenRouterServerToolNativeModel> nativeModels() {
        OpenRouterServerToolNativeSupport support = nativeSupport();
        return support != null ? support.models() : Collections.emptyList();
    }
}