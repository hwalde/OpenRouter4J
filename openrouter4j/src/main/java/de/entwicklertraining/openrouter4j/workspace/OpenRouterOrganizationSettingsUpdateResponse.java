package de.entwicklertraining.openrouter4j.workspace;

import de.entwicklertraining.openrouter4j.OpenRouterResponse;
import org.json.JSONObject;

/**
 * Response of PATCH /organization/settings: the updated settings of the
 * organization associated with the authenticated management key.
 *
 * <p>Follows the swallow-and-return-null convention.
 */
public final class OpenRouterOrganizationSettingsUpdateResponse extends OpenRouterResponse<OpenRouterOrganizationSettingsUpdateRequest> {


    OpenRouterOrganizationSettingsUpdateResponse(JSONObject json, OpenRouterOrganizationSettingsUpdateRequest request) {
        super(json, request);
    }

    /**
 * JSON path: {@code data} - the organization settings as a typed view, or
 * {@code null} when absent or malformed.
 */
    public OpenRouterOrganizationSettings data() {
        JSONObject data = json.optJSONObject("data");
        return data != null ? new OpenRouterOrganizationSettings(data) : null;
    }
}
