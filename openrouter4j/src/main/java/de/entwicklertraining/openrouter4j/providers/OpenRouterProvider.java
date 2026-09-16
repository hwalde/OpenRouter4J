package de.entwicklertraining.openrouter4j.providers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A typed view of one provider row of GET /providers ({@code data[]}):
 * a provider integrated on OpenRouter.
 *
 * <p>All accessors follow the swallow-and-return-{@code null} convention; use
 * {@link #json()} for fields without a typed accessor.
 */
public final class OpenRouterProvider {

    private final JSONObject json;

    OpenRouterProvider(JSONObject json) {
        this.json = json;
    }

    /**
     * @return the raw provider row behind this view
     */
    public JSONObject json() {
        return json;
    }

    /**
     * JSON path: {@code slug} - the provider slug used by the provider-routing
     * options ({@code providerOnly("slug")}, {@code providerIgnore("slug")})
     * and by {@code sortBy(...)}/{@code providerOption(slug, ...)}.
     *
     * @return the value, or {@code null} when absent
     */
    public String slug() {
        return json.optString("slug", null);
    }

    /**
     * JSON path: {@code name} - the human-readable provider name.
     *
     * @return the value, or {@code null} when absent
     */
    public String name() {
        return json.optString("name", null);
    }

    /**
     * JSON path: {@code headquarters} - ISO 3166-1 Alpha-2 country code of
     * the provider headquarters.
     *
     * @return the value, or {@code null} when absent
     */
    public String headquarters() {
        return json.optString("headquarters", null);
    }

    /**
     * JSON path: {@code datacenters[]} - ISO 3166-1 Alpha-2 country codes of
     * the provider datacenter locations.
     *
     * @return the country codes, empty when absent
     */
    public List<String> datacenters() {
        List<String> result = new ArrayList<>();
        try {
            JSONArray centers = json.optJSONArray("datacenters");
            if (centers != null) {
                for (int i = 0; i < centers.length(); i++) {
                    String value = centers.optString(i, null);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        } catch (Exception ignored) {
            // swallow: keep whatever was parsed before the failure
        }
        return result;
    }

    /**
     * JSON path: {@code privacy_policy_url}.
     *
     * @return the value, or {@code null} when absent
     */
    public String privacyPolicyUrl() {
        return json.optString("privacy_policy_url", null);
    }

    /**
     * JSON path: {@code status_page_url}.
     *
     * @return the value, or {@code null} when absent
     */
    public String statusPageUrl() {
        return json.optString("status_page_url", null);
    }

    /**
     * JSON path: {@code terms_of_service_url}.
     *
     * @return the value, or {@code null} when absent
     */
    public String termsOfServiceUrl() {
        return json.optString("terms_of_service_url", null);
    }
}
